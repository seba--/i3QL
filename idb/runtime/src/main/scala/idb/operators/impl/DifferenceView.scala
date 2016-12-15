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
import idb.operators.Difference
import idb.observer.{NotifyObservers, Observer, Observable}


/**
 * The difference operation in our algebra has non-distinct bag semantics
 *
 * This class can compute the difference efficiently by relying on indices from the underlying relations.
 * The operation itself does not store any intermediate results.
 * Updates are computed based on indices and foreach is recomputed on every call.
 *
 *
 * The difference can be update by the expression:
 * [(Δright- ∪ Δleft+) - (Δleft- ∪ Δright+)] - (right - left)
 */
class DifferenceView[Domain](val left: Relation[Domain],
                             val right: Relation[Domain],
							 override val isSet : Boolean)
    extends Difference[Domain] with NotifyObservers[Domain]
{
    left addObserver LeftObserver

    right addObserver RightObserver

    import com.google.common.collect.HashMultiset

    private val leftDiffRight: HashMultiset[Domain] = HashMultiset.create[Domain]()

    private val rightDiffLeft: HashMultiset[Domain] = HashMultiset.create[Domain]()

	override protected def childObservers(o: Observable[_]): Seq[Observer[_]] = {
        if (o == left) {
            return List (LeftObserver)
        }
        if (o == right) {
            return List (RightObserver)
        }
        Nil
    }

    override protected[idb] def resetInternal(): Unit = {
        leftDiffRight.clear()
        rightDiffLeft.clear()
    }

    /**
     * Applies f to all elements of the view with their counts
     */
    def foreachWithCount[T](f: (Domain, Int) => T) {}

    def isDefinedAt(v: Domain) = false

    /**
     * Returns the count for a given element.
     * In case an add/remove/update event is in progression, this always returns the
     */
    def elementCountAt[T >: Domain](v: T) = 0

    /**
     * Applies f to all elements of the view.
     */
    def foreach[T](f: (Domain) => T) {
        val it = leftDiffRight.iterator ()
        while (it.hasNext) {
            val v = it.next ()
            f (v)
        }
    }


    /**
     * [(Δright- ∪ Δleft+) - (Δleft- ∪ Δright+)] - (right - left)
     */
    object LeftObserver extends Observer[Domain]
    {


        /**
         * Δleft+ - (right - left)
         */
        def added(v: Domain) {
            if (rightDiffLeft.count (v) == 0) {
                leftDiffRight.add (v)
				notify_added (v)
            }
            else
            {
                rightDiffLeft.remove (v)
            }
        }

      def addedAll(vs: Seq[Domain]) {
        val added = vs filter (v =>
          if (rightDiffLeft.count (v) == 0) {
            leftDiffRight.add(v)
            true
          } else {
            rightDiffLeft.remove(v)
            false
          })
        notify_addedAll(added)
      }

        /**
         * - Δleft-  - (right - left)
         */
        def removed(v: Domain) {
            if (leftDiffRight.count (v) > 0) {
                leftDiffRight.remove (v)
				notify_removed (v)
            }
            else
            {
                // if it was not in the leftDiffRight newResult it was filtered by being in right
                rightDiffLeft.add (v)
            }
        }

      def removedAll(vs: Seq[Domain]) {
        val removed = vs filter (v =>
          if (leftDiffRight.count (v) > 0) {
            leftDiffRight.remove (v)
            true
          }
          else
          {
            // if it was not in the leftDiffRight newResult it was filtered by being in right
            rightDiffLeft.add (v)
            false
          })
        notify_removedAll(removed)
      }

      def updated(oldV: Domain, newV: Domain) {
            var count = leftDiffRight.count (oldV) + rightDiffLeft.count (oldV)
            if (count == 0) {
                added (newV)
            }
            while (count > 0)
            {
                removed (oldV)
                added (newV)
                count -= 1
            }
        }

    }

    object RightObserver extends Observer[Domain]
    {

        def added(v: Domain) {
            if (leftDiffRight.count (v) > 0) {
                leftDiffRight.remove (v)
				notify_removed (v)
            }
            else
            {
                rightDiffLeft.add (v)
            }
        }

      def addedAll(vs: Seq[Domain]) {
        val removed = vs filter (v =>
          if (leftDiffRight.count (v) > 0) {
            leftDiffRight.remove (v)
            true
          }
          else
          {
            rightDiffLeft.add (v)
            false
          })
        notify_removedAll(removed)
      }

      def removed(v: Domain) {
            if (rightDiffLeft.count (v) == 0) {
                leftDiffRight.add (v)
				notify_added (v)
            }
            else
            {
                rightDiffLeft.remove (v)
            }
        }

      def removedAll(vs: Seq[Domain]) {
        val added = vs filter (v =>
          if (rightDiffLeft.count (v) == 0) {
            leftDiffRight.add (v)
          }
          else
          {
            rightDiffLeft.remove (v)
          })
        notify_addedAll(added)
      }

      def updated(oldV: Domain, newV: Domain) {
            var count = leftDiffRight.count (oldV) + rightDiffLeft.count (oldV)
            if (count == 0) {
                added (newV)
            }
            while (count > 0)
            {
                removed (oldV)
                added (newV)
                count -= 1
            }
        }

    }


}
