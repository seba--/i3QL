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

import idb.Relation
import idb.operators.Projection
import idb.observer.{Observable, NotifyObservers, Observer}


/**
 *
 *
 *
 * The bag projection has the usual SQL meaning of a projection
 * The projection is always self maintained and requires no additional data apart from the provided delta.
 *
 * @author Ralf Mitschke
 *
 */
class ProjectionSetRetainingView[Domain, Range](val relation: Relation[Domain],
                                                val projection: Domain => Range,
                                                override val isSet: Boolean)
  extends Projection[Domain, Range]
  with Observer[Domain]
  with NotifyObservers[Range] {
  relation addObserver this

  override protected def childObservers(o: Observable[_]): Seq[Observer[_]] = {
    if (o == relation) {
      return List(this)
    }
    Nil
  }

  /**
   * Applies f to all elements of the view.
   */
  override def foreach[T](f: (Range) => T) {
    relation.foreach((v: Domain) => f(projection(v)))
  }

  override protected[idb] def resetInternal(): Unit = {

  }

  override def updated(oldV: Domain, newV: Domain) {
    notify_updated(projection(oldV), projection(newV))
  }

  override def removed(v: Domain) {
    notify_removed(projection(v))
  }

  override def removedAll(vs: Seq[Domain]) {
    val removed = vs map (projection(_))
    notify_removedAll(removed)
  }

  override def added(v: Domain) {
    notify_added(projection(v))
  }

  override def addedAll(vs: Seq[Domain]) {
    val added = vs map (projection(_))
    notify_addedAll(added)
  }
}

