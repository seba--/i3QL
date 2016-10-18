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
 *  Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  Neither the name of the Software Technology Group or Technische
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
package idb

import idb.observer.NotifyObservers

/**
 *
 * A table is a base relation for providing data.
 * Tables are invariant in the domain (V), since once they are created they MUST supply objects of the specified
 * elements.
 * Otherwise, runtime errors can occur for already constructed queries.
 * For example, with (Student <: Person) :
 * r1 : Table[Student]
 * r2 : Table[Person]
 * q = SELECT (student => student.grades) FROM r1
 * r2 = r1
 * r2.add(new Person("..."))
 *
 * @author Ralf Mitschke
 */
trait Table[V]
  extends Relation[V]
  with NotifyObservers[V] {
  def update(oldV: V, newV: V) {
    notify_updated(oldV, newV)
  }

  def ~=(vs: (V, V)): Table[V] = {
    update(vs._1, vs._2)
    this
  }

  def remove(v: V) {
    notify_removed(v)
  }

  def removeAll(vs: Seq[V]) {
    notify_removedAll(vs)
  }

  def -=(v: V): Table[V] = {
    remove(v)
    this
  }

  def --=(vs: Seq[V]): Table[V] = {
    removeAll(vs)
    this
  }

  def add(v: V) {
    notify_added(v)
  }

  def addAll(vs: Seq[V]) {
    notify_addedAll(vs)
  }

  def +=(v: V): Table[V] = {
    add(v)
    this
  }

  def ++=(vs: Seq[V]): Table[V] = {
    addAll(vs)
    this
  }


  override def foreach[T](f: (V) => T) {}

  def foreachWithCount[T](f: (V, Int) => T) {}

  def contains(element: V): Boolean = false

  def count(element: V): Int = 0

  def size: Int = 0

  override def children() = Nil

  override def prettyprint(implicit prefix: String) = prefix + this
}