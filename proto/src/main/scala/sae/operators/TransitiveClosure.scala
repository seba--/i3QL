package sae.operators

import util.control.Breaks
import collection.mutable.{HashSet, HashMap}
import java.lang.Error
import sae.{MaterializedView, Observer, LazyView}
import javax.management.remote.rmi._RMIConnection_Stub

/**
 *
 * @author Malte V
 */

trait TransitiveClosure[Edge <: AnyRef, Vertex <: AnyRef] extends MaterializedView[(Vertex, Vertex)] {
  val source: LazyView[Edge]
  // naming after  "Network Flows: Theory, Algorithms, and Applications"
  val getTail: Edge => Vertex
  val getHead: Edge => Vertex
}

/**
 * Algorithm for:
 * incremental view maintenance for transitive closure of ACYCLIC graphs
 * the algorithm is based on
 * Dong and Su Sigmod 2000 44-50
 *
 */
class HashTransitiveClosure[Edge <: AnyRef, Vertex <: AnyRef](val source: LazyView[Edge],
                                                              val getTail: Edge => Vertex,
                                                              val getHead: Edge => Vertex) extends TransitiveClosure[Edge, Vertex] with Observer[Edge] {
  //import scala.collection.mutable.Map

  import com.google.common.collect._
  private var internal_size: Int = 0
  //all not derived Edges = (Vertex,Vertex)
  val graph = HashMultimap.create[Vertex, Vertex]()


  //TransitiveClose saved as double adjacencyList
  //for fast access its stored in a hashmap
  val transitiveClosure = HashMap[Vertex, (HashSet[Vertex], HashSet[Vertex])]()

  source addObserver this


  protected def materialized_contains(v: (Vertex, Vertex)) = {
    if (transitiveClosure.contains(v._1)) {
      transitiveClosureGet(v._1)._1.contains(v._2)
    } else {
      false
    }
  }

  protected def materialized_singletonValue = {
    if (transitiveClosure.size == 1) {
      if (transitiveClosure.head._2._2.size == 1) {
        Some((transitiveClosure.head._1, transitiveClosure.head._2._2.head))
      }
    }
    None
  }

  protected def materialized_size = {
    internal_size
  }



  private def materialized_size_++ {
    internal_size += 1

  }

  private def materialized_size_-- {
    internal_size -= 1
  }


  protected def materialized_foreach[T](f: ((Vertex, Vertex)) => T) {
    transitiveClosure.foreach(x => {
      x._2._1.foreach(y => {
        f((x._1, y))
      })
    })
  }

  def lazyInitialize {

      source.lazy_foreach(x =>
      internal_add(x, false))

    initialized = true


  }


  private def transitiveClosureGet(v: Vertex) = {
    transitiveClosure.getOrElse(v, throw new Error())
  }

  def internal_add(edge: Edge, notify: Boolean) {
    val adjacencyEdgesToStartVertex = transitiveClosure.getOrElseUpdate(getTail(edge), (HashSet[Vertex](), HashSet[Vertex]()))
    val adjacencyEdgesToEndVertex = transitiveClosure.getOrElseUpdate(getHead(edge), (HashSet[Vertex](), HashSet[Vertex]()))
    if (graph.put(getTail(edge), getHead(edge))) {
      if (!adjacencyEdgesToStartVertex._1.contains(getHead(edge))) {
        adjacencyEdgesToStartVertex._1.add(getHead(edge))
        adjacencyEdgesToEndVertex._2.add(getTail(edge))
        if(notify)element_added((getTail(edge), getHead(edge)))
        materialized_size_++
      }
    }
    //Step 1
    adjacencyEdgesToStartVertex._2.foreach((x: Vertex) => {
      //alle kanten die am startpunkt von e enden
      // the Vertex x has an outgoing Edge e = (x,endVertex(edge))
      val tmp = transitiveClosure.getOrElse(x, throw new Error())
      if (!tmp._1.contains(getHead(edge))) {
        tmp._1.add(getHead(edge))
        adjacencyEdgesToEndVertex._2.add(x)
        if(notify)element_added((x, getHead(edge)))
        materialized_size_++
      }

    })
    //Step 2
    adjacencyEdgesToEndVertex._1.foreach((x: Vertex) => {
      //gibt jetzt auch startvertex(edge) -> x
      val tmp = transitiveClosure.getOrElse(x, throw new Error())
      if (!tmp._2.contains(getTail(edge))) {
        tmp._2.add(getTail(edge))
        adjacencyEdgesToStartVertex._1.add(x)
        //tailHeadAdjacencyList.put(startVertex(edge), x)
        // && headTailAdjacencyList.put(x, startVertex(edge)))
        if(notify)element_added((getTail(edge), x))
        materialized_size_++
      }


    })
    //Step 3
    adjacencyEdgesToStartVertex._2.foreach((x: Vertex) => {
      adjacencyEdgesToEndVertex._1.foreach((y: Vertex) => {
        val tmp = transitiveClosure.getOrElse(x, throw new Error())
        if (!tmp._1.contains(y)) {
          tmp._1.add(y)
          transitiveClosure.getOrElse(y, throw new Error())._2.add(x)
          //if (tailHeadAdjacencyList.put(x, y) && headTailAdjacencyList.put(y, x))
          if(notify)element_added((x, y))
          materialized_size_++
        }

      })
    })
  }

  def added(edge: Edge) {
    internal_add(edge, true)
  }

  def removed(e: Edge) {

    graph.remove(getTail(e), getHead(e))

    import scala.collection.mutable.Set
    //set of all paths that maybe go through e
    val edges = Set[(Vertex, Vertex)]()
    edges.add((getTail(e), getHead(e)))
    val adjacencyEdgesToStartVertex = transitiveClosure.getOrElse(getTail(e), throw new Error())
    val adjacencyEdgesToEndVertex = transitiveClosure.getOrElse(getHead(e), throw new Error())
    // S_ab
    adjacencyEdgesToEndVertex._2.foreach((x: Vertex) => {
      if (!graph.containsEntry(x, getHead(e)))
        edges.add((x, getHead(e)))
    })
    adjacencyEdgesToStartVertex._1.foreach((x: Vertex) => {
      if (!graph.containsEntry(getTail(e), x))
        edges.add((getTail(e), x))
    })

    adjacencyEdgesToEndVertex._1.foreach((x: Vertex) => {
      adjacencyEdgesToStartVertex._2.foreach((y: Vertex) => {
        //println(x + " " + y)
        if (!graph.containsEntry(y, x))
          edges.add((y, x))
      })
    })

    //remove all found edges
    //creation of "T_ab" (see paper)
    //we dont remove edges that are in Gnew
    adjacencyEdgesToStartVertex._2.remove(getHead(e))
    adjacencyEdgesToEndVertex._1.remove(getTail(e))
    //headTailAdjacencyList.remove(endVertex(e), startVertex(e))
    //tailHeadAdjacencyList.remove(startVertex(e), endVertex(e))
    edges.foreach(x => {
      transitiveClosure.getOrElse(x._1, throw new Error())._1.remove(x._2)
      transitiveClosure.getOrElse(x._2, throw new Error())._2.remove(x._1)
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
          transitiveClosure.getOrElse(x._1, throw new Error())._1.foreach((y: Vertex) => {

            //tailHeadAdjacencyList.get(x._1).foreach((y: Vertex) => {
            //if (tailHeadAdjacencyList.containsEntry(y, x._2)) {
            // do we still have a path from x._1 to x._2 => reinsert
            if (transitiveClosure.getOrElse(y, throw new Error())._1.contains(x._2)) {
              putBack = (x._1, x._2) :: putBack
              break
            }
          })
        }
      })
      putBack.foreach(x => {
        transitiveClosure.getOrElse(x._1, throw new Error())._1.add(x._2)
        transitiveClosure.getOrElse(x._2, throw new Error())._2.add(x._1)
        edges.remove(x)
      })
    }
    // edges = TC_old - TC_new
    edges.foreach(x => {
      element_removed(x._1, x._2)
      materialized_size_--
    })

  }

  def updated(oldV: Edge, newV: Edge) {
    //a direct update is not supported
    removed(oldV)
    added(newV)
  }
}
