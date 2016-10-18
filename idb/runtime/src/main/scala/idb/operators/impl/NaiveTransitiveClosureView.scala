/* License (BSD Style License):
 *  Copyright (c) 2009, 2011
 *  Software Technology Group
 *  Department of Computer Science
 *  Technische Universität Darmstadt
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  - Neither the name of the Software Technology Group or Technische
 *    Universität Darmstadt nor the names of its contributors may be used to
 *    endorse or promote products derived from this software without specific
 *    prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 */
package idb.operators.impl

import idb.{MaterializedView, Relation}
import idb.operators.TransitiveClosure
import idb.observer.{Observable, NotifyObservers, Observer}
import scala.collection.mutable.{HashSet, HashMap}
import scala.util.control.Breaks._


/**
 * Algorithm for:
 * incremental view maintenance for transitive closure of DAG's with no parallel edges
 * the algorithm is based on
 * Dong and Su Sigmod 2000 44-50
 *
 * @author Malte V
 * @author Ralf Mitschke
 */
class NaiveTransitiveClosureView[Edge, Vertex](val source: Relation[Edge],
                                          val getTail: Edge => Vertex,
                                          val getHead: Edge => Vertex,
										  override val isSet : Boolean)
    extends TransitiveClosure[Edge, Vertex]
    with Observer[Edge]
	with NotifyObservers[(Vertex,Vertex)]
	with MaterializedView[(Vertex,Vertex)]
{
    source addObserver this

    import com.google.common.collect._

    //The multimap does not store duplicate key-value pairs. Adding a new key-value pair equal to an existing key-value pair has no effect.
    //graph = HashSet[(Vertex,Vertex)] would be also possible
    //all not derived Edges = (Vertex,Vertex)
    val graph = HashMultimap.create[Vertex, Vertex]()


    //TransitiveClose saved as double adjacencyList
    //for fast access its stored in a hashmap
    val transitiveClosure = HashMap[Vertex, (HashSet[Vertex], HashSet[Vertex])]()
    // example: trainsitiveClosure = (v -> ({u,w} , x))
    //=> we have the edges:
    // (v,u)(v,w)
    // (v,x)


    override protected def resetInternal(): Unit = ???


    override protected def childObservers(o: Observable[_]): Seq[Observer[_]] = {
        if (o == source) {
            return List (this)
        }
        Nil
    }

    /**
     *
     * access in O(1)
     */
	def contains[U >: (Vertex,Vertex)] (element : U): Boolean = {
		if(!element.isInstanceOf[(Vertex,Vertex)])
			return false

		val v = element.asInstanceOf[(Vertex,Vertex)]

        if (transitiveClosure.contains (v._1)) {
            transitiveClosureGet (v._1)._1.contains (v._2)
        }
        else
        {
            false
        }
    }

	override  def count[T >: (Vertex, Vertex)](t: T): Int = {
        if (!t.isInstanceOf[(Vertex, Vertex)]) {
            return 0
        }
        val v = t.asInstanceOf[(Vertex, Vertex)]
        if (transitiveClosure.contains (v._1)) {
            //transitiveClosureGet (v._1)._1.count (_ == v._2)
            1
        }
        else
        {
            0
        }
    }

	override def size() = {
		throw new UnsupportedOperationException
	}

    def foreach[T](f: ((Vertex, Vertex)) => T) {
        transitiveClosure.foreach (x => {
            x._2._1.foreach (y => {
                f ((x._1, y))
            })
        })
    }

    def foreachWithCount[T](f: ((Vertex, Vertex), Int) => T) {
        transitiveClosure.foreach (x => {
            x._2._1.foreach (y => {
                f ((x._1, y), 1)
            })
        })
    }



    private def transitiveClosureGet(v: Vertex) = {
        transitiveClosure.getOrElse (v, throw new Error ())
    }


    def internal_add(edge: Edge): Seq[(Vertex,Vertex)] = {
        var added = Seq[(Vertex,Vertex)]()
        val adjacencyEdgesToTailVertex = transitiveClosure
            .getOrElseUpdate (getTail (edge), (HashSet[Vertex](), HashSet[Vertex]()))
        val adjacencyEdgesToHeadVertex = transitiveClosure
            .getOrElseUpdate (getHead (edge), (HashSet[Vertex](), HashSet[Vertex]()))
        if (graph.put (getTail (edge), getHead (edge))) {
            if (!adjacencyEdgesToTailVertex._1.contains (getHead (edge))) {
                adjacencyEdgesToTailVertex._1.add (getHead (edge))
                adjacencyEdgesToHeadVertex._2.add (getTail (edge))
                added = (getTail (edge), getHead (edge)) +: added
            }
        }
        //Step 1
        //O(n)
        adjacencyEdgesToTailVertex._2.foreach ((x: Vertex) => {
            // all edges with head x == tail(e)
            // the Vertex x has an outgoing Edge e = (x,endVertex(edge))
            val connectedVertices = transitiveClosure.getOrElse (x, throw new Error ())
            if (!connectedVertices._1.contains (getHead (edge))) {
                connectedVertices._1.add (getHead (edge))
                adjacencyEdgesToHeadVertex._2.add (x)
                added = (x, getHead (edge)) +: added
            }

        })
        //Step 2
        //O(n)
        adjacencyEdgesToHeadVertex._1.foreach ((x: Vertex) => {
            // all edges with tail x == head(e)
            // the Vertex x has an incoming Edge e = (tail(e),x)
            val connectedVertices = transitiveClosure.getOrElse (x, throw new Error ())
            if (!connectedVertices._2.contains (getTail (edge))) {
                connectedVertices._2.add (getTail (edge))
                adjacencyEdgesToTailVertex._1.add (x)
                //tailHeadAdjacencyList.put(startVertex(edge), x)
                // && headTailAdjacencyList.put(x, startVertex(edge)))
                added = (getTail (edge), x) +: added
            }


        })
        //Step 3
        //O(n^2)
        adjacencyEdgesToTailVertex._2.foreach ((x: Vertex) => {
            adjacencyEdgesToHeadVertex._1.foreach ((y: Vertex) => {
                val connectedVertices = transitiveClosure.getOrElse (x, throw new Error ())
                if (!connectedVertices._1.contains (y)) {
                    //all the vertices with e1 = (x,tail(e)) and e2 = (head(e), y)
                    //=> e'=(x,y) new edge in the transitive closure
                    connectedVertices._1.add (y)
                    transitiveClosure.getOrElse (y, throw new Error ())._2.add (x)
                    added = (x, y) +: added
                }

            })
        })
        added
    }

  override def added(edge: Edge) {
    val added = internal_add(edge)
    notify_addedAll(added)
  }

  override def addedAll(edges: Seq[Edge]) {
    val added = edges.foldLeft(Seq[(Vertex,Vertex)]())((seq,edge) => seq ++ internal_add(edge))
    notify_addedAll(added)
  }

	def internal_remove(e: Edge): Seq[(Vertex,Vertex)] = {
        graph.remove (getTail (e), getHead (e))

        //set of all paths that maybe go through e
        val edges = new HashSet[(Vertex, Vertex)]()
        edges.add ((getTail (e), getHead (e)))
        val adjacencyEdgesToTailVertex = transitiveClosure.getOrElse (getTail (e), throw new Error ())
        val adjacencyEdgesToHeadVertex = transitiveClosure.getOrElse (getHead (e), throw new Error ())
        // S_ab
        adjacencyEdgesToHeadVertex._2.foreach ((x: Vertex) => {
            if (!graph.containsEntry (x, getHead (e)))
                edges.add ((x, getHead (e)))
        })
        adjacencyEdgesToTailVertex._1.foreach ((x: Vertex) => {
            if (!graph.containsEntry (getTail (e), x))
                edges.add ((getTail (e), x))
        })

        //O(n^2)
        adjacencyEdgesToHeadVertex._1.foreach ((x: Vertex) => {
            adjacencyEdgesToTailVertex._2.foreach ((y: Vertex) => {
                if (!graph.containsEntry (y, x))
                    edges.add ((y, x))
            })
        })

        //remove all found edges
        //creation of "T_ab" (see paper)
        //we dont remove edges that are in Gnew
        adjacencyEdgesToTailVertex._2.remove (getHead (e))
        adjacencyEdgesToHeadVertex._1.remove (getTail (e))
        //headTailAdjacencyList.remove(endVertex(e), startVertex(e))
        //tailHeadAdjacencyList.remove(startVertex(e), endVertex(e))
        edges.foreach (x => {
            transitiveClosure.getOrElse (x._1, throw new Error ())._1.remove (x._2)
            transitiveClosure.getOrElse (x._2, throw new Error ())._2.remove (x._1)
        })

        // T_ab ∪ (T_ab ο T_ab) ∪ (T_ab ο T_ab ο T_ab)
        for (i <- 1 to 2) {
            var putBack = List[(Vertex, Vertex)]()
            edges.foreach (x => {
                breakable {
                    transitiveClosure.getOrElse (x._1, throw new Error ())._1.foreach ((y: Vertex) => {
                        // do we still have a path from x._1 to x._2 => reinsert
                        if (transitiveClosure.getOrElse (y, throw new Error ())._1.contains (x._2)) {
                            putBack = (x._1, x._2) :: putBack
                            break ()
                        }
                    })
                }
            })
            putBack.foreach (x => {
                transitiveClosure.getOrElse (x._1, throw new Error ())._1.add (x._2)
                transitiveClosure.getOrElse (x._2, throw new Error ())._2.add (x._1)
                edges.remove (x)
            })
        }
        // edges = TC_old - TC_new
        edges.toSeq
    }

  override def removed(edge: Edge) {
    val removed = internal_remove(edge)
    notify_removedAll(removed)
  }

  override def removedAll(edges: Seq[Edge]) {
    val removed = edges.foldLeft(Seq[(Vertex,Vertex)]())((seq,edge) => seq ++ internal_remove(edge))
    notify_removedAll(removed)
  }


  override def updated(oldV: Edge, newV: Edge) {
        //a direct update is not supported
        removed (oldV)
        added (newV)
    }
}
