package sae.core

import sae.Observable
import sae.View

trait MaterializedRelation[T <: AnyRef] 
	 extends Relation[T]
		with MaterializedView[T]
{
	/**
	 * Add a data tuple to this relation.
	 * The element is added and then a change event is fired.
	 */ 
	def +=(v : T) : MaterializedRelation[T] =
	{
		add_element(v)
		element_added(v)
		this
   	}
   	
   	// internal implementation of the add method
	protected def add_element(v : T) : Unit
   		
	/**
	 * Remove a data tuple from this relation
	 * The element is removed and then a change event is fired. 
	 */
	def -=(v : T) : MaterializedRelation[T] =
	{
		remove_element(v)
		element_removed(v)
		this
	}
	
   	// internal implementation of the add method
	protected def remove_element(v : T) : Unit

	
	/**
	 * returns an index for specified key function
	 */
	def index[K <: AnyRef](keyFunction : T => K) : Index[K,T]
}