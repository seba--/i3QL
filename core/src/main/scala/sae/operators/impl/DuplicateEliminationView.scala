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

import sae._
import capabilities.LazyInitializedObserver
import operators.DuplicateElimination
import scala.Some

/**
 * The set projection class implemented here used the relational algebra semantics.
 * The set projection removes duplicates from the results set.
 * We use the same Multiset as in Bag, but directly increment/decrement counts
 */
class DuplicateEliminationView[Domain <: AnyRef](val relation: Relation[Domain])
    extends DuplicateElimination[Domain]
    with MaterializedRelation[Domain]
    with LazyInitializedObserver[Domain]
{

    relation addObserver this

    import com.google.common.collect.HashMultiset

    private val data: HashMultiset[Domain] = HashMultiset.create[Domain]()

    def lazyInitialize() {
        if (isInitialized) return
        relation.foreach (
            t => {
                data.add (t)
            }
        )
        setInitialized ()
    }

    def foreach[U](f: Domain => U) {
        if (!isInitialized){
            lazyInitialize()
            setInitialized()
        }
        val it: java.util.Iterator[Domain] = data.elementSet ().iterator ()
        while (it.hasNext) {
            f (it.next ())
        }
    }


    protected def isDefinedAt_internal(v: Domain) = {
        data.contains (v)
    }

    protected def elementCountAt_internal[T >: Domain](v: T) = {
        data.count(v)
    }

    /**
     * We use a generalized bag semantics, thus this method
     * returns true if the element was not already present in the list
     * otherwise the method returns false
     */
    private def add_element(v: Domain): Boolean = {
        val result = data.count (v) == 0
        data.add (v)
        result
    }

    /**
     * We use a bag semantics, thus this method
     * returns false if the element is still present in the list
     * otherwise the method returns true, i.e., the element is
     * completely removed.
     */
    private def remove_element(v: Domain): Boolean = {
        data.remove (v)
        data.count (v) == 0
    }

    // update operations
    def updated_after_initialization(oldV: Domain, newV: Domain) {
        if (oldV == newV) {
            return
        }
        val count = data.count (oldV)
        data.remove (oldV, count)
        data.add (newV, count)
        element_updated (oldV, newV)
    }

    def removed_after_initialization(v: Domain) {
        if (remove_element (v)) {
            element_removed (v)
        }
    }

    def added_after_initialization(v: Domain) {
        if (add_element (v)) {
            element_added (v)
        }
    }

}
