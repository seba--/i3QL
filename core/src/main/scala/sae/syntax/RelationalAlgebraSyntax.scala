package sae
package syntax

import sae.operators._
import impl.{CrossProductView, AddMultiSetUnion}
import sae.operators.intern._
import sae.Relation

case class InfixConcatenator[Domain <: AnyRef](left: Relation[Domain])
{

    import RelationalAlgebraSyntax._

    import Conversions._

    def ×[OtherDomain <: AnyRef] (otherRelation: Relation[OtherDomain]): Relation[(Domain, OtherDomain)] = new CrossProduct (
        lazyViewToMaterializedView (left),
        lazyViewToMaterializedView (otherRelation)
    )

    // general join using bowtie symbol (U+22C8)
    def ⋈[OtherDomain <: AnyRef] (
                                     filter: ((Domain, OtherDomain)) => Boolean,
                                     otherRelation: Relation[OtherDomain]
                                     ): Relation[(Domain, OtherDomain)] = σ (filter)(this × otherRelation);

    // equi join using bowtie symbol (U+22C8)
    def ⋈[OtherDomain <: AnyRef, Key <: AnyRef, Range <: AnyRef] (leftKey: Domain => Key, rightKey: OtherDomain => Key)
                                                                 (otherRelation: Relation[OtherDomain])
                                                                 (factory: (Domain, OtherDomain) => Range): OLDMaterializedView[Range] =
        new HashEquiJoin (
            lazyViewToIndexedView (left),
            lazyViewToIndexedView (otherRelation),
            leftKey,
            rightKey,
            factory
        )

    // FIXME the type system for operators should make views covariant
    def ∪[CommonSuperClass >: Domain <: AnyRef, OtherDomain <: CommonSuperClass] (otherRelation: Relation[OtherDomain]): Relation[CommonSuperClass] =
        new AddMultiSetUnion[CommonSuperClass, Domain, OtherDomain](
            left,
            otherRelation
        )

    def ∩ (otherRelation: Relation[Domain]): Relation[Domain] =
        new BagIntersection[Domain](
            lazyViewToIndexedView (left),
            lazyViewToIndexedView (otherRelation)
        )

    def ∖ (otherRelation: Relation[Domain]): Relation[Domain] =
        new BagDifference[Domain](
            lazyViewToIndexedView (left),
            lazyViewToIndexedView (otherRelation)
        )

}

case class InfixFunctionConcatenator[Domain <: AnyRef, Range <: AnyRef](
                                                                           left: Relation[Domain],
                                                                           leftFunction: Domain => Range
                                                                           )
{

    import RelationalAlgebraSyntax._

    import Conversions._

    // equi-join
    def ⋈[OtherDomain <: AnyRef, Result <: AnyRef] (
                                                       rightKey: OtherDomain => Range,
                                                       otherRelation: Relation[OtherDomain]
                                                       )
                                                   (factory: (Domain, OtherDomain) => Result): OLDMaterializedView[Result] =
        new HashEquiJoin (
            lazyViewToIndexedView (left),
            lazyViewToIndexedView (otherRelation),
            leftFunction,
            rightKey,
            factory
        )

    // semi-join
    def ⋉[OtherDomain <: AnyRef] (
                                     rightKey: OtherDomain => Range,
                                     otherRelation: Relation[OtherDomain]
                                     ) =
        ⋈ (identity (_: Range), δ (Π (rightKey)(otherRelation))) {
            (left: Domain, right: Range) => left
        }

    //left ∖ (left ∖ ( ⋈ (rightKey, otherRelation){ (left : Domain, right : OtherDomain) => left }))

    // anti semi-join
    def ⊳[OtherDomain <: AnyRef] (
                                     rightKey: OtherDomain => Range,
                                     otherRelation: Relation[OtherDomain]
                                     ) =
        left ∖ (⋉ (rightKey, otherRelation))

}


object RelationalAlgebraSyntax
{

    import sae.collections.QueryResult


