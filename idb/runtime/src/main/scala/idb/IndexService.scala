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
package idb

/**
 *
 * @author Ralf Mitschke
 */
object IndexService
{

    def getIndex[K, V](relation:Relation[V], keyFunction: V=>K) : Index[K, V]  = {
		//TODO This may need some work
		if (relation.isSet)
		{
			new SetIndex[K, V](relation, keyFunction)
		}
		else
		{
			new BagIndex[K, V](relation, keyFunction)
		}
	}



}


class SetIndex[K, V](val relation: Relation[V],
					 val keyFunction: V => K)
	extends Index[K, V]
{

	relation addObserver this

	val isSet = true
	private val map = com.google.common.collect.ArrayListMultimap.create[K, V]()

	lazyInitialize ()

	def foreachKey[U](f: (K) => U) {
		val it = map.keys ().iterator ()
		while (it.hasNext) {
			val next = it.next ()
			f (next)
		}
	}

	def put(key: K, value: V)
	{
		map.put (key, value)
	}

	def get(key: K): Option[Traversable[V]] =
	{
		val l = map.get (key)
		if (l.isEmpty)
			return None
		Some (new ValueListTraverser (l))
	}

	private class ValueListTraverser[V](val values: java.util.List[V]) extends Traversable[V]
	{
		def foreach[T](f: V => T)
		{
			val it: java.util.Iterator[V] = values.iterator
			while (it.hasNext) {
				val next = it.next ()
				f (next)
			}
		}
	}

	def contains(key: K): Boolean = map.containsKey (key)


	def count(key: K) =
		if (map.containsKey (key))
		{
			map.get (key).size ()
		}
		else
		{
			0
		}

	override def foreach[U](f: ((K, V)) => U)
	{
		val it: java.util.Iterator[java.util.Map.Entry[K, V]] = map.entries ().iterator
		while (it.hasNext) {
			val next = it.next ()
			f ((next.getKey, next.getValue))
		}
	}


	override def size = map.values ().size ()

	def add_element(k: K, v: V)
	{
		map.put (k, v)
	}

	def remove_element(k: K, v: V)
	{
		map.remove (k, v)
	}

	def update_element(oldKey: K, oldV: V, newKey: K, newV: V)
	{
		remove_element (oldKey, oldV)
		add_element (newKey, newV)
	}
}

/**
 * An index backed by a guava ListMultimap.
 * The index may have multiple values for a single key.
 * The index stores multiple equal key-value pairs.
 * Thus this Index is suited for bag semantics.
 *
 * Multi-value semantics is a pre-requisite for the index to work with
 * arbitrary data. Parts of a tuple may be defined as index and must not be
 * unique in any way.
 */
class BagIndex[K, V](val relation: Relation[V],
					 val keyFunction: V => K)
	extends Index[K, V] {

	relation addObserver this

	val isSet : Boolean = false


	private val map = com.google.common.collect.ArrayListMultimap.create[K, V]()

	lazyInitialize ()

	def size = map.size ()

	def foreachKey[U](f: (K) => U) {
		val it = map.keySet ().iterator ()
		while (it.hasNext) {
			val next = it.next ()
			f (next)
		}
	}

	def put(key: K, value: V) {
		map.put (key, value)
	}

	def get(key: K): Option[Traversable[V]] = {
		val l = map.get (key)
		if (l.isEmpty)
			return None
		Some (new ValueListTraverser (l))
	}

	private class ValueListTraverser[V](val values: java.util.List[V]) extends Traversable[V]
	{
		def foreach[T](f: V => T) {
			val it: java.util.Iterator[V] = values.iterator
			while (it.hasNext) {
				val next = it.next ()
				f (next)
			}
		}
	}

	def contains(key: K): Boolean = map.containsKey (key)


	def count(key: K) =
		if (map.containsKey (key))
		{
			map.get (key).size ()
		}
		else
		{
			0
		}

	def foreach[U](f: ((K, V)) => U) {
		val it: java.util.Iterator[java.util.Map.Entry[K, V]] = map.entries ().iterator
		while (it.hasNext) {
			val next = it.next ()
			f ((next.getKey, next.getValue))
		}
	}

	def add_element(key: K, value: V) {
		map.put (key, value)
	}


	def remove_element(key: K, value: V) {
		map.remove (key, value)
	}

	def update_element(oldKey: K, oldV: V, newKey: K, newV: V) {
		val list = map.get (oldKey)
		val it = list.iterator ()
		val retainedMap = new java.util.LinkedList[V]()
		val newMap = new java.util.LinkedList[V]()
		while (it.hasNext) {
			val next = it.next ()
			if (next == oldV)
				newMap.add (newV)
			else
				retainedMap.add (next)
		}
		map.replaceValues (oldKey, retainedMap)
		map.putAll (newKey, newMap)
	}

}
