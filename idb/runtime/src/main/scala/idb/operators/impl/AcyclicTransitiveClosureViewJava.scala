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


import scala.collection.JavaConversions._
import idb.Relation
import idb.operators.TransitiveClosure
import idb.observer.{Observable, NotifyObservers, Observer}

/**
 * Algorithm for:
 * incremental view maintenance for transitive closure of DAG's with no parallel edges
 * the algorithm is based on
 * Dong and Su Sigmod 2000 44-50
 *
 * The head of an edge is denoting the arrow head, hence the end vertex
 * The tail of an edge is denoting the start vertex
 *
 * @author Ralf Mitschke
 * @author Malte V
 */
class AcyclicTransitiveClosureViewJava[Edge, Vertex](val source: Relation[Edge],
                                                     val getTail: Edge => Vertex,
                                                     val getHead: Edge => Vertex,
													  override val isSet : Boolean)
    extends TransitiveClosure[Edge, Vertex]
    with Observer[Edge]
	with NotifyObservers[(Vertex,Vertex)]
{
    source addObserver this


    private case class Paths(descendants: java.util.HashSet[Vertex], ancestors: java.util.HashSet[Vertex])
    {
        def this() = {
            this (new java.util.HashSet[Vertex](), new java.util.HashSet[Vertex]())
        }
    }

    //TransitiveClose saved as double adjacencyList
    //for fast access its stored in a hashmap
    private val transitiveClosure = new java.util.HashMap[Vertex, Paths]()
    // example: trainsitiveClosure = (v -> ({u,w} , x))
    //=> we have the edges:
    // (u,v)(w, v)
    // (v, x)

    lazyInitialize ()

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
    def isDefinedAt(v: (Vertex, Vertex)) = {
        if (transitiveClosure.containsKey (v._1)) {
            transitiveClosureGet (v._1).ancestors.contains (v._2)
        }
        else
        {
            false
        }
    }

    def elementCountAt[T >: (Vertex, Vertex)](t: T): Int = {
        if (!t.isInstanceOf[(Vertex, Vertex)]) {
            return 0
        }
        val v = t.asInstanceOf[(Vertex, Vertex)]
        if (transitiveClosure.containsKey (v._1)) {
            1
        }
        else
        {
            0
        }
    }


    def foreach[T](f: ((Vertex, Vertex)) => T) {
        transitiveClosure.foreach (x => {
            x._2.descendants.foreach (y => {
                f ((x._1, y))
            })
        })
        /*
        val it = transitiveClosure.keySet().iterator()
        while (it.hasNext) {
            val x = it.next()
            val descendants = transitiveClosure.get(x)
            descendants.foreach (y => {
                f ((x._1, y))
            })
        }
        */
    }

    def foreachWithCount[T](f: ((Vertex, Vertex), Int) => T) {
        transitiveClosure.foreach (x => {
            x._2.descendants.foreach (y => {
                f ((x._1, y), 1)
            })
        })
    }


    def lazyInitialize() {
        source.foreach (
            x => internal_add (x)
        )
    }


    private def transitiveClosureGet(v: Vertex) = {
        transitiveClosure.get (v)
    }

    private def transitiveClosureGetOrCreate(v: Vertex) = {
        if (!transitiveClosure.containsKey (v)) {
            transitiveClosure.put (v, new Paths ())
        }
        transitiveClosure.get (v)
    }

    def internal_add(edge: Edge): Seq[(Vertex,Vertex)] = {
        var added = Seq[(Vertex,Vertex)]()

        val head = getHead (edge)
        val tail = getTail (edge)

        val pathsOfHeadVertex = transitiveClosureGetOrCreate (head)

        val pathsOfTailVertex = transitiveClosureGetOrCreate (tail)

        // head -> ({}, {tail})
        // (head, tail)
        pathsOfHeadVertex.ancestors.add (tail)
        // tail -> ({head}, {})
        // (head, tail)
        pathsOfTailVertex.descendants.add (head)

        //Step 4 // the new edge itself
        added = ((tail, head)) +: added


        //Step 1 // all new paths constructed by adding the new edge to the back of an existing path
        //O(n)
        pathsOfTailVertex.ancestors.foreach ((x: Vertex) => {
            // all edges with head x == tail(e)
            // the Vertex x has an outgoing Edge e = (x,endVertex(edge))
            val connectedVertices = transitiveClosureGet (x)
            if (!connectedVertices.descendants.contains (head)) {
                connectedVertices.descendants.add (head)
                pathsOfHeadVertex.ancestors.add (x)
                added = ((x, head)) +: added
            }

        })
        //Step 2
        //O(n)
        pathsOfHeadVertex.descendants.foreach ((x: Vertex) => {
            // all edges with tail x == head(e)
            // the Vertex x has an incoming Edge e = (tail(e),x)
            val connectedVertices = transitiveClosureGet (x)
            if (!connectedVertices.ancestors.contains (tail)) {
                connectedVertices.ancestors.add (tail)
                pathsOfTailVertex.descendants.add (x)
                //tailHeadAdjacencyList.put(startVertex(edge), x)
                // && headTailAdjacencyList.put(x, startVertex(edge)))
              added = ((tail, x)) +: added
            }


        })
        //Step 3
        //O(n^2)
        pathsOfTailVertex.ancestors.foreach ((x: Vertex) => {
            pathsOfHeadVertex.descendants.foreach ((y: Vertex) => {
                val connectedVertices = transitiveClosureGet (x)
                if (!connectedVertices.descendants.contains (y)) {
                    //all the vertices with e1 = (x,tail(e)) and e2 = (head(e), y)
                    //=> e'=(x,y) new edge in the transitive closure
                    connectedVertices.descendants.add (y)
                    transitiveClosureGet (y).ancestors.add (x)
                  added = ((x, y)) +: added
                }

            })
        })

      added
    }

  def added(edge: Edge) {
    val added = internal_add (edge)
    notify_addedAll(added)
  }

  def addedAll(edges: Seq[Edge]) {
    val added = edges.foldLeft(Seq[(Vertex,Vertex)]())((seq, e) => seq ++ internal_add(e))
    notify_addedAll(added)
  }

  def removed(edge: Edge) {
    val removed = internal_remove (edge)
    notify_removedAll(removed)
  }

  def removedAll(edges: Seq[Edge]) {
    val removed = edges.foldLeft(Seq[(Vertex,Vertex)]())((seq, e) => seq ++ internal_remove(e))
    notify_removedAll(removed)
  }



  def internal_remove(edge: Edge): Seq[(Vertex,Vertex)] = {

        val head = getHead (edge)
        val tail = getTail (edge)


        val pathsOfTailVertex = transitiveClosureGet (tail)
        val pathsOfHeadVertex = transitiveClosureGet (head)


        //set of all paths that maybe go through e (S_ab -- S for suspicious -- from paper)
        val suspiciousEdges = new java.util.HashSet[(Vertex, Vertex)]()

        suspiciousEdges.add (tail, head)

        //
        pathsOfTailVertex.ancestors.foreach ((x: Vertex) => {
            suspiciousEdges.add ((x, head))
        })
        pathsOfHeadVertex.descendants.foreach ((y: Vertex) => {
            suspiciousEdges.add ((tail, y))
        })

        //O(n^2)
        pathsOfTailVertex.ancestors.foreach ((x: Vertex) => {
            pathsOfHeadVertex.descendants.foreach ((y: Vertex) => {
                suspiciousEdges.add ((x, y))
            })
        })

        suspiciousEdges.foreach (x => {
            val descendants = transitiveClosureGet (x._1).descendants
            descendants.remove (x._2)
            val ancestors = transitiveClosureGet (x._2).ancestors
            ancestors.remove (x._1)
        })



        // T_ab ∪ (T_ab ο T_ab) ∪ (T_ab ο T_ab ο T_ab)
        // THIS IS A DIFFERENT THING

        // find an alternative path => reinsert
        var putBack = List[(Vertex, Vertex)]()
        suspiciousEdges.foreach (
            e => {
                val x = e._1
                val y = e._2
                val descendantsOfX = transitiveClosureGet (x).descendants
                val ancestorsOfY = transitiveClosureGet (y).ancestors
                // if the intersection of ancestors of y and descendants of x is not empty we have a different path
                if (descendantsOfX.exists (v => ancestorsOfY.contains (v))) {
                    putBack = (x, y) :: putBack
                }
            }
        )
        putBack.foreach (
            e => {
                val x = e._1
                val y = e._2
                transitiveClosureGet (x).descendants.add (y)
                transitiveClosureGet (y).ancestors.add (x)
                suspiciousEdges.remove (e)
            }
        )

        // edges = TC_old - TC_new
        Seq() ++ suspiciousEdges
    }

    def updated(oldV: Edge, newV: Edge) {
        //a direct update is not supported
        removed (oldV)
        added (newV)
    }

    override def resetInternal(): Unit = ???

	override def endTransaction() {
		notify_endTransaction()
	}
}
