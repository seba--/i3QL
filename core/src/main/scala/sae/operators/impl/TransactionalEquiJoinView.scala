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

import sae._
import deltas.{Update, Deletion, Addition}
import sae.operators.EquiJoin

class TransactionalEquiJoinView[DomainA, DomainB, Range, Key](val left: Relation[DomainA],
                                                              val right: Relation[DomainB],
                                                              val leftKey: DomainA => Key,
                                                              val rightKey: DomainB => Key,
                                                              val projection: (DomainA, DomainB) => Range)
        extends EquiJoin[DomainA, DomainB, Range, Key]
{

    left addObserver LeftObserver

    right addObserver RightObserver

    override protected def children = List(left, right)

    override protected def childObservers(o: Observable[_]): Seq[Observer[_]] = {
        if (o == left) {
            return List(LeftObserver)
        }
        if (o == right) {
            return List(RightObserver)
        }
        Nil
    }

    /**
     * Applies f to all elements of the view.
     */
    def foreach[T](f: (Range) => T) {
        val idx = com.google.common.collect.ArrayListMultimap.create[Key, DomainA]()
        left.foreach(v =>
            idx.put(leftKey(v), v)
        )
        right.foreach(v => {
            val k = rightKey(v)
            if (idx.containsKey(k)) {
                val leftElements = idx.get(k)
                val it = leftElements.iterator()
                while (it.hasNext) {
                    val l = it.next()
                    f(projection(l, v))
                }
            }
        }
        )
    }

    private def doJoinAndCleanup() {
        joinAdditions()
        joinDeletions()
        LeftObserver.additions.clear()
        LeftObserver.deletions.clear()
        RightObserver.additions.clear()
        RightObserver.deletions.clear()
    }

    private def joinAdditions() {
        val it: java.util.Iterator[java.util.Map.Entry[Key, DomainA]] = LeftObserver.additions.entries ().iterator
        while (it.hasNext) {
            val next = it.next ()
            val left = next.getValue
            val k = next.getKey
            if (RightObserver.additions.containsKey(k)) {
                val rightElements = RightObserver.additions.get(k)
                val it = rightElements.iterator()
                while (it.hasNext) {
                    val right = it.next()
                    element_added(projection(left, right))
                }
            }
        }
    }

    private def joinDeletions() {
        val it: java.util.Iterator[java.util.Map.Entry[Key, DomainA]] = LeftObserver.deletions.entries ().iterator
        while (it.hasNext) {
            val next = it.next ()
            val left = next.getValue
            val k = next.getKey
            if (RightObserver.deletions.containsKey(k)) {
                val rightElements = RightObserver.deletions.get(k)
                val it = rightElements.iterator()
                while (it.hasNext) {
                    val right = it.next()
                    element_removed(projection(left, right))
                }
            }
        }
    }

    object LeftObserver extends Observer[DomainA]
    {

        val additions = com.google.common.collect.ArrayListMultimap.create[Key, DomainA]()

        val deletions = com.google.common.collect.ArrayListMultimap.create[Key, DomainA]()

        override def endTransaction() {
            doJoinAndCleanup()
            notifyEndTransaction()
        }

        // update operations on left relation
        def updated(oldV: DomainA, newV: DomainA) {
            additions.put(leftKey(newV), newV)
            deletions.put(leftKey(oldV), oldV)
        }

        def removed(v: DomainA) {
            deletions.put(leftKey(v), v)
        }

        def added(v: DomainA) {
            additions.put(leftKey(v), v)
        }

        def updated[U <: DomainA](update: Update[U]) {
            throw new UnsupportedOperationException
        }

        def modified[U <: DomainA](additions: Set[Addition[U]], deletions: Set[Deletion[U]], updates: Set[Update[U]]) {
            throw new UnsupportedOperationException
        }
    }

    object RightObserver extends Observer[DomainB]
    {

        val additions = com.google.common.collect.ArrayListMultimap.create[Key, DomainB]()

        val deletions = com.google.common.collect.ArrayListMultimap.create[Key, DomainB]()

        override def endTransaction() {
            doJoinAndCleanup()
            notifyEndTransaction()
        }

        // update operations on right relation
        def updated(oldV: DomainB, newV: DomainB) {
            additions.put(rightKey(newV), newV)
            deletions.put(rightKey(oldV), oldV)
        }

        def removed(v: DomainB) {
            deletions.put(rightKey(v), v)
        }

        def added(v: DomainB) {
            additions.put(rightKey(v), v)
        }

        def updated[U <: DomainB](update: Update[U]) {
            throw new UnsupportedOperationException
        }

        def modified[U <: DomainB](additions: Set[Addition[U]], deletions: Set[Deletion[U]], updates: Set[Update[U]]) {
            throw new UnsupportedOperationException
        }
    }

}
