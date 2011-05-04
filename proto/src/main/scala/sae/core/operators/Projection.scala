package sae.core.operators

import sae.core.Relation
import sae.core.SelfMaintainedView
import sae.core.MaterializedView
import sae.core.impl.BagRelation
import sae.core.impl.SetRelation
import sae.core.impl.MaterializedViewImpl
import sae.core.impl.SizeCachedMaintainedView
import sae.Observer
import sae.View

/**
 * A projection operates as a filter on the relation and eliminates unwanted constituents from the tuples.
 * Thus the projection shrinks the size of relations.
 * The new relations are either a new kind of object or anonymous tuples of the warranted size and types of the projection.
 * Important Note:
 * E.Codd in his seminal work: RELATIONAL COMPLETENESS OF DATA BASE SUBLANGUAGES
 * defined projection as a set operations. Thus the result does NOT contain duplicates.
 * According to other papers this treatment of duplicates complicates things 
 * (i.e., in the translation from relational calculus to relational algebra? - TODO check).
 * In particular the following property is not guaranteed: 
 * R intersect S is subset of R.
 * In set theory this is trivial. However with the use of duplicates the following situation arises:
 * R := a | b  S := a | b
 *      u | v       u | v
 * 
 * Definition of intersection in relational calculus
 * R intersect S = ( R[1,2 = 1,2]S )[1,2].
 * Reads as: R joined with S where column 1 and column 2 are equal the result contains  column 1 and column 2
 * Since the projection in the join 
 * R intersect S := a | b
 *                  u | v
 *                  a | b
 *                  u | v
 * The general projection class implemented here used the relational algebra semantics.
 * Specialized classes for SQL semantics are available (see further below).
 */
class SetProjection[Domain <: AnyRef, Range <: AnyRef]
	(
		val projection : Domain => Range,
		val relation : Relation[Domain]
	)
	 extends SetRelation[Range]
		with SelfMaintainedView[Domain,Range]
		with MaterializedViewImpl[Range]
{
	relation addObserver this

	def materialize() : Unit = 
	{
		relation.foreach( t =>
			{
				add_element(projection(t))
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

/**
 * The non set projection has the ususal SQL meaning of a projection
 */
class BagProjection[Domain <: AnyRef, Range <: AnyRef]
	(
		val projection : Domain => Range,
		val relation : Relation[Domain]
	)
	 extends Relation[Range]
		with SelfMaintainedView[Domain,Range]
		with SizeCachedMaintainedView
{
	relation addObserver this

	def asMaterialized = new MaterializedBagProjection(projection, relation)

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

/**
 * The materialized non-set projection has the semantics as the NonSetProjection
 */
class MaterializedBagProjection[Domain <: AnyRef, Range <: AnyRef]
	(
		val projection : Domain => Range,
		val relation : Relation[Domain]
	)
	 extends BagRelation[Range]
		with MaterializedViewImpl[Range]
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