package sae.core.operators

import sae.core.Relation
import sae.core.SelfMaintainedView
import sae.core.MaterializedView
import sae.core.impl.MultisetRelation
import sae.Observer
import sae.View

/**
 * A projection operates as a filter on the relation and eliminates unwanted constituents from the tuples.
 * Thus the projection shrinks the size of relations.
 * The new relations are either a new kind of object or anonymous tuples of the warranted size and types of the projection.
 */
class Projection[Domain <: AnyRef, Range <: AnyRef]
	(
		val projection : Domain => Range,
		// val projectionArity : Int, 
		val relation : Relation[Domain]
	)
	 extends MultisetRelation[Range]
		with SelfMaintainedView[Domain,Range]
		with MaterializedView[Range]
{
	relation addObserver this

	// TODO we forego arity for the time being and try to rely on the type system
	// TODO how can we infer the arity of the projection?
	// There are not necessarily any tuples generated, otherwise we could ask the first element 
	// def arity = projectionArity
	
	def materialize() : Unit = 
	{
		relation.foreach( t =>
			{
				this += projection(t)
			}
		)
	}
	
	// update operations
	def updated(oldV: Domain, newV: Domain): Unit = 
	{
		val oldP = projection(oldV)
		val newP = projection(newV)
		if( oldP equals newP )
			return;
		this -= oldP
		this += newP
	}

	def removed(v: Domain): Unit = 
	{
		this -= projection(v)
	}

	def added(v: Domain): Unit = 
	{
		this += projection(v)
	}
  

}