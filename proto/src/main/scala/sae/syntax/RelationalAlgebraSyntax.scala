package sae
package syntax

import sae.operators._

case class InfixConcatenator[Domain <: AnyRef](val left : LazyView[Domain]) {

    import RelationalAlgebraSyntax._

    import Conversions._

    def ×[OtherDomain <: AnyRef](otherRelation : LazyView[OtherDomain]) : LazyView[(Domain, OtherDomain)] =
        {
            return new CrossProduct(lazyViewToMaterializedView(left), lazyViewToMaterializedView(otherRelation));
        }

    // general join using bowtie symbol (U+22C8)

    def ⋈[OtherDomain <: AnyRef](filter : ((Domain, OtherDomain)) => Boolean, otherRelation : LazyView[OtherDomain]) : LazyView[(Domain, OtherDomain)] =
        {
            return σ(filter)(this × otherRelation);
        }

    // equi join using bowtie symbol (U+22C8)
    def ⋈[OtherDomain <: AnyRef, Key <: AnyRef, Range <: AnyRef](leftKey : Domain => Key, rightKey : OtherDomain => Key)(otherRelation : LazyView[OtherDomain])(factory : (Domain, OtherDomain) => Range) : MaterializedView[Range] =
        new HashEquiJoin(lazyViewToIndexedView(left), lazyViewToIndexedView(otherRelation), leftKey, rightKey, factory)

}

case class InfixFunctionConcatenator[Domain <: AnyRef, Range <: AnyRef](
        val left : LazyView[Domain],
        val leftFunction : Domain => Range) {

    import RelationalAlgebraSyntax._

    import Conversions._

    def ⋈[OtherDomain <: AnyRef, Result <: AnyRef](rightKey : OtherDomain => Range,
                                                   otherRelation : LazyView[OtherDomain])(factory : (Domain, OtherDomain) => Result) : MaterializedView[Result] =
        new HashEquiJoin(lazyViewToIndexedView(left), lazyViewToIndexedView(otherRelation), leftFunction, rightKey, factory)

}

object RelationalAlgebraSyntax {
    import sae.collections.QueryResult

    // convenience forwarding to not always import conversion, but only the syntax
    implicit def lazyViewToResult[V <: AnyRef](lazyView : LazyView[V]) : QueryResult[V] = sae.collections.Conversions.lazyViewToResult(lazyView)

    implicit def viewToConcatenator[Domain <: AnyRef](relation : LazyView[Domain]) : InfixConcatenator[Domain] =
        InfixConcatenator(relation)

    implicit def viewAndFunToConcatenator[Domain <: AnyRef, Range <: AnyRef](
        tuple : (LazyView[Domain], Domain => Range)) : InfixFunctionConcatenator[Domain, Range] =
        InfixFunctionConcatenator(tuple._1, tuple._2)

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
                return new BagProjection[Domain, Range](projection, relation);
            }

        def unapply[Domain <: AnyRef, Range <: AnyRef](p : Projection[Domain, Range]) : Option[(Domain => Range, LazyView[Domain])] = Some((p.projection, p.relation))
    }

    /** definitions of cross product syntax **/
    // see also infix syntax
    object × {
        // def apply[DomainA <: AnyRef, DomainB <: AnyRef](relationA : Relation[DomainA], relationB: Relation[DomainB]) : Relation[(DomainA, DomainB)] = cross_product(relationA, relationB)

        //def unapply()
    }

    /** definitions of duplicate elimination syntax **/
    object δ {
        def apply[Domain <: AnyRef](relation : LazyView[Domain]) : LazyView[Domain] =
            new SetDuplicateElimination(relation)

        def apply[Domain <: AnyRef, Range <: AnyRef](proj : Π.type) : LazyView[Domain] = { println("PI"); return null }
        /*
    	   proj match
    	{
    		case Π(projection : Domain => Range, relation : LazyView[Domain]) => new SetProjection(projection, relation)
    	}    
    	*/
        def unapply[Domain <: AnyRef](d : DuplicateElimination[Domain]) : Option[LazyView[Domain]] = Some(d.relation)
    }

    /** definitions of aggregation syntax **/
    object γ {

        def apply[Domain <: AnyRef, Key <: Any, AggregationValue <: Any, Result <: AnyRef](
            source : LazyView[Domain],
            groupFunction : Domain => Key,
            aggregationFuncFactory : NotSelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue],
            aggragationConstructorFunc : (Key, AggregationValue) => Result) : Aggregation[Domain, Key, AggregationValue, Result] =
            {
                new AggregationIntern(source, groupFunction, aggregationFuncFactory, aggragationConstructorFunc)
            }

        def apply[Domain <: AnyRef, AggregationValue <: Any](
            source : LazyView[Domain],
            aggregationFuncFactory : NotSelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue]) =
            {
                new AggregationIntern(source, (x : Any) => "a", aggregationFuncFactory, (x : Any, y : AggregationValue) => Some(y))
            }
    }

    /** definitions of sort syntax **/
    object τ {

    }
}
