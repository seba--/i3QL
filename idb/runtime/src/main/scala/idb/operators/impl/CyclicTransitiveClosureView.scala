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

import collection.mutable
import idb.{Index, MaterializedView, Relation}
import idb.operators.TransitiveClosure
import idb.observer.{Observer, Observable, NotifyObservers}

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
												val index: Index[Vertex, Edge], //index = adjacency lists
												val getTail: Edge => Vertex,
												val getHead: Edge => Vertex,
												override val isSet: Boolean)
	extends TransitiveClosure[Edge, Vertex]
	with Observer[Edge]
	with NotifyObservers[(Vertex, Vertex)]
	with MaterializedView[(Vertex, Vertex)] {
	source addObserver this


	private val nonSCCDescendants = mutable.HashMap[Vertex, mutable.HashSet[Vertex]]()

	private val nonSCCPredecessors = mutable.HashMap[Vertex, mutable.HashSet[Vertex]]()

	private val sccRepresentatives = mutable.HashMap[Vertex, Vertex]()


	private def descendants(v: Vertex): mutable.HashSet[Vertex] = {
		if (nonSCCDescendants.isDefinedAt(v))
			return nonSCCDescendants(v)
		if (sccRepresentatives.isDefinedAt(v))
			return nonSCCDescendants(sccRepresentatives(v))

		val descendants = mutable.HashSet.empty[Vertex]
		nonSCCDescendants(v) = descendants
		descendants
	}


	private def predecessors(v: Vertex): mutable.HashSet[Vertex] = {
		if (nonSCCPredecessors.isDefinedAt(v))
			return nonSCCPredecessors(v)
		if (sccRepresentatives.isDefinedAt(v))
			return nonSCCPredecessors(sccRepresentatives(v))

		val predecessors = mutable.HashSet.empty[Vertex]
		nonSCCPredecessors(v) = predecessors
		predecessors
	}


	private def transitiveClosure: mutable.Map[Vertex, mutable.HashSet[Vertex]] =
		nonSCCDescendants

	override protected def childObservers(o: Observable[_]): Seq[Observer[_]] = {
		if (o == source) {
			return List(this)
		}
		Nil
	}

	override protected def resetInternal(): Unit = ???


	/**
	 *
	 * access in O(1)
	 */
	def contains[U >: (Vertex, Vertex)](element: U): Boolean =

		if (element.isInstanceOf[(Vertex, Vertex)]) {

			val v = element.asInstanceOf[(Vertex, Vertex)]


			if (transitiveClosure.contains(v._1)) {
				transitiveClosure(v._1).contains(v._2)
			}
			else {
				false
			}
		} else {
			false
		}


	def count[T >: (Vertex, Vertex)](t: T): Int = {
		if (!t.isInstanceOf[(Vertex, Vertex)]) {
			return 0
		}
		val v = t.asInstanceOf[(Vertex, Vertex)]
		if (transitiveClosure.contains(v._1)) {
			1
		}
		else {
			0
		}
	}

	override def foreach[T](f: ((Vertex, Vertex)) => T) {
		for ((start, descendants) <- transitiveClosure) {
			for (end <- descendants) {
				f(start, end)
			}
		}
	}

	def foreachWithCount[T](f: ((Vertex, Vertex), Int) => T) {
		for ((start, descendants) <- transitiveClosure) {
			for (end <- descendants) {
				f((start, end), 1)
			}
		}
	}



	/**
	 * create an SCC for the start vertex, by removing all entries of the SCC from transitive closure
	 * and only putting one representative back.
	 * The SCC has a list of descendants of the SCC and a list of the elements in the SCC.
	 */
	private def createSCC(representative: Vertex) {
		if (sccRepresentatives.contains(representative))
			return
		val sccDescendants = descendants(representative)

		// deduce the elements in the scc, but leave the current storage intact
		val sccElements =
			for (vertex <- sccDescendants
				 if (descendants(vertex).contains(representative))
			) yield vertex

		// only change scc storage after knowing what is in the scc
		for (vertex <- sccElements) {
			sccRepresentatives.put(vertex, representative)
			transitiveClosure.remove(vertex)
		}


		nonSCCDescendants.put(representative, sccDescendants)
	}

	private def updateAdjacencyLists(start: Vertex, end: Vertex) {
		val descendantsOfStart = descendants(start)
		val predecessorsOfEnd = predecessors(end)

		descendantsOfStart.add(end)
		predecessorsOfEnd.add(start)
	}

	private def computeResultAdditions(start: Vertex, end: Vertex): Seq[(Vertex,Vertex)] = {
		if (descendants(start).contains(end))
			return Seq()

    var added = Seq[(Vertex,Vertex)]()

		// start is in an scc
		if (sccRepresentatives.isDefinedAt(start)) {
			// add (x,end) for all elements in the same scc
			for (descendant <- descendants(start) if isInSameSCC(start, descendant)) {
				added = (descendant, end) +: added
			}
		}
		else {
			added = (start, end) +: added
		}

    added
	}


	private def isInSCC(start: Vertex, end: Vertex): Boolean = {
		sccRepresentatives.isDefinedAt(start) && sccRepresentatives.isDefinedAt(end)
	}


	/**
	 * Computes the additions that result from adding this edge.
	 * Note there is a general protocol followed throughout this methods of:
	 * 1. computeAdditions
	 * 2. updateAdjacencyLists
	 * This order is important, since the computation of additions must know whether an element is already in the adjacency lists
	 */
	private def computeAdditions(edge: Edge): Seq[(Vertex,Vertex)] = {
    var added = Seq[(Vertex,Vertex)]()

		val edgeStart = getTail(edge)
		val edgeEnd = getHead(edge)
        if(edgeEnd == null || edgeStart == null) {
            added = (edgeStart, edgeEnd) +: added
            return added
        }


		val descendantsOfEnd = descendants(edgeEnd)

		val predecessorsOfStart = predecessors(edgeStart)

		//Step 4 // the new edge itself
		computeResultAdditions(edgeStart, edgeEnd)
		updateAdjacencyLists(edgeStart, edgeEnd)


		//Step 1 && Step 3 (inlined) -- O(n^2)

		for (start <- predecessorsOfStart) {
			// Step 1
			// all new paths constructed by adding the end vertex to the back of an existing path
			computeResultAdditions(start, edgeEnd)
			updateAdjacencyLists(start, edgeEnd)

			for (end <- descendantsOfEnd) {
				//Step 3
				// all new paths constructed by concatenating two paths via (start -> end)
				computeResultAdditions(start, end)
				updateAdjacencyLists(start, end)
			}
		}

		//Step 2
		// all new paths constructed by adding the start vertex to the beginning of an existing path
		//O(n)
		for (end <- descendantsOfEnd) {
			computeResultAdditions(edgeStart, end)
			updateAdjacencyLists(edgeStart, end)
		}

		// check if a transitive closure was created, it has to contain edgeStart, because this was the beginning of the new edge

		if (descendants(edgeStart).contains(edgeStart) && !sccRepresentatives.contains(edgeStart)) {
			createSCC(edgeStart)
		}

    added
	}

	def added(edge: Edge) {
		//println (edge)
		val added = computeAdditions(edge)
    notify_addedAll(added)
	}

  def addedAll(edges: Seq[Edge]) {
    val added = edges.foldLeft(Seq[(Vertex,Vertex)]())((seq, e) => seq ++ computeAdditions(e))
    notify_addedAll(added)
  }

  def removed(e: Edge): Unit = {
    val removed = computeRemovals(e)
    notify_removedAll(removed)
  }

  def removedAll(edges: Seq[Edge]) {
    val removed = edges.foldLeft(Seq[(Vertex,Vertex)]())((seq, e) => seq ++ computeRemovals(e))
    notify_removedAll(removed)
  }

	def isInSameSCCAsRepresentative(vertex: Vertex, representative: Vertex): Boolean = {
		sccRepresentatives.get(vertex) == Some(representative)
	}

	def isInSameSCC(vertex: Vertex, other: Vertex): Boolean = {
		sccRepresentatives.get(vertex) == sccRepresentatives.get(other)
	}

	def computeDescendants(start: Vertex, sccRepresentative: Vertex, result: mutable.HashSet[Vertex]) {
		if (!isInSameSCCAsRepresentative(start, sccRepresentative)) {
			result ++= nonSCCDescendants(start)
		}
		else {
			val edgeList = index.get(start).getOrElse(return)
			for (edge <- edgeList) {
				val end = getHead(edge)
				if (result.add(end)) {
					// compute descendants only for new elements
					computeDescendants(end, sccRepresentative, result)
				}
			}
		}
	}

	/**
	 * resolves the previous scc and creates a situation without scc if necessary.
	 * returns all edges in the scc, i.e., all vertex combinations, to be used as suspicious edges
	 */
	def resolveSCCRemoval(start: Vertex, end: Vertex, suspiciousEdges: mutable.Set[(Vertex, Vertex)]): mutable.Set[(Vertex, Vertex)] = {
		val representative = sccRepresentatives(start)

		val oldDescendants = transitiveClosure(representative)

		// recompute the descendants for all elements in SCC
		// fill suspicious edges to contain all cycles in the SCC
		val oldSCCElements =
			for (descendant <- descendants(start)
				 if isInSameSCCAsRepresentative(descendant, representative)) yield {
				val newDescendants = mutable.HashSet.empty[Vertex]
				computeDescendants(descendant, representative, newDescendants)

				for (end <- oldDescendants)
					suspiciousEdges.add(descendant, end)
				(descendant, newDescendants)
			}

		// recreate a situation where the elements are not in any SCC
		for ((vertex, descendants) <- oldSCCElements) {
			transitiveClosure.put(vertex, descendants)
			sccRepresentatives.remove(vertex)

			// remove any edged we have recomputed from suspicious
			for (end <- descendants)
				suspiciousEdges.remove(vertex, end)
		}

		// recreate one or more SCCs as necessary
		for ((vertex, descendants) <- oldSCCElements
			 if descendants.contains(vertex)) {
			createSCC(vertex)
		}

		suspiciousEdges
	}

	def computeRemovals(edge: Edge): Seq[(Vertex,Vertex)] = {
    var removed = Seq[(Vertex,Vertex)]()

		val edgeStart = getTail(edge)
		val edgeEnd = getHead(edge)

        if(edgeEnd == null || edgeStart == null) {
            removed = (edgeStart, edgeEnd) +: removed
            return removed
        }


        //set of all paths that maybe go through e (S_ab -- S for suspicious -- from paper), together with the length
		var suspiciousEdges = mutable.Set[(Vertex, Vertex)]()

		val pathsOfEdgeEnd = descendants(edgeEnd)

		suspiciousEdges.add(edgeStart, edgeEnd)

		// mark all paths that from edgeStart to any end vertex reachable by edgeEnd
		for (end <- pathsOfEdgeEnd) {
			suspiciousEdges.add(edgeStart, end)
		}

		// mark all paths that reach the start and end vertex -- O(n^2)
		for ((start, descendants) <- transitiveClosure
			 if descendants.contains (edgeStart) && descendants.contains (edgeEnd)) // the latter must not always be true in the case of a back edge where the scc was removed first
		{
		// mark all paths from any start vertex reaching edgeEnd and edgeStart
		suspiciousEdges.add(start, edgeEnd)

		// mark all paths from any start vertex reaching to any vertex reachable by edgeEnd
		for (end <- pathsOfEdgeEnd) {
			suspiciousEdges.add(start, end)
		}
	}


	if (isInSCC(edgeStart, edgeEnd)) {
		resolveSCCRemoval(edgeStart, edgeEnd, suspiciousEdges)
	}


	// filter suspicious edges that are not still in the original graph
	// and remove suspicious edges from transitive closure
	suspiciousEdges =
		for ((start, end) <- suspiciousEdges
			 if !index.contains(start) ||
				 !index.get(start).get.exists(
					 edge => (getTail(edge) == start && getHead(edge) == end)
				 )
		) yield {
			descendants(start).remove(end)
			(start, end)
		}


	// T_ab ∪ (T_ab ο T_ab) ∪ (T_ab ο T_ab ο T_ab)
	// alternative paths can be found by concatenating trusted paths once or twice
	for (i <- 1 to 2) {
		// iterate twice
		var trustedEdges = List[(Vertex, Vertex)]()

		for ((suspiciousStart, suspiciousEnd) <- suspiciousEdges;
			 middle <- descendants(suspiciousStart);
			 middleDescendants = descendants(middle)
			 if middleDescendants.contains(suspiciousEnd)) {
			// do we still have a path from  suspiciousStart to suspiciousEnd => reinsert
			trustedEdges = (suspiciousStart, suspiciousEnd) :: trustedEdges
		}

		for ((start, end) <- trustedEdges) {
			descendants(start).add(end)
			suspiciousEdges.remove(start, end)
		}
	}

	// edges = TC_old - TC_new
	  Seq() ++ suspiciousEdges
	}


	def updated(oldV: Edge, newV: Edge) {
		//a direct update is not supported
		removed(oldV)
		added(newV)
	}

	/**
	 * Returns the size of the view in terms of elements.
	 * This can be a costly operation.
	 * Implementors should cache the value in a self-maintained view, but clients can not rely on this.
	 */
	def size: Int =
		throw new UnsupportedOperationException

}
