package sae
package syntax

import sae.operators._
import sae.operators.intern._
import functions.ElementOf
import functions.NotElementOf

case class InfixConcatenator[Domain <: AnyRef](left: LazyView[Domain])
{

    import RelationalAlgebraSyntax._

    import Conversions._

    def ×[OtherDomain <: AnyRef](otherRelation: LazyView[OtherDomain]): LazyView[(Domain, OtherDomain)] = new CrossProduct(lazyViewToMaterializedView(left), lazyViewToMaterializedView(otherRelation))

    // general join using bowtie symbol (U+22C8)


    def ⋈[OtherDomain <: AnyRef](
        filter: ((Domain, OtherDomain)) => Boolean,
        otherRelation: LazyView[OtherDomain]
    ): LazyView[(Domain, OtherDomain)] = σ(filter)(this × otherRelation);

    // equi join using bowtie symbol (U+22C8)
    def ⋈[OtherDomain <: AnyRef, Key <: AnyRef, Range <: AnyRef](leftKey: Domain => Key, rightKey: OtherDomain => Key)
                (otherRelation: LazyView[OtherDomain])
                (factory: (Domain, OtherDomain) => Range): MaterializedView[Range] =
        new HashEquiJoin(lazyViewToIndexedView(left), lazyViewToIndexedView(otherRelation), leftKey, rightKey, factory)

    def ∪[CommonSuperClass >: Domain <: AnyRef, OtherDomain <: CommonSuperClass](otherRelation: LazyView[OtherDomain]): LazyView[CommonSuperClass] = new BagUnion[CommonSuperClass, Domain, OtherDomain](left, otherRelation)

    def ∩(otherRelation: LazyView[Domain]): LazyView[Domain] = new BagIntersection[Domain](lazyViewToIndexedView(left), lazyViewToIndexedView(otherRelation))

    def ∖(otherRelation: LazyView[Domain]): LazyView[Domain] = new BagDifference[Domain](lazyViewToIndexedView(left), lazyViewToIndexedView(otherRelation))
}

case class InfixFunctionConcatenator[Domain <: AnyRef, Range <: AnyRef](
    left: LazyView[Domain],
    leftFunction: Domain => Range
)
{

    import Conversions._

    def ⋈[OtherDomain <: AnyRef, Result <: AnyRef](
        rightKey: OtherDomain => Range,
        otherRelation: LazyView[OtherDomain]
    )
                (factory: (Domain, OtherDomain) => Result): MaterializedView[Result] =
        new HashEquiJoin(lazyViewToIndexedView(left), lazyViewToIndexedView(otherRelation), leftFunction, rightKey, factory)

}


case class ElementConcatenator[Domain <: AnyRef](
    element: Domain
)
{

    import operators.Conversions._

    def ∈(relation: LazyView[Domain]) = ElementOf(lazyViewToMaterializedView(relation))

    def ∉(relation: LazyView[Domain]) = NotElementOf(lazyViewToMaterializedView(relation))
}

object RelationalAlgebraSyntax
{

    import sae.collections.QueryResult

    import operators.Conversions._

    // convenience forwarding to not always import conversion, but only the syntax
    implicit def lazyViewToResult[V <: AnyRef](lazyView: LazyView[V]): QueryResult[V] = sae.collections.Conversions.lazyViewToResult(lazyView)

    implicit def viewToConcatenator[Domain <: AnyRef](relation: LazyView[Domain]): InfixConcatenator[Domain] =
        InfixConcatenator(relation)

    implicit def viewAndFunToConcatenator[Domain <: AnyRef, Range <: AnyRef](tuple: (LazyView[Domain], Domain => Range)): InfixFunctionConcatenator[Domain, Range] =
        InfixFunctionConcatenator(tuple._1, tuple._2)

    implicit def valueToConcatenator[Domain <: AnyRef](value: Domain) =
        ElementConcatenator(value)


    /**definitions of selection syntax **/
    object σ
    {
        def apply[Domain <: AnyRef](filter: Domain => Boolean)
                    (relation: LazyView[Domain]): LazyView[Domain] =
            if (filter.isInstanceOf[Observable[(Domain,Boolean)]]) {

                new DynamicFilterSelection[Domain](filter.asInstanceOf[(Domain => Boolean) with Observable[(Domain,Boolean)]], lazyViewToIndexedView(relation))
            }
            else {
                new LazySelection[Domain](filter, relation)
            }

