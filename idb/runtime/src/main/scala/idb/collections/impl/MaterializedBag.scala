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
package idb.collections.impl

import idb.Relation
import idb.observer.{Observable, Observer}
import idb.collections.Bag


/**
 * A collection that materializes all data from the underlying relation into a bag
 */
class MaterializedBag[V](val relation: Relation[V])
  extends Bag[V]
  with Observer[V] {

  relation addObserver this

  override def children() = List(relation)

  override protected def childObservers(o: Observable[_]): Seq[Observer[_]] = {
    if (o == relation) {
      return List(this)
    }
    Nil
  }

  def updated(oldV: V, newV: V) {
    update_element(oldV, newV)
  }

  def removed(v: V) {
    this -= v
  }

  def added(v: V) {
    this += v
  }

  def addedAll(vs: Seq[V]): Unit = {
    this ++= vs
  }

  def removedAll(vs: Seq[V]): Unit = {
    this --= vs
  }

  override def prettyprint(implicit prefix: String) = prefix +
    s"MaterializedBag(${nested(relation)})"

  override protected def resetInternal(): Unit = clear()
}
