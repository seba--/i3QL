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
package idb.operators.impl.opt

import collection.mutable
import idb.{MaterializedView, Relation}
import idb.operators.TransitiveClosure
import idb.observer.{Observer, Observable, NotifyObservers}
import idb.operators.impl.util.TransactionElementObserver

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
class TransactionalCyclicTransitiveClosureView[Edge, Vertex] (val source: Relation[Edge],
    val getTail: Edge => Vertex,
    val getHead: Edge => Vertex,
    override val isSet: Boolean
)
    extends TransitiveClosure[Edge, Vertex]
    with TransactionElementObserver[Edge]
    with NotifyObservers[(Vertex, Vertex)]
    with MaterializedView[(Vertex, Vertex)]
{
    source addObserver this

    private var adjacencyLists = mutable.HashMap.empty[Vertex, mutable.HashSet[Edge]]

    private var nonSCCDescendants = mutable.HashMap.empty[Vertex, mutable.HashSet[Vertex]]

    private var sccRepresentatives = mutable.HashMap.empty[Vertex, Vertex]


    override protected def resetInternal(): Unit = {
        clear()
    }

    override def clear () {
        adjacencyLists = mutable.HashMap.empty[Vertex, mutable.HashSet[Edge]]
        nonSCCDescendants = mutable.HashMap.empty[Vertex, mutable.HashSet[Vertex]]
        sccRepresentatives = mutable.HashMap.empty[Vertex, Vertex]
        super.clear ()
    }

    override def endTransaction () {
        var it: java.util.Iterator[Edge] = additions.iterator ()
        while (it.hasNext) {
            val next = it.next ()
            internal_added (next)
        }
        it = deletions.iterator ()
        while (it.hasNext) {
            val next = it.next ()
            internal_removed (next)
        }
        clear ()
    }

    private def descendants (v: Vertex): mutable.HashSet[Vertex] = {
        if (nonSCCDescendants.isDefinedAt (v))
            return nonSCCDescendants (v)
        if (sccRepresentatives.isDefinedAt (v))
            return nonSCCDescendants (sccRepresentatives (v))

        val descendants = mutable.HashSet.empty[Vertex]
        nonSCCDescendants (v) = descendants
        descendants
    }


    private def transitiveClosure: mutable.Map[Vertex, mutable.HashSet[Vertex]] =
        nonSCCDescendants

    override protected def childObservers (o: Observable[_]): Seq[Observer[_]] = {
        if (o == source) {
            return List (this)
        }
        Nil
    }

    /**
     *
     * access in O(1)
     */
    def isDefinedAt (v: (Vertex, Vertex)) = {
        if (transitiveClosure.contains (v._1)) {
            transitiveClosure (v._1).contains (v._2)
        }
        else
        {
            false
        }
    }

    def count[T >: (Vertex, Vertex)] (t: T): Int = {
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

    def foreach[T] (f: ((Vertex, Vertex)) => T) {
        /*for ((start, descendants) <- transitiveClosure)
        {
            for (end <- descendants) {
                f (start, end)
            }
        }*/
		throw new UnsupportedOperationException("Method foreach is not implemented for transactional operators.")
    }

    def foreachWithCount[T] (f: ((Vertex, Vertex), Int) => T) {
        for ((start, descendants) <- transitiveClosure)
        {
            for (end <- descendants) {
                f ((start, end), 1)
            }
        }
    }

    override def size: Int = {
        throw new UnsupportedOperationException
    }

    def contains[U >: (Vertex, Vertex)] (element: U): Boolean = {
        throw new UnsupportedOperationException
    }


    /**
     * create an SCC for the start vertex, by removing all entries of the SCC from transitive closure
     * and only putting one representative back.
     * The SCC has a list of descendants of the SCC and a list of the elements in the SCC.
     */
    private def createSCC (representative: Vertex) {
        if (sccRepresentatives.contains (representative))
            return
        val sccDescendants = descendants (representative)

        // deduce the elemens in the scc, but leave the current storage intact
        val sccElements =
            for (vertex <- sccDescendants
                 if (descendants (vertex).contains (representative))
            ) yield vertex

        // only change scc storage after knowing what is in the scc
        for (vertex <- sccElements) {
            sccRepresentatives.put (vertex, representative)
            transitiveClosure.remove (vertex)
        }


        nonSCCDescendants.put (representative, sccDescendants)
    }


    def addEdge (start: Vertex, edge: Edge) {
        val edges = adjacencyLists.getOrElse (start, {
            val empty = mutable.HashSet.empty[Edge]
            adjacencyLists.put (start, empty)
            empty
        })
        edges.add (edge)
    }

    def removeEdge (start: Vertex, edge: Edge) {
        val edges = adjacencyLists (start) // should always be present
        edges.remove (edge)
    }

    def addDescendant (start: Vertex, end: Vertex, descendants: mutable.HashSet[Vertex]) {
        if (descendants.contains (end))
            return

        // start is in an scc
        if (sccRepresentatives.isDefinedAt (start)) {
            // add (x,end) for all elements in the same scc
            for (descendant <- descendants
                 if isInSameSCC (start, descendant)
            )
            {
                notify_added (descendant, end)
            }
        }
        else
        {
            notify_added (start, end)
        }
        descendants.add (end)
    }


    def isInSCC (start: Vertex, end: Vertex): Boolean = {
        sccRepresentatives.isDefinedAt (start) && sccRepresentatives.isDefinedAt (end)
    }

    def internal_added (edge: Edge) {
        val edgeStart = getTail (edge)
        val edgeEnd = getHead (edge)

        val pathsOfEdgeStart = descendants (edgeStart)
        val pathsOfEdgeEnd = descendants (edgeEnd)


        //Step 4 // the new edge itself
        addEdge (edgeStart, edge)
        addDescendant (edgeStart, edgeEnd, pathsOfEdgeStart)

        //Step 1 && Step 3 (inlined) -- O(n^2)

        for ((start, descendants) <- transitiveClosure
             if descendants.contains (edgeStart))
        {
            // Step 1
            // all new paths constructed by adding the end vertex to the back of an existing path
            addDescendant (start, edgeEnd, descendants)

            for (end <- pathsOfEdgeEnd) {
                //Step 3
                // all new paths constructed by concatenating two paths via (start -> end)
                addDescendant (start, end, descendants)
            }
        }

        //Step 2
        // all new paths constructed by adding the start vertex to the beginning of an existing path
        //O(n)
        for (end <- pathsOfEdgeEnd)
        {
            addDescendant (edgeStart, end, pathsOfEdgeStart)
        }

        // check if a transitive closure was created, it has to contain edgeStart, because this was the beginning of
        // the new edge

        if (pathsOfEdgeStart.contains (edgeStart) && !sccRepresentatives.contains (edgeStart))
        {
            createSCC (edgeStart)
        }

    }

    def isInSameSCCAsRepresentative (vertex: Vertex, representative: Vertex): Boolean = {
        sccRepresentatives.get (vertex) == Some (representative)
    }


    def isInSameSCC (vertex: Vertex, other: Vertex): Boolean = {
        sccRepresentatives.get (vertex) == sccRepresentatives.get (other)
    }


    def computeDescendants (start: Vertex, sccRepresentative: Vertex, result: mutable.HashSet[Vertex]) {
        if (!isInSameSCCAsRepresentative (start, sccRepresentative)) {
            result ++= nonSCCDescendants (start)
        }
        else
        {
            val edgeList = adjacencyLists.get (start).getOrElse (return)
            for (edge <- edgeList)
            {
                val end = getHead (edge)
                if (result.add (end)) {
                    // compute descendants only for new elements
                    computeDescendants (end, sccRepresentative, result)
                }
            }
        }
    }

    /**
     * resolves the previous scc and creates a situation without scc if necessary.
     * returns all edges in the scc, i.e., all vertex combinations, to be used as suspicious edges
     */
    def resolveSCCRemoval (start: Vertex,
        end: Vertex,
        suspiciousEdges: mutable.Set[(Vertex, Vertex)]
    ): mutable.Set[(Vertex, Vertex)] = {
        val representative = sccRepresentatives (start)

        val oldDescendants = transitiveClosure (representative)

        // recompute the descendants for all elements in SCC
        // fill suspicious edges to contain all cycles in the SCC
        val oldSCCElements =
            for (descendant <- descendants (start)
                 if isInSameSCCAsRepresentative (descendant, representative)) yield
            {
                val newDescendants = mutable.HashSet.empty[Vertex]
                computeDescendants (descendant, representative, newDescendants)

                for (end <- oldDescendants)
                    suspiciousEdges.add (descendant, end)
                (descendant, newDescendants)
            }

        // recreate a situation where the elements are not in any SCC
        for ((vertex, descendants) <- oldSCCElements)
        {
            transitiveClosure.put (vertex, descendants)
            sccRepresentatives.remove (vertex)

            // remove any edged we have recomputed from suspicious
            for (end <- descendants)
                suspiciousEdges.remove (vertex, end)
        }

        // recreate one or more SCCs as necessary
        for ((vertex, descendants) <- oldSCCElements
             if descendants.contains (vertex))
        {
            createSCC (vertex)
        }

        suspiciousEdges
    }

    def internal_removed (edge: Edge) {
        val edgeStart = getTail (edge)
        val edgeEnd = getHead (edge)

        removeEdge (edgeStart, edge)

        //set of all paths that maybe go through e (S_ab -- S for suspicious -- from paper), together with the length
        var suspiciousEdges = mutable.Set[(Vertex, Vertex)]()

        val pathsOfEdgeEnd = descendants (edgeEnd)

        suspiciousEdges.add (edgeStart, edgeEnd)

        // mark all paths that from edgeStart to any end vertex reachable by edgeEnd
        for (end <- pathsOfEdgeEnd)
        {
            suspiciousEdges.add (edgeStart, end)
        }

        // mark all paths that reach the start and end vertex -- O(n^2)
        for ((start, descendants) <- transitiveClosure
             if descendants.contains (edgeStart) && descendants.contains (
                 edgeEnd)) // the latter must not always be true in the case of a back edge where the scc was removed
        // first
        {

            // mark all paths from any start vertex reaching edgeEnd and edgeStart
            suspiciousEdges.add (start, edgeEnd)

            // mark all paths from any start vertex reaching to any vertex reachable by edgeEnd
            for (end <- pathsOfEdgeEnd)
            {
                suspiciousEdges.add (start, end)
            }
        }


        if (isInSCC (edgeStart, edgeEnd)) {
            resolveSCCRemoval (edgeStart, edgeEnd, suspiciousEdges)
        }


        // filter suspicious edges that are not still in the original graph
        // and remove suspicious edges from transitive closure
        suspiciousEdges =
            for ((start, end) <- suspiciousEdges
                 if !adjacencyLists.isDefinedAt (start) ||
                     !adjacencyLists.get (start).get.exists (
                         edge => (getTail (edge) == start && getHead (edge) == end)
                     )
            ) yield
            {
                descendants (start).remove (end)
                (start, end)
            }


        // T_ab ∪ (T_ab ο T_ab) ∪ (T_ab ο T_ab ο T_ab)
        // alternative paths can be found by concatenating trusted paths once or twice
        for (i <- 1 to 2) {
            // iterate twice
            var trustedEdges = List[(Vertex, Vertex)]()

            for ((suspiciousStart, suspiciousEnd) <- suspiciousEdges;
                 middle <- descendants (suspiciousStart);
                 middleDescendants = descendants (middle)
                 if middleDescendants.contains (suspiciousEnd))
            {
                // do we still have a path from  suspiciousStart to suspiciousEnd => reinsert
                trustedEdges = (suspiciousStart, suspiciousEnd) :: trustedEdges
            }

            for ((start, end) <- trustedEdges)
            {
                descendants (start).add (end)
                suspiciousEdges.remove (start, end)
            }
        }

        // edges = TC_old - TC_new
        for ((start, end) <- suspiciousEdges)
        {
            notify_removed (start, end)
        }
    }

    def lazyInitialize () {
        // should not be required since we add all in one transaction
    }
}
