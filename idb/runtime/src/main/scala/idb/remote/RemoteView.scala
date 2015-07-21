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
package idb.remote

import akka.actor._
import idb.Relation
import idb.observer._


class RemoteViewActor[V](view: RemoteView[V]) extends Actor {
  override def receive = {
    case Added(v: V) => view.notify_added(v)
    case Removed(v: V) => view.notify_removed(v)
    case Updated(oldV: V, newV: V) => view.notify_updated(oldV, newV)
    case AddedAll(vs: Seq[V]) => view.notify_addedAll(vs)
    case RemovedAll(vs: Seq[V]) => view.notify_removedAll(vs)
    case EndTransaction => view.notify_endTransaction()
  }
}

/**
 *
 * A selection view is a selection that stores no tuples by itself.
 * All data is passed along if it passes the filter.
 *
 * The selection automatically registers as an observer of the relation upon construction
 *
 * @author Ralf Mitschke
 */
case class RemoteView[Domain](rel: Relation[Domain], actorSystem: ActorSystem)
  extends Relation[Domain]
  with NotifyObservers[Domain] {

  val actorRef: ActorRef = actorSystem.actorOf(Props(new RemoteViewActor(this)))

  rel addObserver (new SentToRemote(actorRef))

  def isSet = rel.isSet

  protected def lazyInitialize() {
    /* do nothing */
  }

  protected def children: Seq[Relation[_]] = Nil
  override protected def childObservers(o: Observable[_]): Seq[Observer[_]] = Nil

  /**
   * Applies f to all elements of the view.
   */
  def foreach[T](f: (Domain) => T) { }

  override def notify_added(v: Domain) = super.notify_added(v)

  override def notify_addedAll(vs: Seq[Domain]) = super.notify_addedAll(vs)

  override def notify_removed(v: Domain) = super.notify_removed(v)

  override def notify_removedAll(vs: Seq[Domain]) = super.notify_removedAll(vs)

  override def notify_updated(oldV: Domain, newV: Domain) = super.notify_updated(oldV, newV)

  override def notify_endTransaction() = super.notify_endTransaction()
}
