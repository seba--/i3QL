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
import idb.{MaterializedView, Relation}
import idb.operators.TransitiveClosure
import idb.observer.{Observer, Observable, NotifyObservers}

/**
 * A simple transitive closure, that should support minimal memory, by just storing the graph.
 *
 * The head of an edge is denoting the arrow head, hence the end vertex
 * The tail of an edge is denoting the start vertex
 *
 * @author Ralf Mitschke
 */
class TransitiveClosureViewAcyclicGraphStore[Edge, Vertex](val source: Relation[Edge],
                                                           val getTail: Edge => Vertex,
                                                           val getHead: Edge => Vertex,
                                                           override val isSet: Boolean)
  extends TransitiveClosure[Edge, Vertex]
  with Observer[Edge]
  with NotifyObservers[(Vertex, Vertex)]
  with MaterializedView[(Vertex, Vertex)] {
  source addObserver this

  private val graphIncomingEdges = mutable.HashMap[Vertex, List[Edge]]()

  private val graphOutgoingEdges = mutable.HashMap[Vertex, List[Edge]]()

  lazyInitialize()

  private def transitiveClosureApplyForward[U](start: Vertex, f: (Vertex, Vertex) => U) {
    transitiveClosureRecurseForward(start, start, f)
  }

  private def transitiveClosureRecurseForward[U](start: Vertex, current: Vertex, f: (Vertex, Vertex) => U) {
    graphOutgoingEdges.getOrElse((current), return).foreach(
      edge => {
        val head = getHead(edge)
        f(start, head)
        transitiveClosureRecurseForward(start, head, f)
      }
    )
  }


  private def transitiveClosureApplyBackward[U](end: Vertex, f: (Vertex, Vertex) => U) {
    transitiveClosureRecurseBackward(end, end, f)
  }

  private def transitiveClosureRecurseBackward[U](end: Vertex, current: Vertex, f: (Vertex, Vertex) => U) {
    println("transitiveClosureRecurseBackward(" + end + ", " + current + ", f)")
    graphIncomingEdges.getOrElse((current), return).foreach(
      edge => {
        val tail = getTail(edge)
        f(tail, end)
        transitiveClosureRecurseBackward(end, tail, f)
      }
    )
  }

  override protected def childObservers(o: Observable[_]): Seq[Observer[_]] = {
    if (o == source) {
      return List(this)
    }
    Nil
  }

  override protected def resetInternal(): Unit = ???


  /**
   * access in O(n)
   */
  def isDefinedAt(edge: (Vertex, Vertex)): Boolean = {
    transitiveClosureApplyForward[Unit](
      edge._1,
      (start: Vertex, end: Vertex) => {
        if (end == edge._2) {
          return true
        }
      }
    )
    false
  }

  /**
   * access in O(n&#94;2)
   */
  def count[T >: (Vertex, Vertex)](edge: T): Int = {
    throw new UnsupportedOperationException
  }

  override def size(): Int = {
    throw new UnsupportedOperationException
  }

  def contains[U >: (Vertex, Vertex)](element: U): Boolean = {
    throw new UnsupportedOperationException
  }

  def foreach[T](f: ((Vertex, Vertex)) => T) {
    graphOutgoingEdges.keys.foreach(v =>
      transitiveClosureApplyForward[Unit](
        v,
        (start: Vertex, end: Vertex) => {
          f((start, end))
        }
      )
    )
  }

  def foreachWithCount[T](f: ((Vertex, Vertex), Int) => T) {
    throw new UnsupportedOperationException
  }


  private def addGraphEdge(edge: Edge) {
    val head = getHead(edge)
    val tail = getTail(edge)
    graphIncomingEdges(head) = edge :: graphIncomingEdges.getOrElseUpdate(head, Nil)
    graphOutgoingEdges(tail) = edge :: graphOutgoingEdges.getOrElseUpdate(tail, Nil)
  }

  private def removeGraphEdge(edge: Edge) {
    val head = getHead(edge)
    val tail = getTail(edge)
    graphIncomingEdges(head) = graphIncomingEdges(head) filterNot (_ == edge)
    graphOutgoingEdges(tail) = graphOutgoingEdges(tail) filterNot (_ == edge)
  }

  private def graphContains(edge: Edge): Boolean = {
    val head = getHead(edge)
    val tail = getTail(edge)
    graphIncomingEdges.getOrElse(head, return false).contains(edge) &&
      graphOutgoingEdges.getOrElse(tail, return false).contains(edge)
  }

  def lazyInitialize() {
    source.foreach(addGraphEdge)
  }

  override def endTransaction() {
    notify_endTransaction()
  }

  def internal_add(edge: Edge): Seq[(Vertex, Vertex)] = {
    var added = Seq[(Vertex,Vertex)]()

    val head = getHead(edge)
    val tail = getTail(edge)

    if (graphContains(edge)) {
      return added
    }

    var reachableFromHead: List[Vertex] = Nil
    var reachingToTail: List[Vertex] = Nil

    transitiveClosureApplyForward(head, (start: Vertex, end: Vertex) => reachableFromHead = end :: reachableFromHead)
    transitiveClosureApplyBackward(tail, (start: Vertex, end: Vertex) => reachingToTail = start :: reachingToTail)

    addGraphEdge(edge)

    added = (tail, head) +: added
    reachingToTail.foreach(v => added = (v, head) +: added)

    reachableFromHead.foreach(
      end => {
        reachingToTail.foreach(
          start => {
            added = (start, end) +: added
          }
        )
        added = (tail, end) +: added
      }
    )

    added
  }

  def added(edge: Edge): Unit = {
    val added = internal_add(edge)
    notify_addedAll(added)
  }

  def addedAll(edges: Seq[Edge]): Unit = {
    val added = edges.foldLeft(Seq[(Vertex,Vertex)]())((seq,edge) => seq ++ internal_add(edge))
    notify_addedAll(added)
  }

  def internal_remove(edge: Edge): Seq[(Vertex,Vertex)] = {
    var removed = Seq[(Vertex,Vertex)]()

    val head = getHead(edge)
    val tail = getTail(edge)

    var reachableFromHead: List[Vertex] = Nil
    var reachingToTail: List[Vertex] = Nil

    transitiveClosureApplyForward(head, (start: Vertex, end: Vertex) => reachableFromHead = end :: reachableFromHead)
    transitiveClosureApplyBackward(tail, (start: Vertex, end: Vertex) => reachingToTail = start :: reachingToTail)

    removeGraphEdge(edge)

    removed = (tail, head) +: removed
    reachingToTail.foreach(v => removed = (v, head) +: removed)

    reachableFromHead.foreach(
      end => {
        reachingToTail.foreach(
          start => {
            removed = (start, end) +: removed
          }
        )
        removed = (tail, end) +: removed
      }
    )
    removed
  }

  def removed(edge: Edge): Unit = {
    val removed = internal_remove(edge)
    notify_removedAll(removed)
  }

  def removedAll(edges: Seq[Edge]): Unit = {
    val removed = edges.foldLeft(Seq[(Vertex,Vertex)]())((seq,edge) => seq ++ internal_remove(edge))
    notify_addedAll(removed)
  }

  def updated(oldV: Edge, newV: Edge) {
    //a direct update is not supported
    removed(oldV)
    added(newV)
  }
}
