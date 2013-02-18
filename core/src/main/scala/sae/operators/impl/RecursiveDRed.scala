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

class RecursiveDRed[Domain](val relation: Relation[Domain])
    extends RecursiveView[Domain]
{
    relation.addObserver (this)

    // the set of elements in the current recursion
    // recording them avoids endless recursions
    private val recursiveDerivations: mutable.HashSet[Domain] = mutable.HashSet.empty

    // these elements were already derived once and will not be propagated a second time
    private val derivedElements: mutable.HashMap[Domain, Int] = mutable.HashMap.empty

    private val deletedElements: mutable.HashMap[Domain, Int] = mutable.HashMap.empty

    def added(v: Domain) {

        if (!recursiveDerivations.isEmpty) {
            if (!derivedElements.contains(v) && !recursiveDerivations.contains (v)) {
                recursiveDerivations += v
            }
            val derivationCount = derivedElements.getOrElseUpdate (v, 0)
            derivedElements (v) = derivationCount + 1

            // during the recursion I have seen another derivation for v
            return
        }


        val derivationCount = derivedElements.getOrElseUpdate (v, 0)
        derivedElements (v) = derivationCount + 1
        if (derivationCount == 0) {
            recursiveDerivations += v
            element_added (v)
            recursiveDerivations -= v
        }

        while (!recursiveDerivations.isEmpty) {
            val next = recursiveDerivations.head
            val derivationCount = derivedElements.getOrElseUpdate (next, 0)
            if (derivationCount == 1) {
                element_added (next)
            }
            recursiveDerivations -= next
        }
    }

    def removed(v: Domain) {
        if (!recursiveDerivations.isEmpty) {
            if (!deletedElements.contains(v) && !recursiveDerivations.contains (v)) {
                recursiveDerivations += v
            }

            // during the recursion I have seen another derivation for v
            val derivationCount = deletedElements.getOrElseUpdate (v, 0)
            deletedElements (v) = derivationCount + 1

            return
        }


        val derivationCount = deletedElements.getOrElseUpdate (v, 0)
        deletedElements (v) = derivationCount + 1
        if (derivationCount == 0) {
            recursiveDerivations += v
            element_removed (v)
            recursiveDerivations -= v
        }

        while (!recursiveDerivations.isEmpty) {
            val next = recursiveDerivations.head
            val derivationCount = deletedElements.getOrElseUpdate (next, 0)
            if (derivationCount == 1) {
                element_removed (next)
            }
            recursiveDerivations -= next
        }

        for( (key,count) <- deletedElements) {
            var derivationCount = derivedElements(key) // it should be contained in the derived elements
            derivationCount -= count
            if(derivationCount > 0) {
                derivedElements.put(key, derivationCount)
            }
            else {
                derivedElements.remove(key)
            }
        }
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
