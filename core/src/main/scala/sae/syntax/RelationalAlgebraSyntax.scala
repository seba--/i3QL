package sae
package syntax

import sae.operators._
import impl._
import sae.Relation
import scala.Some

case class InfixConcatenator[Domain <: AnyRef](left: Relation[Domain])
{

    import RelationalAlgebraSyntax._


    def ×[OtherDomain <: AnyRef] (otherRelation: Relation[OtherDomain]): Relation[(Domain, OtherDomain)] =
        new CrossProductView (
            if (!left.isStored)
            {
                left.asMaterialized
            }
            else
            {
                left
            },
            if (!otherRelation.isStored)
            {
                otherRelation.asMaterialized
            }
            else
            {
                otherRelation
            },
            (l: Domain, r: OtherDomain) => (l, r)
        )

    // general join using bowtie symbol (U+22C8)
    def ⋈[OtherDomain <: AnyRef] (filter: ((Domain, OtherDomain)) => Boolean,
                                  otherRelation: Relation[OtherDomain]): Relation[(Domain, OtherDomain)] =
        σ (filter)(this × otherRelation)

    // equi join using bowtie symbol (U+22C8)
    def ⋈[OtherDomain <: AnyRef, Key <: AnyRef, Range <: AnyRef] (leftKey: Domain => Key, rightKey: OtherDomain => Key)
                                                                 (otherRelation: Relation[OtherDomain])
                                                                 (projection: (Domain, OtherDomain) => Range): Relation[Range] =
        new EquiJoinView (
            left,
            otherRelation,
            leftKey,
            rightKey,
            projection
        )

    def ⊎[CommonSuperClass >: Domain <: AnyRef, OtherDomain <: CommonSuperClass] (otherRelation: Relation[OtherDomain]): Relation[CommonSuperClass] =
        new UnionViewAdd[CommonSuperClass, Domain, OtherDomain](
            left,
            otherRelation
        )

    def ∪[CommonSuperClass >: Domain <: AnyRef, OtherDomain <: CommonSuperClass] (otherRelation: Relation[OtherDomain]): Relation[CommonSuperClass] =
        new UnionViewMax[CommonSuperClass, Domain, OtherDomain](
            left.asMaterialized,
            otherRelation.asMaterialized
        )

    def ∩ (otherRelation: Relation[Domain]): Relation[Domain] =
        new IntersectionView[Domain](
            left.asMaterialized,
            otherRelation.asMaterialized
        )

    def ∖ (otherRelation: Relation[Domain]): Relation[Domain] =
        new DifferenceView[Domain](
            left.asMaterialized,
            otherRelation.asMaterialized
        )

}

case class InfixFunctionConcatenator[Domain <: AnyRef, Range <: AnyRef](left: Relation[Domain],
                                                                        leftFunction: Domain => Range)
{

    import RelationalAlgebraSyntax._


    // equi-join
    def ⋈[OtherDomain <: AnyRef, Result <: AnyRef] (
                                                       rightKey: OtherDomain => Range,
                                                       otherRelation: Relation[OtherDomain]
                                                       )
                                                   (factory: (Domain, OtherDomain) => Result): Relation[Result] =
        new EquiJoinView (
            left,
            otherRelation,
            leftFunction,
            rightKey,
            factory
        )

    // semi-join
    def ⋉[OtherDomain <: AnyRef] (rightKey: OtherDomain => Range,
                                  otherRelation: Relation[OtherDomain]): Relation[Domain] =
        ⋈ (identity (_: Range), δ (Π (rightKey)(otherRelation))) {
            (left: Domain, right: Range) => left
        }

    //left ∖ (left ∖ ( ⋈ (rightKey, otherRelation){ (left : Domain, right : OtherDomain) => left }))

    // anti semi-join
    def ⊳[OtherDomain <: AnyRef] (rightKey: OtherDomain => Range,
                                  otherRelation: Relation[OtherDomain]): Relation[Domain] =
        left ∖ (⋉ (rightKey, otherRelation))

}


