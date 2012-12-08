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

import sae.{Observable, Observer, MaterializedRelation}
import sae.operators.{ExistsInSameDomain, NotExistsInSameDomain}
import sae.deltas.{Update, Deletion, Addition}

/**
 * The difference operation in our algebra has non-distinct bag semantics
 *
 * This class can compute the difference efficiently by relying on indices from the underlying relations.
 * The operation itself does not store any intermediate results.
 * Updates are computed based on indices and foreach is recomputed on every call.
 *
 */
class ExistsInSameDomainView[Domain](val left: MaterializedRelation[Domain],
                                        val right: MaterializedRelation[Domain])
    extends ExistsInSameDomain[Domain]
{


    left addObserver LeftObserver

    right addObserver RightObserver

    override protected def childObservers (o: Observable[_]): Seq[Observer[_]] =
    {
        if (o == left)
        {
            return List (LeftObserver)
        }
        if (o == right)
        {
            return List (RightObserver)
        }
        Nil
    }

    /**
     * Applies f to all elements of the view.
     */
    def foreach[T] (f: (Domain) => T)
    {
        left.foreachWithCount (
            (v: Domain, leftCount: Int) =>
            {
                val rightCount = right.elementCountAt (v)
                val max = if(rightCount > 0) leftCount else 0
                var i = 0
                while (i < max)
                {
                    f (v)
                    i += 1
                }
            }
        )
    }

    var leftFinished  = false
    var rightFinished = false

    object LeftObserver extends Observer[Domain]
    {
        override def endTransaction() {
            leftFinished = true
            if (rightFinished)
            {
                notifyEndTransaction ()
                leftFinished = false
                rightFinished = false
            }
        }

        def updated(oldV: Domain, newV: Domain) {
            // we are notified after the update, hence the left will be updated to newV
            if (right.elementCountAt (oldV) > 0) {
                var oldCount = left.elementCountAt (oldV)
                while (oldCount > 0)
                {
                    element_removed (oldV)
                    oldCount -= 1
                }
            }
            if (right.elementCountAt (newV) > 0) {
                var newCount = left.elementCountAt (newV)
                while (newCount > 0)
                {
                    element_added (newV)
                    newCount -= 1
                }
            }
        }

        def removed(v: Domain) {
            // check that this was a removal where the element did not exist on the right side
            if (right.elementCountAt (v) > 0) {
                element_removed (v)
            }
        }

        def added(v: Domain) {
            // check that this was an addition where the element did not exist on the right side
            if (right.elementCountAt (v) > 0) {
                element_added (v)
            }
        }

        def updated[U <: Domain](update: Update[U]) {
            throw new UnsupportedOperationException
        }

        def modified[U <: Domain](additions: Set[Addition[U]], deletions: Set[Deletion[U]], updates: Set[Update[U]]) {
            throw new UnsupportedOperationException
        }
    }

    object RightObserver extends Observer[Domain]
    {
        override def endTransaction() {
            rightFinished = true
            if (leftFinished)
            {
                notifyEndTransaction ()
                leftFinished = false
                rightFinished = false
            }
        }

        // update operations on right relation
        def updated(oldV: Domain, newV: Domain) {
            // we are notified after the update, hence the right will be updated to newV
            var oldCount = left.elementCountAt (oldV)

            while (oldCount > 0)
            {
                element_removed (oldV)
                oldCount -= 1
            }

            var newCount = left.elementCountAt (newV)
            while (newCount > 0)
            {
                element_added (newV)
                newCount -= 1
            }
        }

        def removed(v: Domain) {
            // check that this was the last removal of an element on the right side, then remove all from left side
            if (right.elementCountAt (v) == 0) {
                var newCount = left.elementCountAt (v)
                while (newCount > 0)
                {
                    element_removed (v)
                    newCount -= 1
                }
            }
        }

        def added(v: Domain) {
            // we know there is now at least one element on the right side, but we remove only the first time this was added
            if (right.elementCountAt (v) == 1) {
                var oldCount = left.elementCountAt (v)
                while (oldCount > 0)
                {
                    element_added (v)
                    oldCount -= 1
                }
            }
        }

        def updated[U <: Domain](update: Update[U]) {
            throw new UnsupportedOperationException
        }

        def modified[U <: Domain](additions: Set[Addition[U]], deletions: Set[Deletion[U]], updates: Set[Update[U]]) {
            throw new UnsupportedOperationException
        }
    }

}
