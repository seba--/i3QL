package sae.core.operators

import sae.core.Relation
import sae.core.SelfMaintainedView
import sae.core.MaterializedView
import sae.core.impl.MultisetRelation
import sae.core.impl.SizeCachedMaintainedView
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
		val relation : Relation[Domain]
	)
	 extends Relation[Range]
		with SelfMaintainedView[Domain,Range]
		with SizeCachedMaintainedView
{
	relation addObserver this

	def asMaterialized = new MaterializedProjection(projection, relation)

	def initSize : Int = {
		var size = 0
		relation.foreach( v => {size += 1} )
		size
	}
	
	def foreach[T](f: (Range) => T) : Unit = 
	{
		var size = 0
		relation.foreach( v =>
			{
				size += 1
				f(projection(v))
			}
		)
		initSize(size)
	}
   
	def uniqueValue : Option[Range] = relation.uniqueValue match 
	{
		case None => None
		case Some(v) => Some(projection(v))
	}
	

	def updated(oldV : Domain, newV : Domain) : Unit =
	{
		element_updated(projection(oldV), projection(newV))
	}

	def removed(v : Domain) : Unit =
	{
		element_removed(projection(v))
		decSize
	}

	def added(v : Domain) : Unit =
	{
		element_added(projection(v))
		incSize
	}

}


class MaterializedProjection[Domain <: AnyRef, Range <: AnyRef]
	(
		val projection : Domain => Range,
		val relation : Relation[Domain]
	)
	 extends MultisetRelation[Range]
		with MaterializedView[Range]
		with SelfMaintainedView[Domain,Range]
{
	relation addObserver this

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