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
import sae.{Observable, Observer, Relation}
import sae.operators.Projection
import collection.mutable

/**
 *
 *
 *
 * The bag projection has the usual SQL meaning of a projection
 * The projection is always self maintained and requires no additional data apart from the provided delta.
 *
 * @author Ralf Mitschke
 *
 */
class ProjectionView[Domain, Range](val relation: Relation[Domain],
                                    val projection: Domain => Range)
        extends Projection[Domain, Range]
        with Observer[Domain]
{
    relation addObserver this

    override protected def childObservers(o: Observable[_]): Seq[Observer[_]] = {
        if (o == relation) {
            return List(this)
        }
        Nil
    }

    /**
     * Applies f to all elements of the view.
     */
    def foreach[T](f: (Range) => T) {
        relation.foreach((v: Domain) => f(projection(v)))
    }

    @deprecated
    def updated(oldV: Domain, newV: Domain) {
        element_updated(projection(oldV), projection(newV))
    }

    def removed(v: Domain) {
        element_removed(projection(v))
    }

    def added(v: Domain) {
        element_added(projection(v))
    }

    def updated(update: Update[Domain]) {
        if (update.affects(projection)) {
            element_updated(Update(projection(update.oldV), projection(update.newV), update.count, update.project(projection)))
        }
    }

    def modified(additions: Set[Addition[Domain]], deletions: Set[Deletion[Domain]], updates: Set[Update[Domain]]) {
        {
            val realUpdates = updates.filter(_.affects(projection))

            var additionCounter: mutable.Map[Range, Addition[Range]] = mutable.HashMap.empty
            var nextAdditions = Set.empty[Addition[Range]]
            additions.foreach(e => {
                val r = projection(e.value)
                if (additionCounter.isDefinedAt(r)) {
                    val oldAddition = additionCounter(r)
                    val newAddition = Addition(r, oldAddition.count + e.count)
                    additionCounter(r) = newAddition
                    nextAdditions -= oldAddition
                    nextAdditions += newAddition
                }
                else {
                    val newAddition = Addition(r, e.count)
                    additionCounter(r) = newAddition
                    nextAdditions += newAddition
                }
            }
            )
            additionCounter = null


            var deletionCounter: mutable.Map[Range, Deletion[Range]] = mutable.HashMap.empty
            var nextDeletions = Set.empty[Deletion[Range]]

            deletions.foreach(e => {
                val r = projection(e.value)
                if (deletionCounter.isDefinedAt(r)) {
                    val oldDeletion = deletionCounter(r)
                    val newDeletion = Deletion(r, oldDeletion.count + e.count)
                    deletionCounter(r) = newDeletion
                    nextDeletions -= oldDeletion
                    nextDeletions += newDeletion
                }
                else {
                    val newDeletion = Deletion(r, e.count)
                    deletionCounter(r) = newDeletion
                    nextDeletions += newDeletion
                }
            }
            )
            deletionCounter = null

            var updateCounter: mutable.Map[Range, mutable.Map[Range, Update[Range]]] = mutable.HashMap.empty
            var nextUpdates = Set.empty[Update[Range]]

            realUpdates.foreach(e => {
                val oldR = projection(e.oldV)
                val newR = projection(e.newV)
                if (updateCounter.isDefinedAt(oldR)) {
                    if (updateCounter(oldR).isDefinedAt(newR)) {
                        val oldUpdate = updateCounter(oldR)(newR)
                        val newUpdate = Update(oldR, newR, oldUpdate.count + e.count, oldUpdate.properties)
                        updateCounter(oldR)(newR) = newUpdate
                        nextUpdates -= oldUpdate
                        nextUpdates += newUpdate
                    }
                    else {
                        val newUpdate = Update(oldR, newR, e.count, e.project(projection))
                        updateCounter(oldR)(newR) = newUpdate
                        nextUpdates += newUpdate
                    }
                }
                else {
                    val newUpdate = Update(oldR, newR, e.count, e.project(projection))
                    updateCounter(oldR)(newR) = newUpdate
                    nextUpdates += newUpdate
                }
            }
            )
            updateCounter = null

            element_modifications(nextAdditions, nextDeletions, nextUpdates)
        }
    }

