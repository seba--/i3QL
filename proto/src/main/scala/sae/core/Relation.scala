package sae.core

import sae.Observable
import sae.View

trait Relation[T <: AnyRef] 
	 extends View[T]
		with InfixRelationalAlgebraSyntax[T]
{
	def arity : Int

	//def findIndex[K](column : Int) : Index[K, T, Relation[T]]


	/**
	 * Add a data tuple to this relation.
	 * The element is added and then a change event is fired.
	 */ 
	def +=(v : T) : Relation[T] =
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
	def -=(v : T) : Relation[T] =
	{
		remove_element(v)
		element_removed(v)
		this
	}
	
   	// internal implementation of the add method
	protected def remove_element(v : T) : Unit

}