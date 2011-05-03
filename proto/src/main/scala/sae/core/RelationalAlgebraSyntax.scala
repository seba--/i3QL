package sae.core

import sae.core.operators._

trait InfixRelationalAlgebraSyntax[Domain <: AnyRef]
{
	self : Relation[Domain] =>
	
	import RelationalAlgebraSyntax._
	
	def ×[OtherDomain <: AnyRef](otherRelation: Relation[OtherDomain]) : Relation[(Domain, OtherDomain)] = 
	{
		return new CrossProduct(this, otherRelation);
	}
	
}

object RelationalAlgebraSyntax
{

	/** definitions of selection syntax **/
	object σ
	{
		def apply[Domain <: AnyRef](filter: Domain => Boolean, relation: Relation[Domain]) : Relation[Domain] = 
		{
			return new Selection[Domain](filter, relation);
		}	

	}

	/** definitions of projection syntax **/
	object Π
	{
		def apply[Domain <: AnyRef,Range <: AnyRef](projection: Domain => Range, relation : Relation[Domain]) : Relation[Range] = 
		{
			return new Projection[Domain, Range](projection, relation);
		}
		
		def unapply[Domain <: AnyRef,Range <: AnyRef](p : Projection[Domain, Range]) : Option[(Domain => Range, Relation[Domain])]= Some((p.projection, p.relation))
	}
	
	/** definitions of cross product syntax **/
	
	// TODO cross product object for pattern matching
	// see also infix syntax
	object  ×
	{
		// def apply[DomainA <: AnyRef, DomainB <: AnyRef](relationA : Relation[DomainA], relationB: Relation[DomainB]) : Relation[(DomainA, DomainB)] = cross_product(relationA, relationB)
		
		//def unapply()
	}

/*	

	object σ
	{
		def apply[Domain <: AnyRef](filter: SelectionCriteria[Domain], relation: Relation[Domain]) : Relation[Domain] = select(filter, relation)
		{
			return new Selection[Domain](filter, relation);
		}	
	}


	class selectionCriteriaFactory[T <: AnyRef, V <: AnyRef](val f : Function1[T, V])
	{
		
		def ===(v : V) : SelectionCriteria[V] = null
	}

	implicit def functionToSelectionCriteriaFactory[T <: AnyRef, V <: AnyRef](f : Function1[T, V]) : selectionCriteriaFactory[T,V] =
		new selectionCriteriaFactory[T,V](f)
*/

	/** END definitions of selection syntax **/	 

	object eq_join
	{
		// natural join
		def apply[DomainA <: AnyRef, DomainB <: AnyRef](relationA : Relation[DomainA], relationB: Relation[DomainB]) : Relation[(DomainA, DomainB)] =
		{
			return null;
		}

		// equi-join
		def apply[DomainA <: AnyRef, DomainB <: AnyRef](columns : List[String], relationA : Relation[DomainA], relationB: Relation[DomainB]) : Relation[(DomainA, DomainB)] =
		{
			return null;
		}
				
		// self join
		def apply[Domain <: AnyRef](columns : List[String], relation : Relation[Domain]) : Relation[Domain] =
		{
			return null;
		}
	}


	
	// equi-joins
	object ⋈
	{
		// natural join
		def apply[DomainA <: AnyRef, DomainB <: AnyRef](relationA : Relation[DomainA], relationB: Relation[DomainB]) : Relation[(DomainA, DomainB)] = eq_join(relationA, relationB) 

		// general join
		def apply[DomainA <: AnyRef, DomainB <: AnyRef](columns : List[String], relationA : Relation[DomainA], relationB: Relation[DomainB]) : Relation[(DomainA, DomainB)]  = eq_join(columns, relationA, relationB)
		
		// self join
		def apply[Domain <: AnyRef](columns : List[String], relation : Relation[Domain]) : Relation[Domain] = eq_join(columns, relation)

	}
	
}