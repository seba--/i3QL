package sae.operators

import sae.{Observer, LazyView}
import util.control.Breaks
import collection.immutable.Range.Inclusive
import sae.collections.HashMultiMap
import com.sun.corba.se.spi.servicecontext.ServiceContextData


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
  val tailHeadAdjacencyList = HashMultimap.create[Vertex, Vertex]()
  // reversed TransitiveClosure saved as: Adjacency hashmultimap
  // Key = a Vertex   (head of the edge)
  // Value = all adjacent Vertexes to the key Vertex (tail of the edge)
  val headTailAdjacencyList = HashMultimap.create[Vertex, Vertex]()

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

  def removed(e : Edge) {

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
      edges.foreach(x => {
        tailHeadAdjacencyList.get(x._1).foreach((y: Vertex) => {
          if (tailHeadAdjacencyList.containsEntry(y, x._2)) {

            putBack = (x._1, x._2) :: putBack
          }
        })
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