    // convenience forwarding to not always import conversion, but only the syntax
    implicit def lazyViewToResult[V <: AnyRef](lazyView: Relation[V]): QueryResult[V] = sae.collections.Conversions
        .lazyViewToResult (
        lazyView
    )

    implicit def viewToConcatenator[Domain <: AnyRef](relation: Relation[Domain]): InfixConcatenator[Domain] =
        InfixConcatenator (relation)

    implicit def viewAndFunToConcatenator[Domain <: AnyRef, Range <: AnyRef](tuple: (Relation[Domain], Domain => Range)): InfixFunctionConcatenator[Domain, Range] =
        InfixFunctionConcatenator (tuple._1, tuple._2)

    object TC
    {
        // TODO think of better names for start/endVertex functions
        def apply[Domain <: AnyRef, Vertex <: AnyRef](relation: Relation[Domain])
                                                     (startVertex: Domain => Vertex, endVertex: Domain => Vertex) =
            new HashTransitiveClosure[Domain, Vertex](
                relation,
                startVertex,
                endVertex
            )

        def unapply[Domain <: AnyRef, Vertex <: AnyRef](closure: TransitiveClosure[Domain, Vertex]) =
            Some (closure.source, closure.getHead, closure.getTail)
    }

    /** definitions of selection syntax **/
    object σ
    {
        def apply[Domain <: AnyRef](filter: Domain => Boolean)(relation: Relation[Domain]): Relation[Domain] =
            new LazySelection[Domain](filter, relation)

        def unapply[Domain <: AnyRef](s: Selection[Domain]): Option[(Domain => Boolean, Relation[Domain])] = Some ((s
            .filter, s.relation))

        // polymorhpic selection, omit the selection function for a type parameter, that selects all entries of this type
        class PolymorphSelection[T <: AnyRef]
        {

            def apply[Domain >: T <: AnyRef](relation: Relation[Domain])(implicit m: ClassManifest[T]) =
                new LazySelection[Domain]((e: Domain) => polymorphFilter[Domain](e, m), relation)

            def polymorphFilter[Domain >: T](e: Domain, m: ClassManifest[T]) = m.erasure.isInstance (e)

        }

        def apply[T <: AnyRef] = new PolymorphSelection[T]
    }


    /** definitions of projection syntax **/
    object Π
    {
        def apply[Domain <: AnyRef, Range <: AnyRef](projection: Domain => Range)
                                                    (relation: Relation[Domain]): Relation[Range] = new BagProjection[Domain, Range](
            projection,
            relation
        )

        def unapply[Domain <: AnyRef, Range <: AnyRef](p: Projection[Domain, Range]): Option[(Domain => Range, Relation[Domain])] = Some (
            (p.projection, p.relation)
        )

        // polymorhpic projection
        class PolymorphProjection[T <: AnyRef]
        {
            def apply[Domain >: T <: AnyRef](relation: Relation[Domain]): Relation[T] =
                new BagProjection[Domain, T](polymorphProjection[Domain] _, relation)

            def polymorphProjection[Domain >: T](e: Domain): T = e.asInstanceOf[T]
        }

        def apply[T <: AnyRef] = new PolymorphProjection[T]


    }

    /** definitions of cross product syntax **/
    // see also infix syntax
    object ×
    {

    }

    object ⋈
    {
        def unapply[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef, Key <: AnyRef](join: EquiJoin[DomainA, DomainB, Range, Key]): Option[(Relation[DomainA], DomainA => Key, Relation[DomainB], DomainB => Key, (DomainA, DomainB) => Range)] =
            Some ((join.left, join.leftKey, join.right, join.rightKey, join.joinFunction))
    }

    /** definitions of duplicate elimination syntax **/
    object δ
    {
        def apply[Domain <: AnyRef](relation: Relation[Domain]): Relation[Domain] =
            new SetDuplicateElimination (relation)

        def unapply[Domain <: AnyRef](d: DuplicateElimination[Domain]): Option[Relation[Domain]] = Some (d.relation)
    }