        // polymorhpic selection

        class PolymorphSelection[T <: AnyRef]
        {

            def apply[Domain >: T <: AnyRef](relation: LazyView[Domain])(implicit m: ClassManifest[T]) =
                new LazySelection[Domain]((e: Domain) => polymorphFilter[Domain](e, m), relation)

            def polymorphFilter[Domain >: T](e: Domain, m: ClassManifest[T]) = m.erasure.isInstance(e)

        }

        def apply[T <: AnyRef] = new PolymorphSelection[T]


    }

    /**definitions of projection syntax **/
    object Π
    {
        def apply[Domain <: AnyRef, Range <: AnyRef](projection: Domain => Range)
                    (relation: LazyView[Domain]): LazyView[Range] = new BagProjection[Domain, Range](projection, relation)

        def unapply[Domain <: AnyRef, Range <: AnyRef](p: Projection[Domain, Range]): Option[(Domain => Range, LazyView[Domain])] = Some((p.projection, p.relation))

        // polymorhpic projection
        class PolymorphProjection[T <: AnyRef]
        {
            def apply[Domain >: T <: AnyRef](relation: LazyView[Domain]) =
                new BagProjection[Domain, T](polymorphProjection[Domain] _, relation)

            def polymorphProjection[Domain >: T](e: Domain): T = e.asInstanceOf[T]
        }

        def apply[T <: AnyRef] = new PolymorphProjection[T]


    }

    /**definitions of cross product syntax **/
    // see also infix syntax
    object ×
    {
        // def apply[DomainA <: AnyRef, DomainB <: AnyRef](relationA : Relation[DomainA], relationB: Relation[DomainB]) : Relation[(DomainA, DomainB)] = cross_product(relationA, relationB)

        //def unapply()
    }

    /**definitions of duplicate elimination syntax **/
    object δ
    {
        def apply[Domain <: AnyRef](relation: LazyView[Domain]): LazyView[Domain] =
            new SetDuplicateElimination(relation)

        def unapply[Domain <: AnyRef](d: DuplicateElimination[Domain]): Option[LazyView[Domain]] = Some(d.relation)
    }


    /**definitions of aggregation syntax **/
    object γ
    {

        def apply[Domain <: AnyRef, Key <: Any, AggregationValue <: Any, Result <: AnyRef](
            source: LazyView[Domain],
            groupFunction: Domain => Key,
            aggregationFuncFactory: NotSelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue],
            aggragationConstructorFunc: (Key, AggregationValue) => Result
        ): Aggregation[Domain, Key, AggregationValue, Result] =
            new AggregationIntern(source, groupFunction, aggregationFuncFactory, aggragationConstructorFunc)


        def apply[Domain <: AnyRef, Key <: Any, AggregationValue <: Any, Result <: AnyRef](
            source: LazyView[Domain],
            groupFunction: Domain => Key,
            aggregationFuncFactory: SelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue],
            aggragationConstructorFunc: (Key, AggregationValue) => Result
        ): Aggregation[Domain, Key, AggregationValue, Result]
        = new AggregationForSelfMaintainableAggregationFunctions(source, groupFunction, aggregationFuncFactory, aggragationConstructorFunc)

        def apply[Domain <: AnyRef, AggregationValue <: Any](
            source: LazyView[Domain],
            aggregationFuncFactory: NotSelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue]
        ) =
            new AggregationIntern(source, (x: Any) => "a", aggregationFuncFactory, (
                x: Any,
                y: AggregationValue
            ) => Some(y))

        def apply[Domain <: AnyRef, AggregationValue <: Any](
            source: LazyView[Domain],
            aggregationFuncFactory: SelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue]
        ) =
            new AggregationForSelfMaintainableAggregationFunctions(source, (x: Any) => "a", aggregationFuncFactory, (
                x: Any,
                y: AggregationValue
            ) => Some(y))
    }


    /**definitions of sort syntax **/
    object τ
    {

    }

}
