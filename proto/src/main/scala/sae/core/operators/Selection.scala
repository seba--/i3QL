package sae.core.operators

import sae.core.Relation
import sae.core.SelfMaintainedView
import sae.core.MaterializedView
import sae.core.impl.MultisetRelation
import sae.Observer
import sae.View

/**
 * A selection operates as a filter on the values in the relation and eliminates unwanted tuples.
 * Thus the projection shrinks the number of relations.
 */
class Selection[V <: AnyRef]
	(
		val filter : V => Boolean,
		val relation : Relation[V]
	)
	 extends MultisetRelation[V]
		with SelfMaintainedView[V,V]
		with MaterializedView[V]
{
	relation addObserver this

	// TODO we forego arity for the time being and try to rely on the type system
	// def arity = relation.arity
	
	def materialize() : Unit = 
	{
		relation.foreach( t =>
			{
				if(filter(t))
				{
					this += t
				}
			}
		)
	}
	
	// update operations
	def updated(oldV: V, newV: V): Unit = 
	{
		if( filter(oldV) )
		{
			this -= oldV
		}
		if( filter(newV) )
		{
			this += newV
		}
	}

	def removed(v: V): Unit = 
	{
		if( filter(v) )
		{
			this -= v
		}
	}

	def added(v: V): Unit = 
	{
		if( filter(v) )
		{
			this += v
		}
	}
  

}