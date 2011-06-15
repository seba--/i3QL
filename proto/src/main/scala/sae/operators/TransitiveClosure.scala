package sae.operators

import sae.{Observer, LazyView}
import util.control.Breaks
import collection.immutable.Range.Inclusive
import sae.collections.HashMultiMap
import com.sun.corba.se.spi.servicecontext.ServiceContextData
import collection.mutable.{HashSet, HashMap}
import java.lang.Error
import com.sun.org.apache.xerces.internal.impl.dv.xs.YearDV

/**
 *
 * @author Malte V
 */

trait TransitiveClosure[Domain <: AnyRef, Vertex <: AnyRef] extends LazyView[(Vertex, Vertex)] {
  val source: LazyView[Domain]
  val startVertex: Domain => Vertex
  val endVertex: Domain => Vertex
}

/**
 * Algorithm for:
 * incremental view maintenance for transitive closure of ACYCLIC graphs
 * the algorithm is based on
 * Dong and Su Sigmod 2000 44-50
 *
 */
class HashTransitiveClosureOld[Edge <: AnyRef, Vertex <: AnyRef](val source: LazyView[Edge],
                                                               val startVertex: Edge => Vertex,
                                                               val endVertex: Edge => Vertex) extends TransitiveClosure[Edge, Vertex] with Observer[Edge] {
  //import scala.collection.mutable.Map

  import com.google.common.collect._
  import scala.collection.JavaConversions._


  //all not derived Edges = (Vertex,Vertex)
  val graph = HashMultimap.create[Vertex, Vertex]()

  // TransitiveClosure saved as: Adjacency Hashmultimap
  //    e1 = (Key1, Value1), e2 = (Key1,Value2) ..., ei=(Key2,Value1), ei+1= (key2,Value2) ....   en = (Keyk, ValueN)
  // Key = a Vertex (tail of the edge)
  // Value = all adjacent Vertexes to the key Vertex (head of the edge)
  val tailHeadAdjacencyList = HashMultimap.create[Vertex, Vertex]()
  // reversed TransitiveClosure saved as: Adjacency hashmultimap
  // Key = a Vertex   (head of the edge)
  // Value = all adjacent Vertexes to the key Vertex (tail of the edge)
  val headTailAdjacencyList = HashMultimap.create[Vertex, Vertex]()

  //val adjacencyList = HashMap[Vertex,(HashSet[Vertex],HashSet[Vertex])]()

  source addObserver this

  def lazyInitialize {}

  def lazy_foreach[T](f: ((Vertex, Vertex)) => T) = null

  def added(edge: Edge) {
    if (graph.put(startVertex(edge), endVertex(edge))) {
      if (tailHeadAdjacencyList.put(startVertex(edge), endVertex(edge)) &&
        headTailAdjacencyList.put(endVertex(edge), startVertex(edge)))
        element_added((startVertex(edge), endVertex(edge)))
    }
    //Step 1
    headTailAdjacencyList.get(startVertex(edge)).foreach((x: Vertex) => {
      // the Vertex x has an outgoing Edge e = (x,endVertex(edge))
      if (tailHeadAdjacencyList.put(x, endVertex(edge)) && headTailAdjacencyList.put(endVertex(edge), x))
        element_added((x, endVertex(edge)))
    })
    //Step 2
    tailHeadAdjacencyList.get(endVertex(edge)).foreach((x: Vertex) => {
      // the Vertex x has an incoming Edge e = (startVertex(edge),x)
      if (tailHeadAdjacencyList.put(startVertex(edge), x) && headTailAdjacencyList.put(x, startVertex(edge))) //if (startPoints.put(startVertex(edge), x) && endPoints.put(endVertex(edge), x))
        element_added((startVertex(edge), x))
    })
    //Step 3
    headTailAdjacencyList.get(startVertex(edge)).foreach((x: Vertex) => {
      tailHeadAdjacencyList.get(endVertex(edge)).foreach((y: Vertex) => {
        if (tailHeadAdjacencyList.put(x, y) && headTailAdjacencyList.put(y, x))
          element_added((x, y))
      })
    })
  }

  def removed(e: Edge) {

    graph.remove(startVertex(e), endVertex(e))

    import scala.collection.mutable.Set
    //set of all paths that maybe go through e
    val edges = Set[(Vertex, Vertex)]()
    edges.add((startVertex(e), endVertex(e)))

    headTailAdjacencyList.get(endVertex(e)).foreach((x: Vertex) => {
      if (!graph.containsEntry(x, endVertex(e)))
        edges.add((x, endVertex(e)))
    })
    tailHeadAdjacencyList.get(startVertex(e)).foreach((x: Vertex) => {
      if (!graph.containsEntry(startVertex(e), x))
        edges.add((startVertex(e), x))
    })

    headTailAdjacencyList.get(startVertex(e)).foreach((x: Vertex) => {
      tailHeadAdjacencyList.get(endVertex(e)).foreach((y: Vertex) => {
        //println(x + " " + y)
        edges.add((x, y))
      })
    })

    //remove all found edges
    //creation of "T_ab" (see paper)
    //we dont remove edges that are in Gnew
    headTailAdjacencyList.remove(endVertex(e), startVertex(e))
    tailHeadAdjacencyList.remove(startVertex(e), endVertex(e))
    edges.foreach(x => {
      headTailAdjacencyList.remove(x._2, x._1)
      tailHeadAdjacencyList.remove(x._1, x._2)
    })

    // T_ab ∪ (T_ab ο T_ab) ∪ (T_ab ο T_ab ο T_ab)
    for (i <- 1 to 2) {
      var putBack = List[(Vertex, Vertex)]()
      val mybreaks = new Breaks
      import mybreaks.{break, breakable}
      edges.foreach(x => {
        breakable {
          tailHeadAdjacencyList.get(x._1).foreach((y: Vertex) => {
            if (tailHeadAdjacencyList.containsEntry(y, x._2)) {

              putBack = (x._1, x._2) :: putBack
              break

            }
          })
        }
      })
      putBack.foreach(x => {
        headTailAdjacencyList.put(x._2, x._1)
        tailHeadAdjacencyList.put(x._1, x._2)
        edges.remove(x)
      })
    }
    // TC_old - TC_old
    edges.foreach(x => {
      element_removed(x._1, x._2)
    })

  }

  def updated(oldV: Edge, newV: Edge) {
    //a direct update is not supported
    removed(oldV)
    added(newV)
  }
}

