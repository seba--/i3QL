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

import idb.MaterializedView
import idb.operators.Difference
import idb.observer.{Observer, Observable, NotifyObservers}


/**
 * The difference operation in our algebra has non-distinct bag semantics
 *
 * This class can compute the difference efficiently by relying on indices from the underlying relations.
 * The operation itself does not store any intermediate results.
 * Updates are computed based on indices and foreach is recomputed on every call.
 *
 */
class DifferenceOnMaterializedView[Domain](val left: MaterializedView[Domain],
                                           val right: MaterializedView[Domain],
										   override val isSet : Boolean)
    extends Difference[Domain]
	with NotifyObservers[Domain]
{
    left addObserver LeftObserver

    right addObserver RightObserver

	override protected def lazyInitialize() {}

    override protected def childObservers(o: Observable[_]): Seq[Observer[_]] = {
        if (o == left) {
            return List (LeftObserver)
        }
        if (o == right) {
            return List (RightObserver)
        }
        Nil
    }

	override protected def resetInternal(): Unit = ???

	/**
     * Applies f to all elements of the view.
     */
    def foreach[T](f: (Domain) => T) {
        left.foreachWithCount (
            (v: Domain, leftCount: Int) =>
            {
                val rightCount = right.count (v)
                val max = scala.math.max (0, leftCount - rightCount)
                var i = 0
                while (i < max) {
                    f (v)
                    i += 1
                }
            }
        )
    }

    object LeftObserver extends Observer[Domain]
    {

        override def endTransaction() {
            notify_endTransaction ()
        }

        def updated(oldV: Domain, newV: Domain) {
            // we are notified after the update, hence the left will be updated to newV
            var oldCount = left.count (newV) - right.count (oldV)
            var newCount = left.count (newV) - right.count (newV)
            if (oldCount == newCount) {
                notify_updated (oldV, newV)
                return
            }
            while (oldCount > 0)
            {
                notify_removed (oldV)
                oldCount -= 1
            }
            while (newCount > 0)
            {
                notify_added (newV)
                newCount -= 1
            }
        }

        def removed(v: Domain) {
            // check that this was a removal where we still had more elements than right side
            if (left.count (v) >= right.count (v)) {
                notify_removed (v)
            }
        }

      def removedAll(vs: Seq[Domain]) {
        // check that this was a removal where we still had more elements than right side
        val removed = vs filter (v => left.count (v) >= right.count (v))
        notify_removedAll(removed)
      }

      def added(v: Domain) {
            // check that this was an addition where we did not have less elements than right side
            if (left.count (v) > right.count (v)) {
                notify_added (v)
            }
        }

      def addedAll(vs: Seq[Domain]) {
        // check that this was an addition where we did not have less elements than right side
        val added = vs filter (v => left.count (v) > right.count (v))
        notify_addedAll(added)
      }

    }

    object RightObserver extends Observer[Domain]
    {

        override def endTransaction() {
            notify_endTransaction ()
        }

        // update operations on right relation
        def updated(oldV: Domain, newV: Domain) {
            // we are notified after the update, hence the right will be updated to newV
            var oldCount = if (left.count (oldV) >= right.count (newV)) right.count (newV) else left.count (oldV)
            var newCount = if (left.count (newV) >= right.count (newV)) right.count (newV) else left.count (newV)
            if (oldCount == newCount) {
                notify_updated (oldV, newV)
                return
            }
            while (oldCount > 0)
            {
                notify_added (oldV)
                oldCount -= 1
            }
            while (newCount > 0)
            {
                notify_removed (newV)
                newCount -= 1
            }
        }

        def removed(v: Domain) {
            // check that this was the last removal of an element not in left side
            if (left.count (v) > right.count (v)) {
                notify_added (v)
            }
        }

      def removedAll(vs: Seq[Domain]) {
        // check that this was the last removal of an element not in left side
        val added = vs filter (v => left.count (v) > right.count (v))
        notify_addedAll(added)
      }

        def added(v: Domain) {
            // check that this was an addition where we have more or equal amount of elements compared to left side
            if (left.count (v) >= right.count (v)) {
                notify_removed (v)
            }
        }

      def addedAll(vs: Seq[Domain]) {
        // check that this was an addition where we have more or equal amount of elements compared to left side
        val removed = vs filter (v => left.count (v) >= right.count (v))
        notify_removedAll(removed)
      }
    }


}
