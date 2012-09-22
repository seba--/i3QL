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
 * This intersection operation has non-distinct bag semantics
 *
 * This class can compute the intersection efficiently by relying on indices from the underlying relations.
 * The operation itself does not store any intermediate results.
 * Updates are computed based on indices and foreach is recomputed on every call.
 *
 * The size is cached internally to avoid recomputations
 */
class BagIntersection[Domain <: AnyRef]
(
    val left: IndexedViewOLD[Domain],
    val right: IndexedViewOLD[Domain]
    )
    extends Intersection[Domain]
    with OLDMaterializedView[Domain]
{

    val leftIndex = left.index (identity)

    val rightIndex = right.index (identity)

    leftIndex addObserver LeftObserver

    rightIndex addObserver RightObserver

    override protected def children = List (left.asInstanceOf[Observable[AnyRef]], right)

    var cached_size = 0

    def lazyInitialize {
        if (initialized) return
        leftIndex.foreachKey ((key: Domain) => {
            if (rightIndex.isDefinedAt (key)) {
                // we compute the min over the two counts
                cached_size += scala.math.min (leftIndex.elementCountAt (key), rightIndex.elementCountAt (key))
            }
        }
        )
        initialized = true
    }

    def materialized_foreach[T](f: (Domain) => T) {
        leftIndex.foreachKey ((key: Domain) => {
            if (rightIndex.isDefinedAt (key)) {
                // we compute the min over the two counts
                var count = scala.math.min (leftIndex.elementCountAt (key), rightIndex.elementCountAt (key))
                while (count > 0) {
                    f (key) // the keys and elements are the same as we used identity as key function
                    count -= 1
                }
            }
        }
        )
    }


    protected def materialized_singletonValue = left.singletonValue match {
        case None => None
        case singletonValue@Some (v) => {
            if (rightIndex.elementCountAt (v) == 1)
                singletonValue
            else
                None
        }
    }

    protected def materialized_size = this.cached_size


    protected def materialized_contains(v: Domain) = left.contains (v) && right.contains (v)


    object LeftObserver extends Observer[(Domain, Domain)]
    {

        /**
         * We have just added to left (leftIndex.elementCountAt(v) >= 1).
         * While we add elements to left and
         * have less than or equal elements compared to right, we generate new duplicates.
         *
         */
        def added(kv: (Domain, Domain)) {
            val v = kv._1
            /*
            println("+" + v)
            println("left : " + leftIndex.elementCountAt(v))
            println("right: " + rightIndex.elementCountAt(v))
            */
            if (leftIndex.elementCountAt (v) <= rightIndex.elementCountAt (v)) {
                element_added (v)
                cached_size += 1
            }
            initialized = true
        }

        /**
         * as long as left has more elements than right we only remove excess duplicates
         */
        def removed(kv: (Domain, Domain)) {
            val v = kv._1
            /*
            println("-" + v)
            println("left : " + leftIndex.elementCountAt(v) )
            println("right: " + rightIndex.elementCountAt(v) )
            */
            if (leftIndex.elementCountAt (v) < rightIndex.elementCountAt (v)) {
                element_removed (v)
                cached_size -= 1
            }
            initialized = true
        }

        def updated(oldKV: (Domain, Domain), newKV: (Domain, Domain)) {
            val oldV = oldKV._1
            val newV = newKV._1
            val oldDef = rightIndex.isDefinedAt (oldV)
            val newDef = rightIndex.isDefinedAt (newV)
            if (oldDef && newDef) {
                element_updated (oldV, newV)
                return
            }
            if (oldDef) {
                element_removed (oldV)
                cached_size -= 1
            }

            if (newDef) {
                element_added (newV)
                cached_size += 1
            }
            initialized = true
        }
    }


    object RightObserver extends Observer[(Domain, Domain)]
    {

        /**
         * We have just added to right (leftIndex.elementCountAt(v) >= 1). While we add elements to left and
         * have less than or equal elements compared to right, we generate new duplicates.
         *
         */
        def added(kv: (Domain, Domain)) {
            val v = kv._1
            /*
            println("+" + v)
            println("left : " + leftIndex.elementCountAt(v) )
            println("right: " + rightIndex.elementCountAt(v) )
            */
            if (rightIndex.elementCountAt (v) <= leftIndex.elementCountAt (v)) {
                element_added (v)
                cached_size += 1
            }

            initialized = true
        }

        /**
         * as long as left has more elements than right we only remove excess duplicates
         */
        def removed(kv: (Domain, Domain)) {
            val v = kv._1
            /*
            println("-" + v)
            println("left : " + leftIndex.elementCountAt(v) )
            println("right: " + rightIndex.elementCountAt(v) )
            */
            if (rightIndex.elementCountAt (v) < leftIndex.elementCountAt (v)) {
                element_removed (v)
                cached_size -= 1
            }
            initialized = true
        }

        def updated(oldKV: (Domain, Domain), newKV: (Domain, Domain)) {
            val oldV = oldKV._1
            val newV = newKV._1
            val oldDef = leftIndex.isDefinedAt (oldV)
            val newDef = leftIndex.isDefinedAt (newV)
            if (oldDef && !newDef) {
                // the element was in A but will not be in A and in B thus it is not be in the intersection
                element_removed (newV)
                cached_size -= 1
            }

            if (!oldDef && newDef) {
                // the element was not in A but oldV will  be in B thus the oldV is added to the intersection
                element_added (oldV)
                cached_size += 1
            }
            initialized = true
        }
    }

}
