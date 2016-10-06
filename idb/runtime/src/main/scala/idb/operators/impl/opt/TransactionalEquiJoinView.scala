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

import idb.{IndexService, Index, Relation}
import idb.operators.EquiJoin
import idb.observer.{Observable, Observer, NotifyObservers}


class TransactionalEquiJoinView[DomainA, DomainB, Range, Key](val left: Relation[DomainA],
                                                              val right: Relation[DomainB],
                                                              val leftAdditionIndex: Index[Key, DomainA],
                                                              val rightAdditionIndex: Index[Key, DomainB],
                                                              val leftDeletionIndex: Index[Key, DomainA],
                                                              val rightDeletionIndex: Index[Key, DomainB],
                                                              val leftKey: DomainA => Key,
                                                              val rightKey: DomainB => Key,
                                                              val projection: (DomainA, DomainB) => Range,
                                                              override val isSet: Boolean)
  extends EquiJoin[DomainA, DomainB, Range, Key]
  with NotifyObservers[Range] {


  // we observe the indices, but the indices are not part of the observer chain
  // indices have a special semantics in order to ensure updates where all indices are updated prior to their observers

  leftAdditionIndex addObserver LeftObserver

  leftDeletionIndex addObserver LeftObserver

  rightAdditionIndex addObserver RightObserver

  rightDeletionIndex addObserver RightObserver


  override def children() = List(leftAdditionIndex, leftDeletionIndex, rightAdditionIndex, rightAdditionIndex)

  override protected def childObservers(o: Observable[_]): Seq[Observer[_]] = {
    if (o == leftAdditionIndex || o == leftDeletionIndex) {
      return List(LeftObserver)
    }
    if (o == rightAdditionIndex || o == rightDeletionIndex) {
      return List(RightObserver)
    }
    Nil
  }

  def lazyInitialize() {

  }

  override protected def resetInternal(): Unit = ???

  /**
   * Applies f to all elements of the view.
   */
  def foreach[T](f: (Range) => T) {
    /*if (leftAdditionIndex.size <= rightAdditionIndex.size) {
      leftEquiJoin(f)

    }
    else {
      rightEquiJoin(f)
    } */
    throw new UnsupportedOperationException("Method foreach is not implemented for transactional operators.")
  }

  // use the left relation as keys, since this relation is smaller
  def leftEquiJoin[T](f: (Range) => T) {
    leftAdditionIndex.foreach(
    {
      case (key, v) =>
        rightAdditionIndex.get(key) match {
          case Some(col) => {
            col.foreach(u =>
              f(projection(v, u))
            )
          }
          case _ => // do nothing
        }
    }
    )
  }

  // use the right relation as keys, since this relation is smaller
  def rightEquiJoin[T](f: (Range) => T) {
    rightAdditionIndex.foreach(
    {
      case (key, u) =>
        leftAdditionIndex.get(key) match {
          case Some(col) => {
            col.foreach(v =>
              f(projection(v, u))
            )
          }
          case _ => // do nothing
        }
    }
    )
  }


  var leftFinished = false
  var rightFinished = false

  object LeftObserver extends Observer[(Key, DomainA)] {

    override def endTransaction() {
      leftFinished = true
      if (rightFinished) {
        notify_endTransaction()
        leftFinished = false
        rightFinished = false
      }
    }

    // update operations on left relation
    def updated(oldKV: (Key, DomainA), newKV: (Key, DomainA)) {
      throw new UnsupportedOperationException
    }

    def removed(kv: (Key, DomainA)) {
      var removed = Seq[Range]()
      rightDeletionIndex.get(kv._1) match {
        case Some(col) => {
          for (u <- col)
            removed = projection(kv._2, u) +: removed
        }
        case _ => // do nothing
      }
      notify_removedAll(removed)
    }

    def removedAll(kvs: Seq[(Key, DomainA)]) {
      var removed = Seq[Range]()
      for (kv <- kvs)
        rightDeletionIndex.get(kv._1) match {
          case Some(col) => {
            for (u <- col)
              removed = projection(kv._2, u) +: removed
          }
          case _ => // do nothing
        }
      notify_removedAll(removed)
    }

    def added(kv: (Key, DomainA)) {
      var added = Seq[Range]()
      rightAdditionIndex.get(kv._1) match {
        case Some(col) => {
          for (u <- col)
            added = projection(kv._2, u) +: added
        }
        case _ => // do nothing
      }
      notify_addedAll(added)
    }

    def addedAll(kvs: Seq[(Key, DomainA)]) {
      var added = Seq[Range]()
      for (kv <- kvs)
        rightAdditionIndex.get(kv._1) match {
          case Some(col) => {
            for (u <- col)
              added = projection(kv._2, u) +: added
          }
          case _ => // do nothing
        }
      notify_addedAll(added)
    }

    override def toString: String = TransactionalEquiJoinView.this.toString + "$LeftObserver"
  }

  object RightObserver extends Observer[(Key, DomainB)] {

    override def endTransaction() {
      rightFinished = true
      if (leftFinished) {
        notify_endTransaction()
        leftFinished = false
        rightFinished = false
      }
    }

    // update operations on right relation
    def updated(oldKV: (Key, DomainB), newKV: (Key, DomainB)) {
      throw new UnsupportedOperationException
    }

    def removed(kv: (Key, DomainB)) {
      var removed = Seq[Range]()
      leftDeletionIndex.get(kv._1) match {
        case Some(col) => {
          for (u <- col)
            removed = projection(u, kv._2) +: removed
        }
        case _ => // do nothing
      }
      notify_removedAll(removed)
    }

    def removedAll(kvs: Seq[(Key, DomainB)]) {
      var removed = Seq[Range]()
      for (kv <- kvs)
        leftDeletionIndex.get(kv._1) match {
          case Some(col) => {
            for (u <- col)
              removed = projection(u, kv._2) +: removed
          }
          case _ => // do nothing
        }
      notify_removedAll(removed)
    }

    def added(kv: (Key, DomainB)) {
      var added = Seq[Range]()
      leftAdditionIndex.get(kv._1) match {
        case Some(col) => {
          for (u <- col)
            added = projection(u, kv._2) +: added
        }
        case _ => // do nothing
      }
      notify_addedAll(added)
    }

    def addedAll(kvs: Seq[(Key, DomainB)]) {
      var added = Seq[Range]()
      for (kv <- kvs)
        leftAdditionIndex.get(kv._1) match {
          case Some(col) => {
            for (u <- col)
              added = projection(u, kv._2) +: added
          }
          case _ => // do nothing
        }
      notify_addedAll(added)
    }

    override def toString: String = TransactionalEquiJoinView.this.toString + "$RightObserver"
  }

}

object TransactionalEquiJoinView {
  def apply[DomainA, DomainB](left: Relation[DomainA],
                              right: Relation[DomainB],
                              leftEq: Seq[(DomainA => Any)],
                              rightEq: Seq[(DomainB => Any)],
                              isSet: Boolean): Relation[(DomainA, DomainB)] = {

    val leftKey: DomainA => Seq[Any] = x => leftEq.map(f => f(x))
    val rightKey: DomainB => Seq[Any] = x => rightEq.map(f => f(x))

    val leftIndex: Index[Seq[Any], DomainA] = IndexService.getIndex(left, leftKey)
    val rightIndex: Index[Seq[Any], DomainB] = IndexService.getIndex(right, rightKey)
    val leftDelIndex: Index[Seq[Any], DomainA] = IndexService.getIndex(left, leftKey)
    val rightDelIndex: Index[Seq[Any], DomainB] = IndexService.getIndex(right, rightKey)

    return new TransactionalEquiJoinView[DomainA, DomainB, (DomainA, DomainB), Seq[Any]](
      left,
      right,
      leftIndex,
      rightIndex,
      leftDelIndex,
      rightDelIndex,
      leftKey,
      rightKey,
      (_, _),
      isSet
    )


  }

}
