package sae.core.impl

import sae.core.Index
import sae.core.Relation

import scala.collection.mutable.HashMap

/** 
 * 
 */
class HashIndex[K <: AnyRef, V <: AnyRef]
	(
		override val relation : Relation[V],
		override val keyFunction : V => K
	) 
	extends Index[K,V]
{

	private val indexMap = new HashMap[K,V]()


	// implementation of indexing operations
	def updated(oldV : V, newV : V) : Unit =
	{
		// actually this should never happen
		val key = keyFunction(oldV)
		indexMap(key) = newV
	}
	
	def removed(v : V): Unit =
	{
		val key = keyFunction(v)
		indexMap -= key;
	}
	
	def added(v : V): Unit = 
	{
		val key = keyFunction(v)
		indexMap(key) = v
	}

	// access operations
	
	def put_internal(key: K, value: V) : Option[V] = indexMap.put(key, value)
	
	def get_internal(key: K): Option[V] = indexMap.get(key)
		
	def isDefinedAt_internal(key: K): Boolean = indexMap.isDefinedAt(key)
}