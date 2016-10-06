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

import idb.{Relation, MaterializedView}
import idb.operators.Intersection
import idb.observer.{NotifyObservers, Observer, Observable}


/**
 * This intersection operation has multiset semantics for elements
 *
 */
class IntersectionView[Domain](val left: MaterializedView[Domain],
                               val right: MaterializedView[Domain],
							   override val isSet : Boolean)
    extends Intersection[Domain]
	with NotifyObservers[Domain]
{

    left addObserver LeftObserver

    right addObserver RightObserver

	override def lazyInitialize() {

	}

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
        left.foreach (
            (v: Domain) =>
            {
                val min = scala.math.min (left.count (v), right.count (v))
                var i = 0
                while (i < min) {
                    f (v)
                    i += 1
                }
            }
        )
    }

    object LeftObserver extends Observer[Domain]
    {

		override def endTransaction() {
			notify_endTransaction()
		}

        /**
         * We have just added to left (left.count(v) >= 1).
         * While we add elements to left and
         * have less than or equal elements compared to right, we generate new duplicates.
         *
         */
		override def added(v: Domain) {
            if (left.count (v) <= right.count (v)) {
                notify_added (v)
            }
        }

      override def addedAll(vs: Seq[Domain]) {
        val added = vs filter (v => left.count (v) <= right.count (v))
        notify_addedAll(added)
      }


      /**
         * as long as left has more elements than right we only remove excess duplicates
         */
		override def removed(v: Domain) {
            if (left.count (v) < right.count (v)) {
				notify_removed (v)
            }
        }

      override def removedAll(vs: Seq[Domain]) {
        val removed = vs filter (v => left.count (v) < right.count (v))
        notify_removedAll(removed)
      }

		override def updated(oldV: Domain, newV: Domain) {
            val oldDef = right.isDefinedAt (oldV)
            val newDef = right.isDefinedAt (newV)
            if (oldDef && newDef) {
				notify_updated (oldV, newV)
                return
            }
            if (oldDef) {
				notify_removed (oldV)
            }

            if (newDef) {
				notify_added (newV)
            }
        }
    }


    object RightObserver extends Observer[Domain]
    {

		override def endTransaction() {
			notify_endTransaction()
		}

        /**
         * We have just added to right (right.count(v) >= 1).
         * While we add elements to left and
         * have less than or equal elements compared to right, we generate new duplicates.
         *
         */
		override def added(v: Domain) {
            if (right.count (v) <= left.count (v)) {
				notify_added (v)
            }
        }

      override def addedAll(vs: Seq[Domain]) {
        val added = vs filter (v => right.count (v) <= left.count (v))
        notify_addedAll(added)
      }

        /**
         * as long as left has more elements than right we only remove excess duplicates
         */
		override def removed(v: Domain) {
            if (right.count (v) < left.count (v)) {
				notify_removed (v)
            }
        }

      override def removedAll(vs: Seq[Domain]) {
        val removed = vs filter (v => right.count (v) < left.count (v))
        notify_removedAll(removed)
      }

		override def updated(oldV: Domain, newV: Domain) {

            val oldDef = left.isDefinedAt (oldV)
            val newDef = left.isDefinedAt (newV)
            if (oldDef && !newDef) {
                // the element was in A but will not be in A and in B thus it is not be in the intersection
				notify_removed (oldV)
            }

            if (!oldDef && newDef) {
                // the element was not in A but oldV will  be in B thus the oldV is added to the intersection
				notify_added (newV)
            }

        }
    }

}

