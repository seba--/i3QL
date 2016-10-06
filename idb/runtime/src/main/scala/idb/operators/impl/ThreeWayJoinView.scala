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

import idb.operators.ThreeWayJoin
import idb.observer.{Observable, Observer, NotifyObservers}
import idb.{Index, Relation}


class ThreeWayJoinView[DomainA, DomainB, DomainC, Range, KeyA, KeyC](val left: Relation[DomainA],
                                                                     val middle: Relation[DomainB],
                                                                     val right: Relation[DomainC],
                                                                     val leftIndex: Index[KeyA, DomainA],
                                                                     val middleToLeftIndex: Index[KeyA, DomainB],
                                                                     val middleToRightIndex: Index[KeyC, DomainB],
                                                                     val rightIndex: Index[KeyC, DomainC],
                                                                     val projection: (DomainA, DomainB, DomainC) => Range,
                                                                     override val isSet: Boolean)
  extends ThreeWayJoin[DomainA, DomainB, DomainC, Range, KeyA, KeyC]
  with NotifyObservers[Range] {


  // we observe the indices, but the indices are not part of the observer chain
  // indices have a special semantics in order to ensure updates where all indices are updated prior to their observers

  val leftKey: DomainA => KeyA = leftIndex.keyFunction
  val middleToLeftKey: DomainB => KeyA = middleToLeftIndex.keyFunction
  val middleToRightKey: DomainB => KeyC = middleToRightIndex.keyFunction
  val rightKey: DomainC => KeyC = rightIndex.keyFunction

  leftIndex addObserver LeftObserver

  middleToLeftIndex addObserver MiddleToLeftObserver

  middleToRightIndex addObserver MiddleToRightObserver

  rightIndex addObserver RightObserver

  override def children() = List(leftIndex, middleToLeftIndex, middleToRightIndex, rightIndex)

  override protected def resetInternal(): Unit = ???


  override protected def childObservers(o: Observable[_]): Seq[Observer[_]] = {
    if (o == leftIndex) {
      return List(LeftObserver)
    }
    if (o == middleToLeftIndex) {
      return List(MiddleToLeftObserver)
    }
    if (o == middleToRightIndex) {
      return List(MiddleToRightObserver)
    }
    if (o == rightIndex) {
      return List(RightObserver)
    }
    Nil
  }

  override def lazyInitialize() {

  }

  /**
   * Applies f to all elements of the view.
   */
  def foreach[T](f: (Range) => T) {
    threeWayJoin(f)
  }


  private def threeWayJoin[T](f: (Range) => T) {
    leftIndex.foreach {
      case (key, v) =>
        joinOnLeft(v, key) map f
    }
  }

  private def joinOnLeft[T](v: DomainA, key: KeyA): Seq[Range] = {
    var changed = Seq[Range]()
    if (middleToLeftIndex.contains(key)) {
      val middleElements = middleToLeftIndex.get(key).get
      for (middle <- middleElements) {
        val rightKey = middleToRightKey(middle)
        if (middleToRightIndex.contains(rightKey) && rightIndex.contains(rightKey)) {
          val rightElements = rightIndex.get(rightKey).get
          for (right <- rightElements) {
            changed = projection(v, middle, right) +: changed
          }
        }
      }
    }
    changed
  }

  private def joinOnMiddleToLeft[T](v: DomainB, leftKey: KeyA): Seq[Range] = {
    var changed = Seq[Range]()
    val rightKey = middleToRightKey(v)
    if (middleToRightIndex.contains(rightKey) && middleToRightIndex.get(rightKey).get.exists(_ == v) && leftIndex.contains(leftKey) && rightIndex.contains(rightKey)) {
      val leftElements = leftIndex.get(leftKey).get
      val rightElements = rightIndex.get(rightKey).get
      for (left <- leftElements; right <- rightElements) {
        changed = projection(left, v, right) +: changed
      }
    }
    changed
  }

  private def joinOnMiddleToRight[T](v: DomainB, rightKey: KeyC): Seq[Range] = {
    var changed = Seq[Range]()
    val leftKey = middleToLeftKey(v)
    if (middleToLeftIndex.contains(leftKey) && middleToLeftIndex.get(leftKey).get.exists(_ == v) && leftIndex.contains(leftKey) && rightIndex.contains(rightKey)) {
      val leftElements = leftIndex.get(leftKey).get
      val rightElements = rightIndex.get(rightKey).get
      for (left <- leftElements; right <- rightElements) {
        changed = projection(left, v, right) +: changed
      }
    }
    changed
  }


  private def joinOnRight[T](v: DomainC, key: KeyC): Seq[Range] = {
    var changed = Seq[Range]()
    if (middleToRightIndex.contains(key)) {
      val middleElements = middleToRightIndex.get(key).get
      for (middle <- middleElements) {
        val leftKey = middleToLeftKey(middle)
        if (middleToLeftIndex.contains(leftKey) && leftIndex.contains(leftKey)) {
          val leftElements = leftIndex.get(leftKey).get
          for (left <- leftElements) {
            changed = projection(left, middle, v) +: changed
          }
        }
      }
    }
    changed
  }

  object LeftObserver extends Observer[(KeyA, DomainA)] {

    override def endTransaction() {
      notify_endTransaction()
    }

    // update operations on left relation
    def updated(oldKV: (KeyA, DomainA), newKV: (KeyA, DomainA)) {
      throw new UnsupportedOperationException
    }

    def removed(kv: (KeyA, DomainA)) {
      val removed = joinOnLeft(kv._2, kv._1)
      notify_removedAll(removed)
    }

    def removedAll(kvs: Seq[(KeyA, DomainA)]) {
      val removed = kvs.foldLeft(Seq[Range]())((seq,kv) => seq ++ joinOnLeft(kv._2, kv._1))
      notify_removedAll(removed)
    }

    def added(kv: (KeyA, DomainA)) {
      val added = joinOnLeft(kv._2, kv._1)
      notify_addedAll(added)
    }

    def addedAll(kvs: Seq[(KeyA, DomainA)]) {
      val added = kvs.foldLeft(Seq[Range]())((seq,kv) => seq ++ joinOnLeft(kv._2, kv._1))
      notify_addedAll(added)
    }
  }

  object MiddleToLeftObserver extends Observer[(KeyA, DomainB)] {

    override def endTransaction() {
      notify_endTransaction()
    }

    def updated(oldKV: (KeyA, DomainB), newKV: (KeyA, DomainB)) {
      throw new UnsupportedOperationException
    }

    def removed(kv: (KeyA, DomainB)) {
      val removed = joinOnMiddleToLeft(kv._2, kv._1)
      notify_removedAll(removed)
    }

    def removedAll(kvs: Seq[(KeyA, DomainB)]) {
      val removed = kvs.foldLeft(Seq[Range]())((seq,kv) => seq ++ joinOnMiddleToLeft(kv._2, kv._1))
      notify_removedAll(removed)
    }

    def added(kv: (KeyA, DomainB)) {
      val added = joinOnMiddleToLeft(kv._2, kv._1)
      notify_addedAll(added)
    }

    def addedAll(kvs: Seq[(KeyA, DomainB)]) {
      val added = kvs.foldLeft(Seq[Range]())((seq,kv) => seq ++ joinOnMiddleToLeft(kv._2, kv._1))
      notify_addedAll(added)
    }
  }

  object MiddleToRightObserver extends Observer[(KeyC, DomainB)] {

    override def endTransaction() {
      notify_endTransaction()
    }

    def updated(oldKV: (KeyC, DomainB), newKV: (KeyC, DomainB)) {
      throw new UnsupportedOperationException
    }

    def removed(kv: (KeyC, DomainB)) {
      val removed = joinOnMiddleToRight(kv._2, kv._1)
      notify_removedAll(removed)
    }

    def removedAll(kvs: Seq[(KeyC, DomainB)]) {
      val removed = kvs.foldLeft(Seq[Range]())((seq,kv) => seq ++ joinOnMiddleToRight(kv._2, kv._1))
      notify_removedAll(removed)
    }

    def added(kv: (KeyC, DomainB)) {
      val added = joinOnMiddleToRight(kv._2, kv._1)
      notify_addedAll(added)
    }

    def addedAll(kvs: Seq[(KeyC, DomainB)]) {
      val added = kvs.foldLeft(Seq[Range]())((seq,kv) => seq ++ joinOnMiddleToRight(kv._2, kv._1))
      notify_addedAll(added)
    }
  }


  object RightObserver extends Observer[(KeyC, DomainC)] {

    override def endTransaction() {
      notify_endTransaction()
    }

    def updated(oldKV: (KeyC, DomainC), newKV: (KeyC, DomainC)) {
      throw new UnsupportedOperationException
    }

    def removed(kv: (KeyC, DomainC)) {
      val removed = joinOnRight(kv._2, kv._1)
      notify_removedAll(removed)
    }

    def removedAll(kvs: Seq[(KeyC, DomainC)]) {
      val removed = kvs.foldLeft(Seq[Range]())((seq,kv) => seq ++ joinOnRight(kv._2, kv._1))
      notify_removedAll(removed)
    }

    def added(kv: (KeyC, DomainC)) {
      val added = joinOnRight(kv._2, kv._1)
      notify_addedAll(added)
    }

    def addedAll(kvs: Seq[(KeyC, DomainC)]) {
      val added = kvs.foldLeft(Seq[Range]())((seq,kv) => seq ++ joinOnRight(kv._2, kv._1))
      notify_addedAll(added)
    }
  }

}