    /** definitions of aggregation syntax **/
    object γ
    {

        def apply[Domain <: AnyRef, Key <: Any, AggregationValue <: Any, Result <: AnyRef](
                                                                                              source: Relation[Domain],
                                                                                              groupingFunction: Domain => Key,
                                                                                              aggregationFunctionFactory: NotSelfMaintainableAggregateFunctionFactory[Domain, AggregationValue],
                                                                                              convertKeyAndAggregationValueToResult: (Key, AggregationValue) => Result
                                                                                              ):
        Aggregation[Domain, Key, AggregationValue, Result, NotSelfMaintainableAggregateFunction[Domain, AggregationValue], NotSelfMaintainableAggregateFunctionFactory[Domain, AggregationValue]] =
        {
            new AggregationForNotSelfMaintainableFunctions[Domain, Key, AggregationValue, Result](
                source,
                groupingFunction,
                aggregationFunctionFactory,
                convertKeyAndAggregationValueToResult
            )
        }


        def apply[Domain <: AnyRef, Key <: Any, AggregationValue <: Any, Result <: AnyRef](
                                                                                              source: Relation[Domain],
                                                                                              groupFunction: Domain => Key,
                                                                                              aggregationFunctionFactory: SelfMaintainableAggregateFunctionFactory[Domain, AggregationValue],
                                                                                              convertKeyAndAggregationValueToResult: (Key, AggregationValue) => Result
                                                                                              ):
        Aggregation[Domain, Key, AggregationValue, Result, SelfMaintainableAggregateFunction[Domain, AggregationValue], SelfMaintainableAggregateFunctionFactory[Domain, AggregationValue]] =
        {
            new AggregationForSelfMaintainableAggregationFunctions[Domain, Key, AggregationValue, Result](
                source,
                groupFunction,
                aggregationFunctionFactory,
                convertKeyAndAggregationValueToResult
            )
        }


        def apply[Domain <: AnyRef, AggregationValue <: Any](
                                                                source: Relation[Domain],
                                                                aggregationFunctionFactory: NotSelfMaintainableAggregateFunctionFactory[Domain, AggregationValue]
                                                                ) =
        {
            new AggregationForNotSelfMaintainableFunctions (
                source,
                (x: Any) => "a",
                aggregationFunctionFactory,
                (x: Any, y: AggregationValue) => Some (y)
            )
        }


        def apply[Domain <: AnyRef, AggregationValue <: Any](source: Relation[Domain],
                                                             aggregationFunctionFactory: SelfMaintainableAggregateFunctionFactory[Domain, AggregationValue]
                                                                ) =
        {
            new AggregationForSelfMaintainableAggregationFunctions (
                source,
                (x: Any) => "a",
                aggregationFunctionFactory,
                (x: Any, y: AggregationValue) => Some (y)
            )
        }

        def apply[Domain <: AnyRef, Key <: Any, AggregationValue <: Any](
                                                                            source: Relation[Domain],
                                                                            groupingFunction: Domain => Key,
                                                                            aggregationFunctionFactory: NotSelfMaintainableAggregateFunctionFactory[Domain, AggregationValue]
                                                                            ):
        Aggregation[Domain, Key, AggregationValue, (Key, AggregationValue), NotSelfMaintainableAggregateFunction[Domain, AggregationValue], NotSelfMaintainableAggregateFunctionFactory[Domain, AggregationValue]] =
        {
            new AggregationForNotSelfMaintainableFunctions[Domain, Key, AggregationValue, (Key, AggregationValue)](
                source,
                groupingFunction,
                aggregationFunctionFactory,
                (a: Key, b: AggregationValue) => (a, b)
            )
        }

