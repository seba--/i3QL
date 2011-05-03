package sae.core

import sae.Observable
import sae.View

trait Relation[T <: AnyRef] 
	 extends View[T]
		with InfixRelationalAlgebraSyntax[T]
{

	/**
	 * The type of the tuples is sometimes needed as first class entity, e.g., during optimization.
	 * Here the scala type system is not always powerful enough.
	 * In particular the generic arguments can not be pattern matched over.
	 */  
	type tupleType = T


	def asMaterialized : MaterializedView[T]
}