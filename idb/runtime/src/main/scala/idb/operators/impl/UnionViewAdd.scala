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
import idb.Relation
import idb.observer.{Observable, NotifyObservers, Observer}


/**
 * A self maintained union, that produces count(A) + count(B) duplicates for underlying relations A and B
 */
class UnionViewAdd[Range, DomainA <: Range, DomainB <: Range](val left: Relation[DomainA],
                                                              val right: Relation[DomainB],
                                                              override val isSet: Boolean)
  extends Union[Range, DomainA, DomainB]
  with NotifyObservers[Range] {

  left addObserver LeftObserver
  right addObserver RightObserver

  override protected def resetInternal(): Unit = {

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

  /**
   * Applies f to all elements of the view.
   */
  def foreach[T](f: (Range) => T) {
    left.foreach(f)
    right.foreach(f)
  }

  object LeftObserver extends Observer[DomainA] {

    override def updated(oldV: DomainA, newV: DomainA) {
      removed(oldV)
      added(newV)
    }

    override def removed(v: DomainA) {
      notify_removed(v)
    }

    override def removedAll(vs: Seq[DomainA]): Unit = {
      notify_removedAll(vs)
    }

    override def added(v: DomainA) {
      notify_added(v)
    }

    override def addedAll(vs: Seq[DomainA]) {
      notify_addedAll(vs)
    }


  }

  object RightObserver extends Observer[DomainB] {

    override def updated(oldV: DomainB, newV: DomainB) {
      removed(oldV)
      added(newV)
    }

    override def removed(v: DomainB) {
      notify_removed(v)
    }

    override def removedAll(vs: Seq[DomainB]) {
      notify_removedAll(vs)
    }

    override def added(v: DomainB) {
      notify_added(v)
    }

    override def addedAll(vs: Seq[DomainB]) {
      notify_addedAll(vs)
    }

  }

}
