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

import sae.deltas.{Deletion, Addition, Update}
import sae.{Update, Observable, Observer, Relation}
import sae.operators.Selection

/**
 *
 * A selection view is a selection that stores no tuples by itself.
 * All data is passed along if it passes the filter.
 *
 * The selection automatically registers as an observer of the relation upon construction
 *
 * @author Ralf Mitschke
 */
class SelectionView[Domain <: AnyRef](val relation: Relation[Domain],
                                      val filter: Domain => Boolean)
    extends Selection[Domain]
    with Observer[Domain]
{
    relation addObserver this

    override protected def childObservers(o: Observable[_]): Seq[Observer[_]] = {
        if (o == relation) {
            return List (this)
        }
        Nil
    }

    /**
     * Applies f to all elements of the view.
     */
    def foreach[T](f: (Domain) => T) {
        relation.foreach (
            (v: Domain) => if (filter (v)) {
                f (v)
            }
        )
    }

    def updated(oldV: Domain, newV: Domain) {
        val oldVPasses = filter (oldV)
        val newVPasses = filter (newV)
        if (oldVPasses && newVPasses) {
            element_updated (oldV, newV)
        }
        else
        {
            // only one of the elements complies to the filter
            if (oldVPasses)
            {
                element_removed (oldV)
            }
            if (newVPasses)
            {
                element_added (newV)
            }
        }
    }

    def removed(v: Domain) {
        if (filter (v)) {
            element_removed (v)
        }
    }

    def added(v: Domain) {
        if (filter (v)) {
            element_added (v)
        }
    }

    def modified(additions: Set[Addition[Domain]], deletions: Set[Deletion[Domain]], updates: Set[Update[Domain]]) {
        val realUpdates = updates.filter (_.affects (filter))

        val (updateOldToNew, removeOldAddNew) = realUpdates.partition (e => {
            filter (e.oldV) && filter (e.newV) // both pass and are an update
        })
        // TODO refactor that filter only gets applied once for updates
        val nextAdditions = additions.filter (e => filter(e.value)) ++ removeOldAddNew.filter(e => filter(e.newV)).map (_.asAddition)
        val nextDeletions = deletions.filter (e => filter(e.value)) ++ removeOldAddNew.filter(e => filter(e.oldV)).map (_.asDeletion)
        val nextUpdates = updateOldToNew
        element_modifications (nextAdditions, nextDeletions, nextUpdates)
    }

    def updated(oldV: Domain, update: Update[Domain]) {
        if (update.affects (filter)) {
            val oldVPasses = filter (oldV)
            val newV = update.oldV
            val newVPasses = filter (newV)
            if (oldVPasses && newVPasses) {
                element_updated (update)
            }
            else
            {
                var i = 0
                while (i < update.count) {
                    // only one of the elements complies to the filter
                    if (oldVPasses)
                    {
                        element_removed (oldV)
                    }
                    if (newVPasses)
                    {
                        element_added (newV)
                    }
                    i += 1
                }
            }
        }
    }
}
