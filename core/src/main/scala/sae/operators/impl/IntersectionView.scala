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

import sae.{LazyInitializedObserver, LazyInitializedRelation, MaterializedRelation, Observer}
import sae.operators.Intersection

/**
 * This intersection operation distinct set semantics for elements
 *
 */
class IntersectionView[Domain <: AnyRef](val left: MaterializedRelation[Domain],
                                         val right: MaterializedRelation[Domain])
    extends Intersection[Domain]
    with LazyInitializedRelation
{

    left addObserver LeftObserver

    right addObserver RightObserver

    def lazyInitialize() {
        if (isInitialized) return
        left.foreach (
        {
            case (key, element) if (right.isDefinedAt (element)) =>
                element_added (element)
            case _ => // do nothing
        }
        )
        isInitialized = true
    }

    object LeftObserver extends LazyInitializedObserver[Domain]
    {

        /**
         * We have just added to left (left.elementCountAt(v) >= 1).
         * While we add elements to left and
         * have less than or equal elements compared to right, we generate new duplicates.
         *
         */
        override def added(v: Domain) {
            if (left.elementCountAt(v) <= right.elementCountAt (v)) {
                element_added (v)
            }
        }

        /**
         * as long as left has more elements than right we only remove excess duplicates
         */
        override def removed(v:Domain) {
            if (left.elementCountAt(v) > right.elementCountAt (v)) {
                element_removed (v)
            }
        }

        def updated(oldKV: (Domain, Domain), newKV: (Domain, Domain)) {
            val oldV = oldKV._1
            val newV = newKV._1

            val oldDef = right.isDefinedAt (oldV)
            val newDef = right.isDefinedAt (newV)
            if (oldDef && newDef) {
                element_updated (oldV, newV)
                return
            }
            if (oldDef) {
                element_removed (oldV)
            }

            if (newDef) {
                element_added (newV)
            }
        }


    }


    object RightObserver extends Observer[(Domain, Domain)]
    {

        /**
         * We have just added to left (leftIndex.elementCountAt(v) >= 1).
         * While we add elements to left and
         * have less than or equal elements compared to right, we generate new duplicates.
         *
         */
        def added(kv: (Domain, Domain)) {
            val v = kv._1

            if (leftIndex.isDefinedAt (v) && !contains (v)) {
                element_added (v)
                add_element (v)
            }

            isInitialized = true
        }

        /**
         * as long as left has more elements than right we only remove excess duplicates
         */
        def removed(kv: (Domain, Domain)) {
            val v = kv._1

            if (!(rightIndex.isDefinedAt (v) && leftIndex.isDefinedAt (v))) {
                element_removed (v)
                remove_element (v)
            }

            isInitialized = true
        }

        def updated(oldKV: (Domain, Domain), newKV: (Domain, Domain)) {
            val oldV = oldKV._1
            val newV = newKV._1

            val oldDef = leftIndex.isDefinedAt (oldV)
            val newDef = leftIndex.isDefinedAt (newV)
            if (oldDef && !newDef) {
                // the element was in A but will not be in A and in B thus it is not be in the intersection
                element_removed (newV)
                remove_element (oldV)
            }

            if (!oldDef && newDef) {
                // the element was not in A but oldV will  be in B thus the oldV is added to the intersection
                element_added (oldV)
                add_element (newV)
            }

            isInitialized = true
        }
    }

}
