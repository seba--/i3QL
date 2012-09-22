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
package sae.operators.impl

import sae.{Observer, Observable, OLDMaterializedView, IndexedViewOLD}
import scala.Some

/**
 * The difference operation in our algebra has non-distinct bag semantics
 *
 * This class can compute the difference efficiently by relying on indices from the underlying relations.
 * The operation itself does not store any intermediate results.
 * Updates are computed based on indices and foreach is recomputed on every call.
 *
 * The size is cached internally to avoid recomputations
 */
class BagDifference[Domain <: AnyRef]
(
    val left: IndexedViewOLD[Domain],
    val right: IndexedViewOLD[Domain]
    )
    extends Difference[Domain]
    with OLDMaterializedView[Domain]
{
    left addObserver LeftObserver

    right addObserver RightObserver

    override protected def children = List (left, right)

    override protected def childObservers(o: Observable[_]): Seq[Observer[_]] = {
        if (o == left) {
            return List (LeftObserver, leftIndex)
        }
        if (o == right) {
            return List (RightObserver, rightIndex)
        }
        Nil
    }

    private val leftIndex = left.index (identity)

    private val rightIndex = right.index (identity)

    private var cached_size = 0

    def lazyInitialize {
        if (initialized) return
        leftIndex.foreachKey (
            (v: Domain) => {
                var difference = leftIndex.elementCountAt (v) - rightIndex.elementCountAt (v)
                if (difference > 0) {
                    cached_size += difference
                }
            }
        )
        initialized = true
    }

    // TODO the materialized foreach already has all elements of the underlying relation
    // if another view calls foreach during lazyInitialize at the time of an add event from this operator,
    // then elements may be added twice.
    // Note: The deeper issue is, what is the consistent state for operators before they fire events.
    // currently they have the values of the result minus the delta values they currently process in an event
    def materialized_foreach[T](f: (Domain) => T) {
        leftIndex.foreachKey (
            (v: Domain) => {
                var difference = leftIndex.elementCountAt (v) - rightIndex.elementCountAt (v)
                while (difference > 0) {
                    f (v)
                    difference = difference - 1
                }
            }
        )
    }


    protected def materialized_singletonValue = left.singletonValue match {
        case None => None
        case singletonValue@Some (v) => {
            if (!rightIndex.isDefinedAt (v))
                singletonValue
            else
                None
        }
    }

    protected def materialized_size = this.cached_size

    protected def materialized_contains(v: Domain) = left.contains (v) && !right.contains (v)

    object LeftObserver extends Observer[Domain]
    {
        def updated(oldV: Domain, newV: Domain) {
            // we are notified after the update, hence the leftIndex will be updated to newV
            var oldCount = leftIndex.elementCountAt (newV) - rightIndex.elementCountAt (oldV)
            var newCount = leftIndex.elementCountAt (newV) - rightIndex.elementCountAt (newV)
            if (!initialized) {
                lazyInitialize
                if (oldCount > 0) cached_size -= oldCount
                if (newCount > 0) cached_size += newCount
            }

            if (oldCount == newCount) {
                element_updated (oldV, newV)
                return
            }
            while (oldCount > 0)
            {
                element_removed (oldV)
                cached_size -= 1
                oldCount -= 1
            }
            while (newCount > 0)
            {
                element_added (oldV)
                cached_size += 1
                newCount -= 1
            }
        }

        def removed(v: Domain) {
            if (!initialized) {
                lazyInitialize
                // add the removed element
                if (leftIndex.elementCountAt (v) >= rightIndex.elementCountAt (v)) cached_size += 1
            }

            // check that this was a removal where we still had more elements than right side
            if (leftIndex.elementCountAt (v) >= rightIndex.elementCountAt (v)) {
                element_removed (v)
                cached_size -= 1
            }
        }

        def added(v: Domain) {
            if (!initialized) {
                lazyInitialize
                // remove the added element
                if (leftIndex.elementCountAt (v) > rightIndex.elementCountAt (v)) cached_size -= 1
            }

            // check that this was an addition where we did not have less elements than right side
            if (leftIndex.elementCountAt (v) > rightIndex.elementCountAt (v)) {
                element_added (v)
                cached_size += 1
            }
        }
    }

    object RightObserver extends Observer[Domain]
    {
        // update operations on right relation
        def updated(oldV: Domain, newV: Domain) {
            // we are notified after the update, hence the rightIndex will be updated to newV
            var oldCount = if (leftIndex.elementCountAt (oldV) >= rightIndex.elementCountAt (newV)) rightIndex.elementCountAt (newV) else leftIndex.elementCountAt (oldV)
            var newCount = if (leftIndex.elementCountAt (newV) >= rightIndex.elementCountAt (newV)) rightIndex.elementCountAt (newV) else leftIndex.elementCountAt (oldV)
            if (!initialized) {
                lazyInitialize
                if (oldCount > 0) cached_size += oldCount
                if (newCount > 0) cached_size -= newCount
            }
            if (oldCount == newCount) {
                element_updated (oldV, newV)
                return
            }
            while (oldCount > 0)
            {
                element_added (oldV)
                cached_size += 1
                oldCount -= 1
            }
            while (newCount > 0)
            {
                element_removed (oldV)
                cached_size -= 1
                newCount -= 1
            }
        }

        def removed(v: Domain) {
            if (!initialized) {
                lazyInitialize
                // remove the removed element
                if (leftIndex.elementCountAt (v) > rightIndex.elementCountAt (v)) cached_size -= 1
            }

            // check that this was the last removal of an element not in left side
            if (leftIndex.elementCountAt (v) > rightIndex.elementCountAt (v)) {
                element_added (v)
                cached_size += 1
            }
        }

        def added(v: Domain) {
            if (!initialized) {
                lazyInitialize
                // add the added element
                if (leftIndex.elementCountAt (v) >= rightIndex.elementCountAt (v)) cached_size += 1
            }

            // check that this was an addition where we have more or equal amount of elements compared to left side
            if (leftIndex.elementCountAt (v) >= rightIndex.elementCountAt (v)) {
                element_removed (v)
                cached_size -= 1
            }
        }
    }

}
