package sae
package syntax

import sae.operators._

case class InfixConcatenator[Domain <: AnyRef](val left : LazyView[Domain]) {

    import RelationalAlgebraSyntax._

    import Conversions._

    def ×[OtherDomain <: AnyRef](otherRelation : LazyView[OtherDomain]) : LazyView[(Domain, OtherDomain)] =
        {
            return new CrossProduct(left, otherRelation);
        }

    // general join using bowtie symbol (U+22C8)
    def ⋈[OtherDomain <: AnyRef](filter : ((Domain, OtherDomain)) => Boolean, otherRelation : LazyView[OtherDomain]) : LazyView[(Domain, OtherDomain)] =
        {
            return σ(filter)(this × otherRelation);
        }

}

object RelationalAlgebraSyntax {
    import sae.collections.QueryResult

    // convenience forwarding to not always import conversion, but only the syntax
    implicit def lazyViewToResult[V <: AnyRef](lazyView : LazyView[V]) : QueryResult[V] = sae.collections.Conversions.lazyViewToResult(lazyView)

    implicit def viewToConcatenator[Domain <: AnyRef](relation : LazyView[Domain]) : InfixConcatenator[Domain] =
        InfixConcatenator(relation)

    /** definitions of selection syntax **/
    object σ {
        def apply[Domain <: AnyRef](filter : Domain => Boolean)(relation : LazyView[Domain]) : LazyView[Domain] =
            {
                return new LazySelection[Domain](filter, relation);
            }

    }

    /** definitions of projection syntax **/
    object Π {
        def apply[Domain <: AnyRef, Range <: AnyRef](projection : Domain => Range)(relation : LazyView[Domain]) : LazyView[Range] =
            {
                return new SetProjection[Domain, Range](projection, relation);
            }

        def unapply[Domain <: AnyRef, Range <: AnyRef](p : SetProjection[Domain, Range]) : Option[(Domain => Range, LazyView[Domain])] = Some((p.projection, p.relation))
    }

    /** BEGIN definitions of cross product syntax **/

    // TODO cross product object for pattern matching
    // see also infix syntax
    object × {
        // def apply[DomainA <: AnyRef, DomainB <: AnyRef](relationA : Relation[DomainA], relationB: Relation[DomainB]) : Relation[(DomainA, DomainB)] = cross_product(relationA, relationB)

        //def unapply()
    }

    /** END definitions of cross product syntax **/
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

    /** BEGIN definitions of join syntax **/

    // TODO case object for pattern matching (see infix syntax)
    object ⋈ {

        /*
		// general join
		def apply[DomainA <: AnyRef, DomainB <: AnyRef](filter : (DomainA, DomainB) => Boolean, relationA : Relation[DomainA], relationB: Relation[DomainB]) : Relation[(DomainA, DomainB)] =
		{
			
		}

		// natural join
		def apply[DomainA <: AnyRef, DomainB <: AnyRef](relationA : Relation[DomainA], relationB: Relation[DomainB]) : Relation[(DomainA, DomainB)] =
		{
			return null;
		}
			
		// self join
		def apply[Domain <: AnyRef](columns : List[String], relation : Relation[Domain]) : Relation[Domain] =
		{
			return null;
		}
		*/
    }
    /** END definitions of join syntax **/
}
