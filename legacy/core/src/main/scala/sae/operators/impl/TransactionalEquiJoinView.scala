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
import collections.TransactionalBagIndex
import deltas.{Deletion, Addition, Update}
import sae.operators.EquiJoin

class TransactionalEquiJoinView[DomainA, DomainB, Range, Key](val left: Relation[DomainA],
                                                              val right: Relation[DomainB],
                                                              val leftKey: DomainA => Key,
                                                              val rightKey: DomainB => Key,
                                                              val projection: (DomainA, DomainB) => Range)
    extends EquiJoin[DomainA, DomainB, Range, Key]
{

    val leftAdditionIndex = left.index (leftKey, false)

    val rightAdditionIndex = right.index (rightKey, false)

    val leftDeletionIndex = left.index (leftKey, true)

    val rightDeletionIndex = right.index (rightKey, true)

    // we observe the indices, but the indices are not part of the observer chain
    // indices have a special semantics in order to ensure updates where all indices are updated prior to their observers

    leftAdditionIndex addObserver LeftObserver

    leftDeletionIndex addObserver LeftObserver

    rightAdditionIndex addObserver RightObserver

    rightDeletionIndex addObserver RightObserver


    override protected def children = List (leftAdditionIndex, leftDeletionIndex, rightAdditionIndex, rightAdditionIndex)

    override protected def childObservers(o: Observable[_]): Seq[Observer[_]] = {
        if (o == leftAdditionIndex || o == leftDeletionIndex) {
            return List (LeftObserver)
        }
        if (o == rightAdditionIndex || o == rightDeletionIndex) {
            return List (RightObserver)
        }
        Nil
    }

    /**
     * Applies f to all elements of the view.
     */
    def foreach[T](f: (Range) => T) {
        if (leftAdditionIndex.size <= rightAdditionIndex.size) {
            leftEquiJoin (f)

        }
        else
        {
            rightEquiJoin (f)
        }
    }

    // use the left relation as keys, since this relation is smaller
    def leftEquiJoin[T](f: (Range) => T) {
        leftAdditionIndex.foreach (
        {
            case (key, v) =>
                rightAdditionIndex.get (key) match {
                    case Some (col) => {
                        col.foreach (u =>
                            f (projection (v, u))
                        )
                    }
                    case _ => // do nothing
                }
        }
        )
    }

    // use the right relation as keys, since this relation is smaller
    def rightEquiJoin[T](f: (Range) => T) {
        rightAdditionIndex.foreach (
        {
            case (key, u) =>
                leftAdditionIndex.get (key) match {
                    case Some (col) => {
                        col.foreach (v =>
                            f (projection (v, u))
                        )
                    }
                    case _ => // do nothing
                }
        }
        )
    }


    var leftFinished  = false
    var rightFinished = false

    object LeftObserver extends Observer[(Key, DomainA)]
    {

        override def endTransaction() {
            leftFinished = true
            if (rightFinished) {
                notifyEndTransaction ()
                leftFinished = false
                rightFinished = false
            }
        }

        // update operations on left relation
        def updated(oldKV: (Key, DomainA), newKV: (Key, DomainA)) {
            throw new UnsupportedOperationException
        }

        def removed(kv: (Key, DomainA)) {
            rightDeletionIndex.get (kv._1) match {
                case Some (col) => {
                    col.map (u => (projection (kv._2, u))).foreach (element_removed)
                }
                case _ => // do nothing
            }
        }

        def added(kv: (Key, DomainA)) {
            rightAdditionIndex.get (kv._1) match {
                case Some (col) => {
                    col.map (u => (projection (kv._2, u))).foreach (element_added)
                }
                case _ => // do nothing
            }
        }

        def updated[U <: (Key, DomainA)](update: Update[U]) {
            throw new UnsupportedOperationException
        }

        def modified[U <: (Key, DomainA)](additions: Set[Addition[U]], deletions: Set[Deletion[U]], updates: Set[Update[U]]) {
            throw new UnsupportedOperationException
        }

        override def toString: String = TransactionalEquiJoinView.this.toString + "$LeftObserver"
    }

    object RightObserver extends Observer[(Key, DomainB)]
    {

        override def endTransaction() {
            rightFinished = true
            if (leftFinished) {
                notifyEndTransaction ()
                leftFinished = false
                rightFinished = false
            }
        }

        // update operations on right relation
        def updated(oldKV: (Key, DomainB), newKV: (Key, DomainB)) {
            throw new UnsupportedOperationException
        }

        def removed(kv: (Key, DomainB)) {
            leftDeletionIndex.get (kv._1) match {
                case Some (col) => {
                    col.map (u => (projection (u, kv._2))).foreach (element_removed)
                }
                case _ => // do nothing
            }

        }

        def added(kv: (Key, DomainB)) {
            leftAdditionIndex.get (kv._1) match {
                case Some (col) => {
                    col.map (u => (projection (u, kv._2))).foreach (element_added)
                }
                case _ => // do nothing
            }

        }

        def updated[U <: (Key, DomainB)](update: Update[U]) {
            throw new UnsupportedOperationException
        }

        def modified[U <: (Key, DomainB)](additions: Set[Addition[U]], deletions: Set[Deletion[U]], updates: Set[Update[U]]) {
            throw new UnsupportedOperationException
        }

        override def toString: String = TransactionalEquiJoinView.this.toString + "$RightObserver"
    }

}
