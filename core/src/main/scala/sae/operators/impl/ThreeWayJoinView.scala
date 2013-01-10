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
import operators.ThreeWayJoin

class ThreeWayJoinView[DomainA, DomainB, DomainC, Range, KeyA, KeyC](val left: Relation[DomainA],
                                                                     val middle: Relation[DomainB],
                                                                     val right: Relation[DomainC],
                                                                     val leftKey: DomainA => KeyA,
                                                                     val middleToLeftKey: DomainB => KeyA,
                                                                     val middleToRightKey: DomainB => KeyC,
                                                                     val rightKey: DomainC => KeyC,
                                                                     val projection: (DomainA, DomainB, DomainC) => Range)
    extends ThreeWayJoin[DomainA, DomainB, DomainC, Range, KeyA, KeyC]
{

    val leftIndex = left.index (leftKey)

    val middleToLeftIndex = middle.index (middleToLeftKey)

    val middleToRightIndex = middle.index (middleToRightKey)

    val rightIndex = right.index (rightKey)

    // we observe the indices, but the indices are not part of the observer chain
    // indices have a special semantics in order to ensure updates where all indices are updated prior to their observers

    leftIndex addObserver LeftObserver

    middleToLeftIndex addObserver MiddleToLeftObserver

    middleToRightIndex addObserver MiddleToRightObserver

    rightIndex addObserver RightObserver

    override protected def children = List (leftIndex, middleToLeftIndex, middleToRightIndex, rightIndex)

    override protected def childObservers(o: Observable[_]): Seq[Observer[_]] = {
        if (o == leftIndex) {
            return List (LeftObserver)
        }
        if (o == middleToLeftIndex) {
            return List (MiddleToLeftObserver)
        }
        if (o == middleToRightIndex) {
            return List (MiddleToRightObserver)
        }
        if (o == rightIndex) {
            return List (RightObserver)
        }
        Nil
    }

    /**
     * Applies f to all elements of the view.
     */
    def foreach[T](f: (Range) => T) {
        threeWayJoin (f)
    }


    private def threeWayJoin[T](f: (Range) => T) {
        leftIndex.foreach {
            case (key, v) =>
                joinOnLeft (v, key, f)
        }
    }

    private def joinOnLeft[T](v: DomainA,
                              key: KeyA,
                              f: (Range) => T)
    {
        if (middleToLeftIndex.isDefinedAt (key)) {
            val middleElements = middleToLeftIndex.get (key).get
            for (middle <- middleElements) {
                val rightKey = middleToRightKey (middle)
                if (middleToRightIndex.isDefinedAt(rightKey) && rightIndex.isDefinedAt (rightKey)) {
                    val rightElements = rightIndex.get (rightKey).get
                    for (right <- rightElements) {
                        f (projection (v, middle, right))
                    }
                }

            }
        }
    }

    private def joinOnMiddleToLeft[T](v: DomainB,
                                      leftKey: KeyA,
                                      f: (Range) => T)
    {

        val rightKey = middleToRightKey (v)
        if (middleToRightIndex.isDefinedAt(rightKey) && middleToRightIndex.get(rightKey).get.exists(_ == v) && leftIndex.isDefinedAt (leftKey) && rightIndex.isDefinedAt (rightKey)) {
            val leftElements = leftIndex.get (leftKey).get
            val rightElements = rightIndex.get (rightKey).get
            for (left <- leftElements; right <- rightElements) {
                f (projection (left, v, right))
            }
        }
    }

    private def joinOnMiddleToRight[T](v: DomainB,
                                       rightKey: KeyC,
                                       f: (Range) => T)
    {
        val leftKey = middleToLeftKey (v)
        if (middleToLeftIndex.isDefinedAt(leftKey) && middleToLeftIndex.get(leftKey).get.exists(_ == v) && leftIndex.isDefinedAt (leftKey) && rightIndex.isDefinedAt (rightKey)) {
            val leftElements = leftIndex.get (leftKey).get
            val rightElements = rightIndex.get (rightKey).get
            for (left <- leftElements; right <- rightElements) {
                f (projection (left, v, right))
            }
        }
    }


    private def joinOnRight[T](v: DomainC,
                               key: KeyC,
                               f: (Range) => T)
    {
        if (middleToRightIndex.isDefinedAt (key)) {
            val middleElements = middleToRightIndex.get (key).get
            for (middle <- middleElements) {
                val leftKey = middleToLeftKey (middle)
                if (middleToLeftIndex.isDefinedAt(leftKey) && leftIndex.isDefinedAt (leftKey)) {
                    val leftElements = leftIndex.get (leftKey).get
                    for (left <- leftElements) {
                        f (projection (left, middle, v))
                    }
                }

            }
        }
    }

    object LeftObserver extends Observer[(KeyA, DomainA)]
    {

        override def endTransaction() {
            notifyEndTransaction ()
        }

        // update operations on left relation
        def updated(oldKV: (KeyA, DomainA), newKV: (KeyA, DomainA)) {
            throw new UnsupportedOperationException
        }

        def removed(kv: (KeyA, DomainA)) {
            joinOnLeft (kv._2, kv._1, element_removed)
        }

        def added(kv: (KeyA, DomainA)) {
            joinOnLeft (kv._2, kv._1, element_added)
        }

        def updated[U <: (KeyA, DomainA)](update: Update[U]) {
            throw new UnsupportedOperationException
        }

        def modified[U <: (KeyA, DomainA)](additions: Set[Addition[U]], deletions: Set[Deletion[U]], updates: Set[Update[U]]) {
            throw new UnsupportedOperationException
        }
    }

    object MiddleToLeftObserver extends Observer[(KeyA, DomainB)]
    {

        override def endTransaction() {
            notifyEndTransaction ()
        }

        def updated(oldKV: (KeyA, DomainB), newKV: (KeyA, DomainB)) {
            throw new UnsupportedOperationException
        }

        def removed(kv: (KeyA, DomainB)) {
            joinOnMiddleToLeft (kv._2, kv._1, element_removed)
        }

        def added(kv: (KeyA, DomainB)) {
            joinOnMiddleToLeft (kv._2, kv._1, element_added)
        }

        def updated[U <: (KeyA, DomainB)](update: Update[U]) {
            throw new UnsupportedOperationException
        }

        def modified[U <: (KeyA, DomainB)](additions: Set[Addition[U]], deletions: Set[Deletion[U]], updates: Set[Update[U]]) {
            throw new UnsupportedOperationException
        }
    }

    object MiddleToRightObserver extends Observer[(KeyC, DomainB)]
    {

        override def endTransaction() {
            notifyEndTransaction ()
        }

        def updated(oldKV: (KeyC, DomainB), newKV: (KeyC, DomainB)) {
            throw new UnsupportedOperationException
        }

        def removed(kv: (KeyC, DomainB)) {
            joinOnMiddleToRight (kv._2, kv._1, element_removed)
        }

        def added(kv: (KeyC, DomainB)) {
            joinOnMiddleToRight (kv._2, kv._1, element_added)
        }

        def updated[U <: (KeyC, DomainB)](update: Update[U]) {
            throw new UnsupportedOperationException
        }

        def modified[U <: (KeyC, DomainB)](additions: Set[Addition[U]], deletions: Set[Deletion[U]], updates: Set[Update[U]]) {
            throw new UnsupportedOperationException
        }
    }


    object RightObserver extends Observer[(KeyC, DomainC)]
    {

        override def endTransaction() {
            notifyEndTransaction ()
        }

        def updated(oldKV: (KeyC, DomainC), newKV: (KeyC, DomainC)) {
            throw new UnsupportedOperationException
        }

        def removed(kv: (KeyC, DomainC)) {
            joinOnRight (kv._2, kv._1, element_removed)
        }

        def added(kv: (KeyC, DomainC)) {
            joinOnRight (kv._2, kv._1, element_added)
        }

        def updated[U <: (KeyC, DomainC)](update: Update[U]) {
            throw new UnsupportedOperationException
        }

        def modified[U <: (KeyC, DomainC)](additions: Set[Addition[U]], deletions: Set[Deletion[U]], updates: Set[Update[U]]) {
            throw new UnsupportedOperationException
        }
    }

}
