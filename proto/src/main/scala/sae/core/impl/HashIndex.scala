package sae.core.impl

import sae.core.Index
import sae.core.Relation

import scala.collection.mutable.HashMap

/** 
 * TODO Identifiable[K] -> K is probably covariant?
 */
/*
class HashIndex[K, V <: Identifiable[K], R <: Relation[_ <: V]]
	(override val relation : R) 
extends ObservingIndex[K,V,R](relation)
{
	// the index is lazy initialized
	private val indexMap = new HashMap[K,V]()

	// implementation of indexing operations
	def update_index(oldV: V, newV: V): K =
	{
		// actually this should never happen
		val key = oldV.guid
		indexMap(key) = newV
		return key
	}
	
	def remove_index(v: V): K =
	{
		val key = v.guid
		indexMap -= key;
		return key;
	}
	
	def add_index(v: V): K = 
	{
		val key = v.guid
		indexMap(key) = v
		return key
	}
	
	// view operations
	
	def foreach[T](f: Tuple2[K,V] => T) : Unit = indexMap.foreach(f)


	// access operations
	def get(key: K): Option[V] = indexMap.get(key)
	 
	
	def getOrElse(key: K, f: => V): V = indexMap.getOrElse(key, f)

	
	def isDefinedAt(key: K): Boolean = indexMap.isDefinedAt(key)
}
*/