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

import sae.operators.RecursiveView
import sae.deltas.{Update, Deletion, Addition}
import sae.Relation
import collection.mutable

/**
 *
 * @author Ralf Mitschke
 *
 */

case class RecursiveDRed[Domain](relation: Relation[Domain])
    extends RecursiveView[Domain]
{
    relation.addObserver (this)

    var currentRecursionElements: List[Domain] = Nil

    var isRecursionRunning = false

    // these elements were already derived once and will not be propagated a second time
    var derivedElements: mutable.HashSet[Domain] = mutable.HashSet.empty

    var deletedElements: mutable.HashSet[Domain] = mutable.HashSet.empty

    def added(v: Domain) {
        if (isRecursionRunning) {
            if (!derivedElements.contains (v)) {
                currentRecursionElements ::= v
            }
            return
        }

        isRecursionRunning = true
        derivedElements.add (v)
        element_added (v)

        while (!currentRecursionElements.isEmpty) {
            val next = currentRecursionElements.head
            currentRecursionElements = currentRecursionElements.tail
            if (derivedElements.add (next)) {
                element_added (next)
            }
        }
        isRecursionRunning = false
    }

    def removed(v: Domain) {
        if (isRecursionRunning) {
            if (!deletedElements.contains (v)) {
                currentRecursionElements ::= v
            }
            return
        }

        isRecursionRunning = true

        deletedElements.add (v)
        element_removed (v)


        while (!currentRecursionElements.isEmpty) {
            val next = currentRecursionElements.head
            currentRecursionElements = currentRecursionElements.tail
            if (deletedElements.add (next)) {
                element_removed (next)
            }
        }

        derivedElements = derivedElements -- deletedElements
        deletedElements = mutable.HashSet.empty
        isRecursionRunning = false
    }

    def updated(oldV: Domain, newV: Domain) {
        throw new UnsupportedOperationException
    }

    def updated[U <: Domain](update: Update[U]) {
        throw new UnsupportedOperationException
    }

    def modified[U <: Domain](additions: Set[Addition[U]], deletions: Set[Deletion[U]], updates: Set[Update[U]]) {
        throw new UnsupportedOperationException
    }

}
