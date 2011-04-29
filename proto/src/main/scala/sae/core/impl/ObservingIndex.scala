package sae.core.impl

import sae.core.Index
import sae.core.Relation

// an abstract class for reuse in java
abstract class ObservingIndex[K, V, R <: Relation[_ <: V]]
	(val relation : R) 
	extends Index[K,V,R]
{
	// init the observer interface
	relation addObserver this

	// required indexing operations
	def update_index(oldV: V, newV: V): K
	
	def remove_index(v: V): K
	
	def add_index(v: V): K

	// observer notifications
	def updated(oldV: V, newV: V): Unit = 
	{
		val key = update_index(oldV, newV)
		element_updated((key, oldV), (key, newV)) 
	}

	def removed(v: V): Unit = 
	{
		val key = remove_index(v)
		element_removed((key, v)) 
	}

	def added(v: V): Unit = 
	{ 
		val key = add_index(v)
		element_added((key, v))
	}
}

/*

	// init the observer interface
	relation addObserver this

	// required indexing operations
	def update_index(oldV: (K, V), newV: (K, V)): Unit
	
	def remove_index(e: (K,V)): Unit
	
	def add_index(e: (K, V)): Unit

	// observer notifications
	def updated(oldV: (K, V), newV: (K, V)): Unit = 
	{
		update_index(oldV, newV)
		element_updated(oldV, newV) 
	}

	def removed(e: (K,V)): Unit = 
	{
		remove_index(e)
		element_removed(e) 
	}

	def added(e: (K, V)): Unit = 
	{ 
		add_index(e)
		element_added(e)
	}
	
*/