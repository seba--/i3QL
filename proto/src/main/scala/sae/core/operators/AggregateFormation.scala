package sae.core.operators

import sae.core.Relation
import sae.core.SelfMaintainedView
import sae.core.MaterializedView
import sae.core.impl.BagRelation
import sae.core.impl.SizeCachedMaintainedView
import sae.Observer
import sae.View


/**
 * An aggregate operator receives a list of functions and a list of attributes.
 * The functions are evaluated on all tuples that coincide in all supplied attributes.
 * Each function has one (or several) attributes as its domain.
 * The list of attributes serves as a grouping key. This key is used to split the relation into 
 * a relation that has only distinct combinations in the supplied attributes.
 * (Note that this grouping is a projection in terms of Codds original relational algebra) 
 * The list of grouping attributes is optional.
 * If no grouping is supplied, the aggregation functions are applied on the entire relation.
 * If no function is supplied the aggregation has no effect.
 */
/*
class SingleRelationAggregate[Domain <: AnyRef, KeyDomain <: AnyRef]
	(
		val relation : Relation[Domain]
	)
	 extends Relation[DomainB]
		with SelfMaintainedView[Domain,V]
		with SizeCachedMaintainedView
{
	relation addObserver this

	def asMaterialized = new MaterializedSelection(filter, relation)

	def initSize : Int = {
		var size = 0
		relation.foreach( v =>
			{
				if( filter(v) )
				{
					size += 1
				}
			} 
		)
		size
	}
	
	def foreach[T](f: (V) => T) : Unit = 
	{
		var size = 0
		relation.foreach( v =>
			{
				if( filter(v) )
				{
					size += 1
					f(v)
				}
			}
		)
		initSize(size)
	}
	
	def uniqueValue : Option[V] = 
	{
		var value : Option[V] = None
		var double : Boolean = false
		relation.foreach( v => 
			{
				if( filter(v) )
				{
					if( value == None)
					{
						value = Some(v)
					}
					else
					{
						double = true
					}
				}
			} 
		)
		if( ! double )
			value
		else
			None
	}
	

	def updated(oldV : V, newV : V) : Unit =
	{
		if( filter(oldV) && filter(newV))
		{
			element_updated(oldV, newV)
		}
		else
		{
			// only one of the elements complies to the filter
			if( filter(oldV)  )
			{
				element_removed(oldV)
				decSize
			}
			if( filter(newV) )
			{
				element_added(newV)
				incSize
			}
		}
	}

	def removed(v : V) : Unit =
	{
		if( filter(v) )
		{
			element_removed(v)
			decSize
		}
	}

	def added(v : V) : Unit =
	{
		if( filter(v) )
		{
			element_added(v)
			incSize
		}
	}

}
*/