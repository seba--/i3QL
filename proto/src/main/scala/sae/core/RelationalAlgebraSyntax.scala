package sae.core

import sae.core.operators._

trait InfixRelationalAlgebraSyntax[Domain <: AnyRef]
{
	self : Relation[Domain] =>
	
	import RelationalAlgebraSyntax._
	
	def ×[OtherDomain <: AnyRef](otherRelation: Relation[OtherDomain]) : Relation[(Domain, OtherDomain)] = cross_product(this, otherRelation)
	
}

object RelationalAlgebraSyntax
{

	/** definitions of selection syntax **/


	object select
	{
		def apply[Domain <: AnyRef](filter: Domain => Boolean, relation: Relation[Domain]) : Relation[Domain] =
		{
			return new Selection[Domain](filter, relation);
		}	
	}
	

	object σ
	{
		def apply[Domain <: AnyRef](filter: Domain => Boolean, relation: Relation[Domain]) : Relation[Domain] = select(filter, relation)
	}

/*	
	object select
	{
		def apply[Domain <: AnyRef](filter: SelectionCriteria[Domain], relation: Relation[Domain]) : Relation[Domain] =
		{
			return new Selection[Domain](filter, relation);
		}	
	}
	

	object σ
	{
		def apply[Domain <: AnyRef](filter: SelectionCriteria[Domain], relation: Relation[Domain]) : Relation[Domain] = select(filter, relation)
	}


	class selectionCriteriaFactory[T <: AnyRef, V <: AnyRef](val f : Function1[T, V])
	{
		
		def ===(v : V) : SelectionCriteria[V] = null
	}

	implicit def functionToSelectionCriteriaFactory[T <: AnyRef, V <: AnyRef](f : Function1[T, V]) : selectionCriteriaFactory[T,V] =
		new selectionCriteriaFactory[T,V](f)
*/

	/** END definitions of selection syntax **/

	
	/** definitions of projection syntax **/
	
	object project
	{
		def apply[Domain <: AnyRef,Range <: AnyRef](projection: Domain => Range, relation : Relation[Domain]) : Relation[Range] = 
		{
			return new Projection[Domain, Range](projection, relation);
		}
	}
	
	object Π
	{
		def apply[Domain <: AnyRef,Range <: AnyRef](selection: Domain => Range, relation : Relation[Domain]) : Relation[Range] = project(selection, relation)
		
		def unapply[Domain <: AnyRef,Range <: AnyRef](p : Projection[Domain, Range]) : Option[(Domain => Range, Relation[Domain])]= Some((p.projection, p.relation))
	}
	
	/** definitions of cross product syntax **/
	
	object cross_product
	{
		def apply[DomainA <: AnyRef, DomainB <: AnyRef](relationA : Relation[DomainA], relationB: Relation[DomainB]) : Relation[(DomainA, DomainB)] =
		{
			return new CrossProduct(relationA, relationB);
		}
	}
	
	// TODO cross product object for pattern matching
	// see also infix syntax
	object  ×
	{
		def apply[DomainA <: AnyRef, DomainB <: AnyRef](relationA : Relation[DomainA], relationB: Relation[DomainB]) : Relation[(DomainA, DomainB)] = cross_product(relationA, relationB)
		
		//def unapply()
	}

	 

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
	

	// equi-joins
	object |><|
	{
		// natural join
		def apply[DomainA <: AnyRef, DomainB <: AnyRef](relationA : Relation[DomainA], relationB: Relation[DomainB]) : Relation[(DomainA, DomainB)] = eq_join(relationA, relationB) 

		// general join
		def apply[DomainA <: AnyRef, DomainB <: AnyRef](columns : List[String], relationA : Relation[DomainA], relationB: Relation[DomainB]) : Relation[(DomainA, DomainB)]  = eq_join(columns, relationA, relationB)
		
		// self join
		def apply[Domain <: AnyRef](columns : List[String], relation : Relation[Domain]) : Relation[Domain] = eq_join(columns, relation)

	}


}