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
package sae.collections

import sae._
import deltas.{Update, Deletion, Addition}

/**
 * A newResult that materializes all data from the underlying relation into a set
 */
class SetResult[V](val relation: Relation[V])
    extends Set[V]
    with Observer[V]
{

    relation addObserver this

    lazyInitialize ()

    override def endTransaction() {
        notifyEndTransaction()
    }

    override protected def children = List (relation)

    override protected def childObservers(o: Observable[_]): Seq[Observer[_]] = {
        if (o == relation) {
            return List (this)
        }
        Nil
    }

    def lazyInitialize() {
        relation.foreach (
            v => add_element (v)
        )
    }


    def updated(oldV: V, newV: V) {
        this -= oldV
        this += newV
    }

    def removed(v: V) {
        this -= v
    }

    def added(v: V) {
        this += v
    }

    def added[U <: V](addition: Addition[U]) {
        //assert (addition.count == 1)
        add_element (addition.value)
    }

    def deleted[U <: V](deletion: Deletion[U]) {
        //assert (deletion.count == 1)
        remove_element (deletion.value)
    }

    def updated[U <: V](update: Update[U]) {
        //assert (update.count == 1)
        remove_element (update.oldV)
        add_element (update.newV)
    }
    def modified[U <: V](additions: scala.collection.immutable.Set[Addition[U]], deletions: scala.collection.immutable.Set[Deletion[U]], updates: scala.collection.immutable.Set[Update[U]]) {
        additions.foreach (added[U])
        deletions.foreach (deleted[U])
        updates.foreach (updated[U])
        element_modifications (additions, deletions, updates)
    }

}
