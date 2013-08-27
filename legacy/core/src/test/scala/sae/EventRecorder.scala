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
package sae

import deltas.{Update, Deletion, Addition}

/**
 *
 * This observer records all events that are passed to it.
 *
 * @author Ralf Mitschke
 */
class EventRecorder[-V] extends Observer[V]
{

    import EventRecorder._

    private var eventQueue: List[ObserverEvent] = Nil

    def events = eventQueue

    def eventsChronological = eventQueue.reverse

    def clearEvents() {
        eventQueue = Nil
    }

    def updated(oldV: V, newV: V) {
        eventQueue ::= UpdateEvent (oldV, newV)
    }

    def removed(v: V) {
        eventQueue ::= RemoveEvent (v)
    }

    def added(v: V) {
        eventQueue ::= AddEvent (v)
    }

    def added[U <: V](addition: Addition[U]) {
        for (i <- (0 to addition.count)) {
            eventQueue ::= AddEvent (addition.value)
        }
    }

    def deleted[U <: V](deletion: Deletion[U]) {
        for (i <- (0 to deletion.count)) {
            eventQueue ::= RemoveEvent (deletion.value)
        }
    }

    def updated[U <: V](update: Update[U]) {
        for (i <- (0 to update.count)) {
            eventQueue ::= UpdateEvent (update.oldV, update.newV)
        }
    }

    def modified[U <: V](additions: Set[Addition[U]], deletions: Set[Deletion[U]], updates: Set[Update[U]]) {
        additions.foreach (added[U])
        deletions.foreach (deleted[U])
        updates.foreach (updated[U])
    }
}

object EventRecorder
{

    trait ObserverEvent

    case class AddEvent[T](value: T) extends ObserverEvent

    case class RemoveEvent[T](value: T) extends ObserverEvent

    case class UpdateEvent[T](oldValue: T, newValue: T) extends ObserverEvent

}