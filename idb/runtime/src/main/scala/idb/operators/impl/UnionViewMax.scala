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

import idb.operators.Union
import idb.observer.{Observable, Observer, NotifyObservers}
import idb.MaterializedView


/**
 * A not self maintained union, that produces max(count(A) , count(B)) duplicates for underlying relations A and B
 * Required for correctness if we translate conditionA OR conditionB to algebra.
 * Can be omitted if A \intersect B == empty
 *
 * The UnionViewMax requires materialized relations as underlying relations, but does not require to store the newResult.
 */
class UnionViewMax[Range, DomainA <: Range, DomainB <: Range](val left: MaterializedView[DomainA],
                                                              val right: MaterializedView[DomainB],
                                                              override val isSet: Boolean)
  extends Union[Range, DomainA, DomainB]
  with NotifyObservers[Range] {
  left addObserver LeftObserver
  right addObserver RightObserver


  override def lazyInitialize() {

  }

  override protected def childObservers(o: Observable[_]): Seq[Observer[_]] = {
    if (o == left) {
      return List(LeftObserver)
    }
    if (o == right) {
      return List(RightObserver)
    }
    Nil
  }


  def foreach[T](f: (Range) => T) {
    left.foreach(
      (v: Range) => {
        val max = scala.math.max(left.count(v), right.count(v))
        var i = 0
        while (i < max) {
          f(v)
          i += 1
        }
      }
    )
  }

  override protected def resetInternal(): Unit = ???


  object LeftObserver extends Observer[DomainA] {

    override def endTransaction() {
      notify_endTransaction()
    }

    def updated(oldV: DomainA, newV: DomainA) {
      if (oldV == newV)
        return
      // if we assume that update means all instances are updated this is correct
      notify_updated(oldV, newV)
    }

    def removed(v: DomainA) {
      val oldCount = left.count(v) + 1
      val rightCount = right.count(v)
      if (rightCount < oldCount - 1) {
        notify_removed(v)
      }
    }

    def removedAll(vs: Seq[DomainA]) {
      val removed = vs filter {v =>
        val oldCount = left.count(v) + 1
        val rightCount = right.count(v)
        rightCount < oldCount - 1
      }
      notify_removedAll(removed)
    }

    def added(v: DomainA) {
      val oldCount = left.count(v) - 1
      val rightCount = right.count(v)
      if (rightCount < oldCount + 1) {
        notify_added(v)
      }
    }

    def addedAll(vs: Seq[DomainA]) {
      val added = vs filter {v =>
        val oldCount = left.count(v) - 1
        val rightCount = right.count(v)
        rightCount < oldCount + 1
      }
      notify_addedAll(added)
    }

  }

  object RightObserver extends Observer[DomainB] {

    override def endTransaction() {
      notify_endTransaction()
    }

    // update operations on right relation
    def updated(oldV: DomainB, newV: DomainB) {
      if (oldV == newV)
        return
      // if we assume that update means all instances are updated this is correct
      notify_updated(oldV, newV)
    }

    def removed(v: DomainB) {
      val leftCount = left.count(v)
      val oldCount = right.count(v) + 1
      if (leftCount < oldCount - 1) {
        notify_removed(v)
      }
    }

    def removedAll(vs: Seq[DomainB]) {
      val removed = vs filter {v =>
        val leftCount = left.count(v)
        val oldCount = right.count(v) + 1
        leftCount < oldCount - 1
      }
      notify_removedAll(removed)
    }

    def added(v: DomainB) {
      val leftCount = left.count(v)
      val oldCount = right.count(v) - 1
      if (leftCount < oldCount + 1) {
        notify_added(v)
      }
    }

    def addedAll(vs: Seq[DomainB]) {
      val added = vs filter {v =>
        val leftCount = left.count(v)
        val oldCount = right.count(v) - 1
        leftCount < oldCount + 1
      }
      notify_addedAll(added)
    }
  }

}
