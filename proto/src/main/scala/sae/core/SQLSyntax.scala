package sae.core

import sae.core.operators._

import RelationalAlgebraSyntax._

/**
 * σ(Filter)(R) corresponds to the following SQL query:
 *   SELECT *
 *   FROM R
 *   WHERE Filter
 *
 * Π(Projection)(R) corresponds to the following SQL query:
 *   SELECT DISTINCT Projection
 *   FROM R
 * Note: Non distinct projections do not fulfill all relational algebra properties (see Projection.scala)
 */
trait InfixSQLSyntax[Domain <: AnyRef]
{
	self : Relation[Domain] =>
	
	import SQLSyntax._
	
	def where(filter: Domain => Boolean) : Relation[Domain] = σ(filter, this)
	
}
object SQLSyntax
{
	

	object select
	{
		def apply[Domain <: AnyRef, Range <: AnyRef](projection: Domain => Range) : SelectFunctor[Domain,Range] = new SelectFunctor(projection) 
		
		def apply[Domain <: AnyRef, Range <: AnyRef](functor : DistinctFunctor[Domain,Range]) : DistinctFunctor[Domain,Range] = functor
		
		def apply[Domain <: AnyRef](star : *.type) : *.type = *
	}
	
	object distinct
	{
		def apply[Domain <: AnyRef, Range <: AnyRef](projection: Domain => Range) : DistinctFunctor[Domain,Range] = new DistinctFunctor[Domain,Range](projection)
	}

	/* helper classes for sql style infix syntax */
	final class SelectFunctor[Domain <: AnyRef, Range <: AnyRef]
		(projection: Domain => Range)
	{
		def from(relation : Relation[Domain]) = new NonSetProjection[Domain, Range](projection, relation);
		
	}
	
	final class DistinctFunctor[Domain <: AnyRef, Range <: AnyRef]
		(projection: Domain => Range)
	{
		def from(relation : Relation[Domain]) : Relation[Range] = Π(projection, relation);
	}
	
	object *
	{
		// def apply[T <: AnyRef]() : (T => T) = (x:T) => x
		
		def from[Domain <: AnyRef](relation : Relation[Domain]) : Relation[Domain] = relation;
	}
}