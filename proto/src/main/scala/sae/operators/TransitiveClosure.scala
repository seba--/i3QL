package sae.operators

import sae.{Observer, LazyView}
import util.control.Breaks


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
 * incremental view maintenance for transitive close of ACYCLIC graphs
 * the algorithm is based on
 * Dong and Su Sigmod 2000 44-50
 *
 */
class HashTransitiveClosure[Domain <: AnyRef, Vertex <: AnyRef](val source: LazyView[Domain],
                                                                val startVertex: Domain => Vertex,
                                                                val endVertex: Domain => Vertex) extends TransitiveClosure[Domain, Vertex] with Observer[Domain] {
  //import scala.collection.mutable.Map

  import com.google.common.collect._
  import scala.collection.JavaConversions._

  val graph = HashMultimap.create[Vertex, Vertex]()
  val startPoints = HashMultimap.create[Vertex, Vertex]()
  val endPoints = HashMultimap.create[Vertex, Vertex]()

  source addObserver this

  def lazyInitialize {}

  def lazy_foreach[T](f: ((Vertex, Vertex)) => T) = null

  def added(edge: Domain) {
    if (graph.put(startVertex(edge), endVertex(edge))) {

      if (startPoints.put(startVertex(edge), endVertex(edge)) &&
        endPoints.put(endVertex(edge), startVertex(edge)))
        element_added((startVertex(edge), endVertex(edge)))
    }
    //Step 1
    endPoints.get(startVertex(edge)).foreach((x: Vertex) => {
      // the Vertex x has an outgoing Edge e = (x,endVertex(edge))
      if (startPoints.put(x, endVertex(edge)) && endPoints.put(endVertex(edge), x))
        element_added((x, endVertex(edge)))
    })
    //Step 2
    startPoints.get(endVertex(edge)).foreach((x: Vertex) => {
      // the Vertex x has an incoming Edge e = (startVertex(edge),x)
      if (startPoints.put(startVertex(edge), x) && endPoints.put(x, startVertex(edge))) //if (startPoints.put(startVertex(edge), x) && endPoints.put(endVertex(edge), x))
        element_added((startVertex(edge), x))
    })
    //Step 3
    endPoints.get(startVertex(edge)).foreach((x: Vertex) => {
      startPoints.get(endVertex(edge)).foreach((y: Vertex) => {
        if (startPoints.put(x, y) && endPoints.put(y, x))
          element_added((x, y))
      })
    })
  }

  def removed(v: Domain) {
    //val mybreak = new Breaks
    //import mybreak.{break, breakable}
    graph.remove(startVertex(v), endVertex(v))
    //endPoints.remove(endVertex(v), startVertex(v))
    //startPoints.remove(startVertex(v), endVertex(v))
    import scala.collection.mutable.Set
    val list = Set[(Vertex, Vertex)]()
    list.add((startVertex(v), endVertex(v)))
    endPoints.get(endVertex(v)).foreach((x: Vertex) => {
      if (!graph.containsEntry(x, endVertex(v)))
        list.add((x, endVertex(v)))
    })
    startPoints.get(startVertex(v)).foreach((x: Vertex) => {
      if (!graph.containsEntry(startVertex(v), x))
        list.add((startVertex(v), x))
    })

    endPoints.get(startVertex(v)).foreach((x: Vertex) => {
      startPoints.get(endVertex(v)).foreach((y: Vertex) => {
        //println(x + " " + y)
        list.add((x, y))
      })
    })
    endPoints.remove(endVertex(v), startVertex(v))
    startPoints.remove(startVertex(v), endVertex(v))
    list.foreach(x => {
      endPoints.remove(x._2, x._1)
      startPoints.remove(x._1, x._2)
    })
    var putBack = List[(Vertex, Vertex)]()
    list.foreach(x => {
      startPoints.get(x._1).foreach((y: Vertex) => {
        if (startPoints.containsEntry(y, x._2)) {
          //println(x._1 + " " + y + " " + x._2)
          putBack = (x._1, x._2) :: putBack
        }
      })
    })
    putBack.foreach(x => {
      endPoints.put(x._2, x._1)
      startPoints.put(x._1, x._2)
      list.remove(x)
    })
    putBack = List[(Vertex, Vertex)]()
    list.foreach(x => {
      startPoints.get(x._1).foreach((y: Vertex) => {
        if (startPoints.containsEntry(y, x._2)) {
          //println(x._1 + " " + y + " " + x._2)
          putBack = (x._1, x._2) :: putBack
        }
      })
    })
    putBack.foreach(x => {
      endPoints.put(x._2, x._1)
      startPoints.put(x._1, x._2)
      list.remove(x)
    })
    list.foreach(x => {
      //if (!startPoints.containsEntry(x._1, x._2))
        element_removed(x._1, x._2)
    })

  }

  def updated(oldV: Domain, newV: Domain) {
    //a direct update is not supported
    removed(oldV)
    added(newV)
  }
}