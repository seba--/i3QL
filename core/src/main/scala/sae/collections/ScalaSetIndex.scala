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
package collections

import collection.mutable
import deltas.{Deletion, Addition, Update}

/**
 *
 */
class ScalaSetIndex[K, V](val relation: Relation[V],
                          val keyFunction: V => K)
    extends Index[K, V]
{

    private val map: mutable.HashMap[K, List[V]] = new mutable.HashMap[K, List[V]]

    lazyInitialize ()

    var totalSize = 0

    def size = totalSize

    def foreachKey[U](f: (K) => U) {
        map.foreach (entry => f (entry._1))
    }

    def put(key: K, value: V) {
        val list = map.getOrElseUpdate (key, Nil)
        map (key) = value :: list
        totalSize += 1
    }

    def get(key: K): Option[Traversable[V]] = {
        map.get (key)
    }

    def isDefinedAt(key: K): Boolean = map.isDefinedAt (key)


    def elementCountAt(key: K) =
        if (isDefinedAt (key))
        {
            map (key).size
        }
        else
        {
            0
        }

    def foreach[U](f: ((K, V)) => U) {
        map.foreach (entry => {
            entry._2.foreach (
                v => f (entry._1, v)
            )
        }
        )
    }

    def add_element(key: K, value: V) {
        put (key, value)
    }


    def remove_element(key: K, value: V) {
        val list = map (key)
        val newList = list.filterNot (_ == value)
        if (newList.isEmpty)
        {
            map.remove (key)
        }
        else
        {
            map (key) = newList
        }
        totalSize -= 1
    }

    def update_element(oldKey: K, oldV: V, newKey: K, newV: V) {
        remove_element (oldKey, oldV)
        add_element (newKey, newV)
    }

    def updated[U <: V](update: Update[U]) {
        //assert (update.count == 1)
        val oldV = update.oldV
        val newV = update.newV

        val oldKey = keyFunction (update.oldV)
        val newKey = keyFunction (update.newV)

        if (oldKey == newKey)
        {
            return
        }
        remove_element (oldKey, oldV)
        add_element (newKey, newV)
    }

    def added[U <: V](addition: Addition[U]) {
        //assert (addition.count == 1)
        val v = addition.value
        val key = keyFunction (v)
        add_element (key, v)
    }

    def deleted[U <: V](deletion: Deletion[U]) {
        //assert (deletion.count == 1)
        val v = deletion.value
        val key = keyFunction (v)
        remove_element (key, v)
    }

    def modified[U <: V](additions: scala.collection.immutable.Set[Addition[U]], deletions: scala.collection.immutable.Set[Deletion[U]], updates: scala.collection.immutable.Set[Update[U]]) {
        additions.foreach (added)
        deletions.foreach (deleted)
        updates.foreach (updated)
    }
}