object RelationalAlgebraSyntax
{

    implicit def viewToConcatenator[Domain <: AnyRef](relation: Relation[Domain]): InfixConcatenator[Domain] =
        InfixConcatenator (relation)

    implicit def viewAndFunToConcatenator[Domain <: AnyRef, Range <: AnyRef](tuple: (Relation[Domain], Domain => Range)): InfixFunctionConcatenator[Domain, Range] =
        InfixFunctionConcatenator (tuple._1, tuple._2)

    object TC
    {
        // TODO think of better names for start/endVertex functions
        def apply[Domain <: AnyRef, Vertex <: AnyRef](relation: Relation[Domain])
                                                     (startVertex: Domain => Vertex, endVertex: Domain => Vertex) =
            new AcyclicTransitiveClosureView[Domain, Vertex](
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
            new SelectionView[Domain](relation, filter)

        def unapply[Domain <: AnyRef](s: Selection[Domain]): Option[(Domain => Boolean, Relation[Domain])] = Some ((s
            .filter, s.relation))

        // polymorhpic selection, omit the selection function for a type parameter, that selects all entries of this type
        class PolymorphSelection[T <: AnyRef]
        {

            def apply[Domain >: T <: AnyRef](relation: Relation[Domain])(implicit m: ClassManifest[T]) =
                new SelectionView[Domain](relation, (e: Domain) => polymorphFilter[Domain](e, m))

            def polymorphFilter[Domain >: T](e: Domain, m: ClassManifest[T]) = m.erasure.isInstance (e)

        }

        def apply[T <: AnyRef] = new PolymorphSelection[T]
    }


    /** definitions of projection syntax **/
    object Π
    {
        def apply[Domain <: AnyRef, Range <: AnyRef](projection: Domain => Range)
                                                    (relation: Relation[Domain])
                                                    (implicit isSetPreserving: Boolean = false): Relation[Range] =
            if (!isSetPreserving) {
                new ProjectionView[Domain, Range](
                    relation,
                    projection
                )
            }
            else
            {
                new ProjectionViewSetPreserving[Domain, Range](
                    relation,
                    projection
                )
            }


        def unapply[Domain <: AnyRef, Range <: AnyRef](p: Projection[Domain, Range]): Option[(Domain => Range, Relation[Domain])] = Some (
            (p.projection, p.relation)
        )

        // polymorhpic projection
        class PolymorphProjection[T <: AnyRef]
        {
            def apply[Domain >: T <: AnyRef](relation: Relation[Domain]): Relation[T] =
                new ProjectionView[Domain, T](relation, polymorphProjection[Domain] _)

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
            Some ((join.left, join.leftKey, join.right, join.rightKey, join.projection))
    }

    /** definitions of duplicate elimination syntax **/
    object δ
    {
        def apply[Domain <: AnyRef](relation: Relation[Domain]): Relation[Domain] =
            new DuplicateEliminationView[Domain](relation)

        def unapply[Domain <: AnyRef](d: DuplicateElimination[Domain]): Option[Relation[Domain]] = Some (d.relation)
    }


    /** definitions of aggregation syntax **/
    object γ
    {

        def apply[Domain, Key, AggregationValue, Result](
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


        def apply[Domain, Key, AggregationValue, Result](
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


        def apply[Domain, AggregationValue](
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


        def apply[Domain, AggregationValue](source: Relation[Domain],
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

        def apply[Domain, Key, AggregationValue](
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

        def apply[Domain, Key, AggregationValue](
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
        def unapply[Key <: AnyRef, DomainA <: AnyRef, DomainB <: AnyRef](semiJoin: EquiJoinView[DomainA, DomainB, DomainA, Key]): Option[(Relation[DomainA], DomainA => Key, Relation[DomainB], DomainB => Key)] = semiJoin match {
            case ⋈ (left, leftKey, δ (Π (rightKey: (DomainB => Key), right: Relation[DomainB])), _, _) => Some ((left, leftKey, right, rightKey))
            case _ => None
        }

    }

}
