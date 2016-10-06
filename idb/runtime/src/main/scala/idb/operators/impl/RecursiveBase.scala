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
import idb.operators.Recursive
import idb.observer.NotifyObservers


/**
 *
 * @author Ralf Mitschke
 *
 */

case class RecursiveBase[Domain](relation: Relation[Domain],
                                 override val isSet: Boolean)
  extends Recursive[Domain]
  with NotifyObservers[Domain] {
  relation.addObserver(this)

  var nextElement: Option[Domain] = None

  var recursiveNotification = false

  override def lazyInitialize() {

  }

  override def endTransaction() {
    notify_endTransaction()
  }

  override protected def resetInternal(): Unit = ???


  override def added(v: Domain) {
    if (recursiveNotification) {
      nextElement = Some(v)
      return
    }

    recursiveNotification = true
    notify_added(v)
    //println("Base -> Added " + v)

    while (nextElement.isDefined) {
      val next = nextElement.get
      nextElement = None
      notify_added(next)
      //println("Base -> Added " + next)
    }
    recursiveNotification = false
  }

  // TODO more efficient version possible?
  override def addedAll(vs: Seq[Domain]) {
    println(s"WARNING: Running slow incremental recursive add, size ${vs.size}")
    vs foreach (added(_))
  }

  override def removed(v: Domain) {
    if (recursiveNotification) {
      nextElement = Some(v)
      return
    }

    recursiveNotification = true
    notify_removed(v)
    //println("Base -> Removed")

    while (nextElement.isDefined) {
      val next = nextElement.get
      nextElement = None
      notify_removed(next)
    }
    recursiveNotification = false
  }

  // TODO more efficient version possible?
  override def removedAll(vs: Seq[Domain]) {
    println(s"WARNING: Running slow incremental recursive remove, size ${vs.size}")
    vs foreach (removed(_))
  }

  override def updated(oldV: Domain, newV: Domain) {
    throw new UnsupportedOperationException
  }

}