/**
 * Algorithm for:
 * incremental view maintenance for transitive closure of ACYCLIC graphs
 * the algorithm is based on
 * Dong and Su Sigmod 2000 44-50
 *
 */
class HashTransitiveClosure[Edge <: AnyRef, Vertex <: AnyRef](val source: LazyView[Edge],
                                                              val startVertex: Edge => Vertex,
                                                              val endVertex: Edge => Vertex) extends TransitiveClosure[Edge, Vertex] with Observer[Edge] {
  //import scala.collection.mutable.Map

  import com.google.common.collect._
  import scala.collection.JavaConversions._


  //all not derived Edges = (Vertex,Vertex)
  val graph = HashMultimap.create[Vertex, Vertex]()

  // TransitiveClosure saved as: Adjacency Hashmultimap
  //    e1 = (Key1, Value1), e2 = (Key1,Value2) ..., ei=(Key2,Value1), ei+1= (key2,Value2) ....   en = (Keyk, ValueN)
  // Key = a Vertex (tail of the edge)
  // Value = all adjacent Vertexes to the key Vertex (head of the edge)
  //val tailHeadAdjacencyList = HashMultimap.create[Vertex, Vertex]()
  // reversed TransitiveClosure saved as: Adjacency hashmultimap
  // Key = a Vertex   (head of the edge)
  // Value = all adjacent Vertexes to the key Vertex (tail of the edge)
  //val headTailAdjacencyList = HashMultimap.create[Vertex, Vertex]()

  val adjacencyList = HashMap[Vertex, (HashSet[Vertex], HashSet[Vertex])]()

  source addObserver this

  def lazyInitialize {}

  def lazy_foreach[T](f: ((Vertex, Vertex)) => T) = null

  def added(edge: Edge) {
    val adjacencyEdgesToStartVertex = adjacencyList.getOrElseUpdate(startVertex(edge), (HashSet[Vertex](), HashSet[Vertex]()))
    val adjacencyEdgesToEndVertex = adjacencyList.getOrElseUpdate(endVertex(edge), (HashSet[Vertex](), HashSet[Vertex]()))
    if (graph.put(startVertex(edge), endVertex(edge))) {
      if (!adjacencyEdgesToStartVertex._1.contains(endVertex(edge))) {
        adjacencyEdgesToStartVertex._1.add(endVertex(edge))
        adjacencyEdgesToEndVertex._2.add(startVertex(edge))
        //tailHeadAdjacencyList.put(startVertex(edge), endVertex(edge)) &&
        //headTailAdjacencyList.put(endVertex(edge), startVertex(edge)))
        element_added((startVertex(edge), endVertex(edge)))
      }
    }
    //Step 1
    adjacencyEdgesToStartVertex._2.foreach((x: Vertex) => {
      //alle kanten die am startpunkt von e enden
      // the Vertex x has an outgoing Edge e = (x,endVertex(edge))
      val tmp = adjacencyList.getOrElse(x, throw new Error())
      if (!tmp._1.contains(endVertex(edge))) {
        tmp._1.add(endVertex(edge))
        adjacencyEdgesToEndVertex._2.add(x)
        element_added((x, endVertex(edge)))
      }

    })
    //Step 2
    adjacencyEdgesToEndVertex._1.foreach((x: Vertex) => {
      //gibt jetzt auch startvertex(edge) -> x
      val tmp = adjacencyList.getOrElse(x, throw new Error())
      if (!tmp._2.contains(startVertex(edge))) {
        tmp._2.add(startVertex(edge))
        adjacencyEdgesToStartVertex._1.add(x)
        //tailHeadAdjacencyList.put(startVertex(edge), x)
        // && headTailAdjacencyList.put(x, startVertex(edge)))
        element_added((startVertex(edge), x))
      }


    })
    //Step 3
    adjacencyEdgesToStartVertex._2.foreach((x: Vertex) => {
      adjacencyEdgesToEndVertex._1.foreach((y: Vertex) => {
        val tmp = adjacencyList.getOrElse(x, throw new Error())
        if (!tmp._1.contains(y)) {
          tmp._1.add(y)
          adjacencyList.getOrElse(y, throw new Error())._2.add(x)
          //if (tailHeadAdjacencyList.put(x, y) && headTailAdjacencyList.put(y, x))
          element_added((x, y))
        }

      })
    })
  }

  def removed(e: Edge) {

    graph.remove(startVertex(e), endVertex(e))

    import scala.collection.mutable.Set
    //set of all paths that maybe go through e
    val edges = Set[(Vertex, Vertex)]()
    edges.add((startVertex(e), endVertex(e)))
    val adjacencyEdgesToStartVertex = adjacencyList.getOrElse(startVertex(e), throw new Error())
    val adjacencyEdgesToEndVertex = adjacencyList.getOrElse(endVertex(e), throw new Error())

    adjacencyEdgesToEndVertex._2.foreach((x: Vertex) => {
      if (!graph.containsEntry(x, endVertex(e)))
        edges.add((x, endVertex(e)))
    })
    adjacencyEdgesToStartVertex._1.foreach((x: Vertex) => {
      if (!graph.containsEntry(startVertex(e), x))
        edges.add((startVertex(e), x))
    })

    adjacencyEdgesToEndVertex._1.foreach((x: Vertex) => {
      adjacencyEdgesToStartVertex._2.foreach((y: Vertex) => {
        //println(x + " " + y)
        edges.add((y, x))
      })
    })

    //remove all found edges
    //creation of "T_ab" (see paper)
    //we dont remove edges that are in Gnew
    adjacencyEdgesToStartVertex._2.remove(endVertex(e))
    adjacencyEdgesToEndVertex._1.remove(startVertex(e))
    //headTailAdjacencyList.remove(endVertex(e), startVertex(e))
    //tailHeadAdjacencyList.remove(startVertex(e), endVertex(e))
    edges.foreach(x => {
      adjacencyList.getOrElse(x._1, throw new Error())._1.remove(x._2)
      adjacencyList.getOrElse(x._2, throw new Error())._2.remove(x._1)
      //headTailAdjacencyList.remove(x._2, x._1)
      //tailHeadAdjacencyList.remove(x._1, x._2)
    })

    // T_ab ∪ (T_ab ο T_ab) ∪ (T_ab ο T_ab ο T_ab)

    for (i <- 1 to 2) {
      var putBack = List[(Vertex, Vertex)]()
      edges.foreach(x => {
        val mybreaks = new Breaks
        import mybreaks.{break, breakable}
        breakable {
          adjacencyList.getOrElse(x._1, throw new Error())._1.foreach((y: Vertex) => {

            //tailHeadAdjacencyList.get(x._1).foreach((y: Vertex) => {
            //if (tailHeadAdjacencyList.containsEntry(y, x._2)) {
            if (adjacencyList.getOrElse(y, throw new Error())._1.contains(x._2)) {
              putBack = (x._1, x._2) :: putBack
              break
            }
          })
        }
      })
      putBack.foreach(x => {
        adjacencyList.getOrElse(x._1, throw new Error())._1.add(x._2)
        adjacencyList.getOrElse(x._2, throw new Error())._2.add(x._1)
        edges.remove(x)
      })
    }
    // TC_old - TC_old
    edges.foreach(x => {
      element_removed(x._1, x._2)
    })

  }

  def updated(oldV: Edge, newV: Edge) {
    //a direct update is not supported
    removed(oldV)
    added(newV)
  }
}

