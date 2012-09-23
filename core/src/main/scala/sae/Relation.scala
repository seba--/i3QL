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
 *  Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  Neither the name of the Software Technology Group or Technische
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
package sae

import capabilities.{Listable, Iterable}
import collections.{BagResult, SetResult}

/**
 *
 *
 * A relation is the base trait for all operators and views.
 * A relation can be marked as a set, which must be guaranteed by the data provider.
 * If the data provider guarantees the data to have set property,
 * i.e., all tuples occur at most once, then additional optimizations are possible.
 *
 * All relations can be iterated over.
 * The iteration requires that this relation or view is materialized, or one of the underlying relation is materialized.
 * If no relation whatsoever is materialized the iteration returns has no elements.
 *
 * All relations can be materialized, even if they do not store elements themselves.
 *
 * All relations can be indexed, even if they do not store elements themselves.
 *
 * @author Ralf Mitschke
 */
trait Relation[V]
    extends Observable[V]
    with Iterable[V]
    with Listable[V]
{

    type Value = V

    def isSet: Boolean

    /**
     * Returns true if there is some intermediary storage, i.e., foreach is guaranteed to return a set of values.
     */
    def isStored : Boolean

    /**
     * Always return the same materialized view for this relation
     */
    def asMaterialized: MaterializedRelation[V] = materializedRelation

    private lazy val materializedRelation = {
        if (isSet) {
            new SetResult[V](this)
        }
        else
        {
            new BagResult[V](this)
        }
    }

    // internally we forget the concrete Key type, since there may be many
    // different keys
    var indices = new scala.collection.immutable.HashMap[(V => _), Index[_, V]]

    // Indices MUST be updated prior to any other notifications
    abstract override def element_added(v: V) {
        indices.values.foreach (_.added (v))
        super.element_added (v)
    }

    abstract override def element_removed(v: V) {
        indices.values.foreach (_.removed (v))
        super.element_removed (v)
    }

    abstract override def element_updated(oldV: V, newV: V) {
        indices.values.foreach (_.updated (oldV, newV))
        super.element_updated (oldV, newV)
    }

    /**
     * returns an index for specified key function
     */
    def index[K](keyFunction: V => K): Index[K, V] = {
        val index = indices.getOrElse (
        keyFunction,
        {
            val newIndex = createIndex (keyFunction)
            indices += (keyFunction -> newIndex)
            newIndex
        }
        )
        index.asInstanceOf[Index[K, V]]
    }

    protected def createIndex[K](keyFunction: V => K): Index[K, V] =
    {
        val index = new sae.collections.BagIndex[K, V](this, keyFunction)
        index
    }

}