        def apply[Domain <: AnyRef, Key <: Any, AggregationValue <: Any](
                                                                            source: Relation[Domain],
                                                                            groupingFunction: Domain => Key,
                                                                            aggregationFunctionFactory: SelfMaintainableAggregateFunctionFactory[Domain, AggregationValue]
                                                                            ):
        Aggregation[Domain, Key, AggregationValue, (Key, AggregationValue), SelfMaintainableAggregateFunction[Domain, AggregationValue], SelfMaintainableAggregateFunctionFactory[Domain, AggregationValue]] =
        {
            new AggregationForSelfMaintainableAggregationFunctions[Domain, Key, AggregationValue, (Key, AggregationValue)](
                source,
                groupingFunction,
                aggregationFunctionFactory,
                (a: Key, b: AggregationValue) => (a, b)
            )
        }


    }


    /** definitions of sort syntax **/
    object τ
    {

    }


    object ∪
    {

        def unapply[Range <: AnyRef, DomainA <: Range, DomainB <: Range](union: Union[Range, DomainA, DomainB]): Option[(Relation[DomainA], Relation[DomainB])] = Some (
            (union.left, union.right)
        )

    }


    object ⋉
    {
        def unapply[Key <: AnyRef, DomainA <: AnyRef, DomainB <: AnyRef](semiJoin: HashEquiJoin[DomainA, DomainB, DomainA, Key]): Option[(Relation[DomainA], DomainA => Key, Relation[DomainB], DomainB => Key)] = semiJoin match {
            case ⋈ (left, leftKey, δ (Π (rightKey: (DomainB => Key), right: Relation[DomainB])), _, _) => Some ((left, leftKey, right, rightKey))
            case _ => None
        }

    }

    /*
        object ∈
        {
            def apply[Domain <: AnyRef](relation: Relation[Domain]) = ElementOf(lazyViewToMaterializedView(relation))
        }


        object ∉
        {
            def apply[Domain <: AnyRef](relation: Relation[Domain]) = NotElementOf(lazyViewToMaterializedView(relation))
        }
    */


    /*
        implicit def valueToSetInclusion[Domain <: AnyRef](value: Domain) =
            SetInclusionConverter(value)

        implicit def functionToSetInclusion[Domain <: AnyRef, Range <: AnyRef](function: Domain => Range) =
            SetProjectionInclusionConverter(function)

        case class ElementOf[Domain <: AnyRef](relation: OLDMaterializedView[Domain]) extends (Domain => Boolean)
        {
            def apply(e: Domain) = relation.contains(e)
        }

        case class ElementOfProjection[Domain <: AnyRef, Range <: AnyRef](
                                                                             projection: Domain => Range,
                                                                             relation: OLDMaterializedView[Range]
                                                                         ) extends (Domain => Boolean)
        {
            def apply(e: Domain) = relation.contains(projection(e))
        }

        case class NotElementOf[Domain <: AnyRef](relation: OLDMaterializedView[Domain]) extends (Domain => Boolean)
        {
            def apply(e: Domain) = !relation.contains(e)
        }

        case class NotElementOfProjection[Domain <: AnyRef, Range <: AnyRef](
                                                                                projection: Domain => Range,
                                                                                relation: OLDMaterializedView[Range]
                                                                            ) extends (Domain => Boolean)
        {
            type Rng = Range

            def apply(e: Domain) = !relation.contains(projection(e))
        }

        case class SetInclusionConverter[Domain <: AnyRef](element: Domain)
        {
            def ∈(relation: Relation[Domain]) = ElementOf(relation)

            def ∉(relation: Relation[Domain]) = NotElementOf(relation)
        }

        case class SetProjectionInclusionConverter[Domain <: AnyRef, Range <: AnyRef](
                                                                                         projection: Domain => Range
                                                                                     )
        {
            def ∈(relation: Relation[Domain]) = ElementOfProjection(projection, lazyViewToMaterializedView(relation))

            def ∉(relation: Relation[Domain]) = NotElementOfProjection(projection, lazyViewToMaterializedView(relation))

            def apply(f: ElementOf[Range]) = ElementOfProjection(projection, f.relation)

            def apply(f: NotElementOf[Range]) = NotElementOfProjection(projection, f.relation)
        }
    */
}