/**
 * Algorithm for:
 * incremental view maintenance for transitive closure of ACYCLIC graphs
 * the algorithm is based on
 * Dong and Su Sigmod 2000 44-50
 *
 */
/*class SQLTransitiveClosure[Domain <: AnyRef, Vertex <: AnyRef](val source: LazyView[Domain],
                                                               val startVertex: Domain => Vertex,
                                                               val endVertex: Domain => Vertex) extends TransitiveClosure[Domain, Vertex] with Observer[Domain] {
  //import scala.collection.mutable.Map

  import sae.collections.HashMultiMap

  val startPoint = new HashMultiMap(source, startVertex)
  val endPoint = new HashMultiMap(source, endVertex)


  //source addObserver this
  val addObs = new Observer[Domain] {
    def added(edge: Domain) {
      val first = startPoint.get(endVertex(edge))
      val second = endPoint.get(startVertex(edge))
      //val third =
    }

    def removed(v: Domain) {

    }

    def updated(oldV: Domain, newV: Domain) {
      //a direct update is not supported
      removed(oldV)
      added(newV)
    }
  }

  def lazyInitialize {}

  def lazy_foreach[T](f: ((Vertex, Vertex)) => T) = null

  def added(edge: Domain) {

  }

  def removed(v: Domain) {


  }

  def updated(oldV: Domain, newV: Domain) {
    //a direct update is not supported
    removed(oldV)
    added(newV)
  }
}   */