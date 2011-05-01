package sae.core

import sae.Observable
import sae.View

trait Relation[T <: AnyRef] 
	 extends View[T]
		with InfixRelationalAlgebraSyntax[T]
{
	// TODO we forego arity for the moment and try to rely more on the typesystem 
	// def arity : Int

	//def findIndex[K](column : Int) : Index[K, T, Relation[T]]

	/**
	 * The type of the tuples is sometimes needed as first class entity, e.g., during optimization.
	 * Here the scala type system is not always powerful enough.
	 * In particular the generic arguments can not be pattern matched over.
	 */  
	type tupleType = T

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