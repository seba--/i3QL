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

import sae.{Observable, MaterializedRelation, Observer}
import sae.operators.Union
import sae.deltas.{Update, Deletion, Addition}

/**
 * A not self maintained union, that produces max(count(A) , count(B)) duplicates for underlying relations A and B
 * Required for correctness if we translate conditionA OR conditionB to algebra.
 * Can be omitted if A \intersect B == empty
 *
 * The UnionViewMax requires materialized relations as underlying relations, but does not require to store the result.
 */
class UnionViewMax[Range, DomainA <: Range, DomainB <: Range](val left: MaterializedRelation[DomainA],
                                                              val right: MaterializedRelation[DomainB])
    extends Union[Range, DomainA, DomainB]
{
    left addObserver LeftObserver

    right addObserver RightObserver


    override protected def childObservers(o: Observable[_]): Seq[Observer[_]] = {
        if (o == left) {
            return List (LeftObserver)
        }
        if (o == right) {
            return List (RightObserver)
        }
        Nil
    }


    def foreach[T](f: (Range) => T) {
        left.foreach (
            (v: Range) =>
            {
                val max = scala.math.max (left.elementCountAt (v), right.elementCountAt (v))
                var i = 0
                while (i < max) {
                    f (v)
                    i += 1
                }
            }
        )
    }

    object LeftObserver extends Observer[DomainA]
    {
        def updated(oldV: DomainA, newV: DomainA) {
            if (oldV == newV)
                return
            // if we assume that update means all instances are updated this is correct
            element_updated (oldV, newV)
        }

        def removed(v: DomainA) {
            val oldCount = left.elementCountAt (v)
            val rightCount = right.elementCountAt (v)
            if (rightCount < oldCount - 1) {
                element_removed (v)
            }
        }

        def added(v: DomainA) {
            val oldCount = left.elementCountAt (v)
            val rightCount = right.elementCountAt (v)
            if (rightCount < oldCount + 1) {
                element_added (v)
            }
        }

        def updated(update: Update[DomainA]) {
            throw new UnsupportedOperationException
        }

        def modified(additions: Set[Addition[DomainA]], deletions: Set[Deletion[DomainA]], updates: Set[Update[DomainA]]) {
            throw new UnsupportedOperationException
        }
    }

    object RightObserver extends Observer[DomainB]
    {
        // update operations on right relation
        def updated(oldV: DomainB, newV: DomainB) {
            if (oldV == newV)
                return
            // if we assume that update means all instances are updated this is correct
            element_updated (oldV, newV)
        }

        def removed(v: DomainB) {
            val leftCount = left.elementCountAt (v)
            val oldCount = right.elementCountAt (v)
            if (leftCount < oldCount - 1) {
                element_removed (v)
            }
        }

        def added(v: DomainB) {
            val leftCount = left.elementCountAt (v)
            val oldCount = right.elementCountAt (v)
            if (leftCount < oldCount + 1) {
                element_added (v)
            }
        }

        def updated(update: Update[DomainB]) {
            throw new UnsupportedOperationException
        }

        def modified(additions: Set[Addition[DomainB]], deletions: Set[Deletion[DomainB]], updates: Set[Update[DomainB]]) {
            throw new UnsupportedOperationException
        }
    }

}
