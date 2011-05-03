package sae.core.operators

import sae.core.Relation
import sae.core.SelfMaintainedView
import sae.core.MaterializedView
import sae.core.impl.MultisetRelation
import sae.core.impl.SizeCachedMaintainedView
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
	 extends Relation[V]
		with SelfMaintainedView[V,V]
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

/**
 * A materialized selection extends a selection to be materialized
 */
class MaterializedSelection[V <: AnyRef]
	(
		val filter : V => Boolean,
		val relation : Relation[V]
	)
	 extends MultisetRelation[V]
		with MaterializedView[V]
		with SelfMaintainedView[V,V]
{
	relation addObserver this
	
	def materialize() : Unit = 
	{
		relation.foreach( t =>
			{
				if(filter(t))
				{
					add_element(t)
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

/*
trait SelectionCriteria

class ConstantSelection[Attribut, Constant](attr : Attribut, const : Constant) extends SelectionCriteria

class AttributeSelection[AttributA, AttributB](attrA : AttributA, attrB : AttributB) extends SelectionCriteria
*/