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
package sae.operators.impl

import sae.{Observable, Observer, Relation}
import sae.operators.TransitiveClosure
import collection.mutable
import sae.deltas._

/**
 *
 * An efficient algorithm for incremental data flow analysis  -- Thomas J. Marlowe

 *
 * The head of an edge is denoting the arrow head, hence the end vertex
 * The tail of an edge is denoting the start vertex
 *
 * The idea behind cycles is to pick a representative vertex for strongly connected components.
 * All vertices in the strongly connected components share the same paths, hence must be updated only once.
 *
 * @author Ralf Mitschke
 */
class CyclicTransitiveClosureView[Edge, Vertex](val source: Relation[Edge],
                                                val getTail: Edge => Vertex,
                                                val getHead: Edge => Vertex)
    extends TransitiveClosure[Edge, Vertex]
    with Observer[Edge]
{
    source addObserver this

    private trait Descendants
    {
        def descendants: mutable.Map[Vertex, Int]

        def add(end: Vertex, length: Int): Boolean = {
            val oldLength = descendants.get (end)
            if (!oldLength.isDefined || oldLength.get > length) {
                descendants.put (end, length)
                return true
            }
            false
        }

        def remove(end: Vertex) = descendants.remove (end)

        def contains(end: Vertex) = descendants.contains (end)

        def apply(end: Vertex) = descendants (end)

        def isSCC: Boolean
    }

    private object Descendants
    {

        def apply(): Descendants = NonSCCPredecessors (mutable.HashMap.empty[Vertex, Int])

    }

    private case class NonSCCPredecessors(descendants: mutable.HashMap[Vertex, Int])
        extends Descendants
    {
        def isSCC: Boolean = false
    }

    private case class SCCPredecessors(descendants: mutable.HashMap[Vertex, Int])
        extends Descendants
    {
        def isSCC: Boolean = true
    }

    /*
    private case class Paths(descendants: HashSet[Vertex], ancestors: HashSet[Vertex], isSCC : Boolean = false)
    {
        def this() = {
            this (HashSet[Vertex](), HashSet[Vertex]())
        }
    }
    */

    //TransitiveClose saved as double adjacencyList
    //for fast access its stored in a hash map
    private val transitiveClosure = mutable.HashMap[Vertex, Descendants]()

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
        if (transitiveClosure.contains (v._1)) {
            transitiveClosureGet (v._1).descendants.contains (v._2)
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
        if (transitiveClosure.contains (v._1)) {
            1
        }
        else
        {
            0
        }
    }

    def foreach[T](f: ((Vertex, Vertex)) => T) {
        transitiveClosure.foreach (x => {
            x._2.descendants.keys.foreach (y => {
                f ((x._1, y))
            })
        })
    }

    def foreachWithCount[T](f: ((Vertex, Vertex), Int) => T) {
        transitiveClosure.foreach (x => {
            x._2.descendants.keys.foreach (y => {
                f ((x._1, y), 1)
            })
        })
    }


    def lazyInitialize() {
        source.foreach (
            x => internal_add (x, notify = false)
        )
    }


    private def transitiveClosureGet(v: Vertex) = {
        transitiveClosure.getOrElse (v, throw new Error ())
    }

    /*
        private def addToSCC(edge: Edge) {
            val head = getHead (edge)
            val tail = getTail (edge)
        }

        private def createSCC(paths: Paths) {
            val scc = paths.ancestors.union (paths.descendants)
            val entry = new Paths (scc, scc, true)
            scc.foreach (
                transitiveClosure (_) = entry
            )

        }

        private def isInSCC(v: Vertex): Boolean = {
            transitiveClosure.getOrElse (v, return false).isSCC
        }

    */

    def internal_add(edge: Edge, notify: Boolean) {
        val edgeStart = getTail (edge)
        val edgeEnd = getHead (edge)

        val pathsOfEdgeStart = transitiveClosure
            .getOrElseUpdate (edgeStart, Descendants ())
        val pathsOfEdgeEnd = transitiveClosure
            .getOrElseUpdate (edgeEnd, Descendants ())



        /*
        // check for SCC
        if (pathsOfTailVertex.ancestors.contains (head))
        {
            createSCC (pathsOfTailVertex)
        }
        */


        // head -> ({}, {tail})
        // (head, tail)
        //pathsOfHeadVertex.ancestors.add (tail)
        // tail -> ({head}, {})
        // (head, tail)

        //Step 4 // the new edge itself
        if (pathsOfEdgeStart.add (edgeEnd, 1) && notify) element_added ((edgeStart, edgeEnd))

        //Step 1 && Step 3 (inlined) -- O(n^2)

        for ((start, descendants) <- transitiveClosure
             if descendants.contains (edgeStart))
        {
            // Step 1
            // all new paths constructed by adding the end vertex to the back of an existing path
            if (descendants.add (edgeEnd, descendants (edgeStart) + 1) && notify)
                element_added ((start, edgeEnd))

            for ((end, length) <- pathsOfEdgeEnd.descendants) {
                //Step 3
                // all new paths constructed by concatenating two paths via (start -> end)
                if (descendants.add (end, descendants (edgeStart) + length + 1) && notify)
                    element_added ((start, end))
            }
        }

        //Step 2
        // all new paths constructed by adding the start vertex to the beginning of an existing path
        //O(n)
        for ((end, length) <- pathsOfEdgeEnd.descendants
             if !pathsOfEdgeStart.contains (end))
        {
            if (pathsOfEdgeStart.add (end, length + 1) && notify)
                element_added ((edgeStart, end))
        }
    }

    def added(edge: Edge) {
        internal_add (edge, notify = true)
    }

    def removed(edge: Edge) {
        val edgeStart = getTail (edge)
        val edgeEnd = getHead (edge)

        //val pathsOfEdgeStart = transitiveClosure (edgeStart)
        val pathsOfEdgeEnd = transitiveClosure (edgeEnd)


        //set of all paths that maybe go through e (S_ab -- S for suspicious -- from paper), together with the length
        var suspiciousEdges = mutable.Map[(Vertex, Vertex), Int]()
        suspiciousEdges.put ((edgeStart, edgeEnd), 1)

        // mark all paths that from edgeStart to any end vertex reachable by edgeEnd
        for ((end, length) <- pathsOfEdgeEnd.descendants)
        {
            suspiciousEdges.put ((edgeStart, end), length + 1)
        }

        // mark all paths that reach the start and end vertex -- O(n^2)
        for ((start, descendants) <- transitiveClosure
             if descendants.contains (edgeStart)) //&& descendants.contains (edgeEnd)) // the latter should be true in any case
        {

            // mark all paths from any start vertex reaching edgeEnd and edgeStart
            suspiciousEdges.put ((start, edgeEnd), descendants (edgeStart) + 1)

            // mark all paths from any start vertex reaching to any vertex reachable by edgeEnd
            for ((end, length) <- pathsOfEdgeEnd.descendants)
            {
                suspiciousEdges.put ((start, end), descendants (edgeStart) + length + 1)
            }
        }


        // filter suspicious edges to not have smaller paths and
        // remove all suspicious edges where the edge is also the shortest path
        suspiciousEdges =
            for (((start, end), length) <- suspiciousEdges;
                 descendants = transitiveClosure (start).descendants
                 if length == descendants (end)) yield
            {
                descendants.remove (end)
                ((start, end), length)
            }


        // T_ab ∪ (T_ab ο T_ab) ∪ (T_ab ο T_ab ο T_ab)
        // alternative paths can be found by concatenating trusted paths once or twice
        for (i <- 1 to 2) {
            // iterate twice
            var trustedEdges = List[(Vertex, Vertex, Int)]()

            for (((suspiciousStart, suspiciousEnd), length) <- suspiciousEdges;
                 (middle, middleLength) <- transitiveClosure (suspiciousStart).descendants;
                 middleDescendants = transitiveClosure (middle).descendants
                 if middleDescendants.contains (suspiciousEnd))
            {
                // do we still have a path from  suspiciousStart to suspiciousEnd => reinsert
                trustedEdges = (suspiciousStart, suspiciousEnd, middleLength + middleDescendants (suspiciousEnd)) :: trustedEdges
            }

            for ((start, end, length) <- trustedEdges)
            {
                transitiveClosure (start).add (end, length)
                suspiciousEdges.remove (start, end)
            }
        }

        // edges = TC_old - TC_new
        for ((start, end) <- suspiciousEdges.keys)
        {
            element_removed (start, end)
        }
    }


    def updated (oldV: Edge, newV: Edge)
    {
        //a direct update is not supported
        removed (oldV)
        added (newV)
    }

    def updated[U <: Edge] (update: Update[U])
    {
        throw new UnsupportedOperationException
    }

    def modified[U <: Edge] (additions: Set[Addition[U]], deletions: Set[Deletion[U]], updates: Set[Update[U]])
    {
        throw new UnsupportedOperationException
    }
}
