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

import idb.operators.Selection
import idb.observer.{NotifyObservers, Observable, Observer}
import idb.Relation

/**
 *
 * A selection view is a selection that stores no tuples by itself.
 * All data is passed along if it passes the filter.
 *
 * The selection automatically registers as an observer of the relation upon construction
 *
 * @author Ralf Mitschke
 */
case class SelectionView[Domain](
	relation: Relation[Domain],
	filter: Domain => Boolean,
	isSet: Boolean
)
  extends Selection[Domain]
  with Observer[Domain]
  with NotifyObservers[Domain] {

  relation addObserver this


  protected def lazyInitialize() {
    /* do nothing */
  }


  override protected def childObservers(o: Observable[_]): Seq[Observer[_]] = {
    if (o == relation) {
      return List(this)
    }
    Nil
  }

  override def endTransaction() {
    notify_endTransaction()
  }

  /**
   * Applies f to all elements of the view.
   */
  def foreach[T](f: (Domain) => T) {
    relation.foreach(
      (v: Domain) => if (filter(v)) {
        f(v)
      }
    )
  }

  def updated(oldV: Domain, newV: Domain) {
    val oldVPasses = applyFilter(oldV)
    val newVPasses = applyFilter(newV)
    if (oldVPasses && newVPasses) {
      notify_updated(oldV, newV)
    }
    else {
      // only one of the elements complies to the filter
      if (oldVPasses) {
        notify_removed(oldV)
      }
      if (newVPasses) {
        notify_added(newV)
      }
    }
  }

  def removed(v: Domain) {
    if (applyFilter(v)) {
      notify_removed(v)
    }
  }

  def removedAll(vs: Seq[Domain]) {
    val removed = vs filter (applyFilter(_))
    notify_removedAll(removed)
  }


  def added(v: Domain) {
    if (applyFilter(v)) {
      notify_added(v)
    }
  }

  def addedAll(vs: Seq[Domain]) {
    val added = vs filter (applyFilter(_))
    notify_addedAll(added)
  }

  private def applyFilter(v: Domain): Boolean = {
    try {
      filter(v)
    } catch {
      case e: IndexOutOfBoundsException => false
      case e: ClassCastException => false
      case e: NoSuchElementException => false
    }
  }


}
