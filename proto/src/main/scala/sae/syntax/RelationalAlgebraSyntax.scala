package sae
package syntax

import sae.operators._
import sae.operators.intern._
import sae.LazyView

case class InfixConcatenator[Domain <: AnyRef](left: LazyView[Domain]) {

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

    // FIXME the type system for operators should make views covariant
    def ∪[CommonSuperClass >: Domain <: AnyRef, OtherDomain <: CommonSuperClass](otherRelation: LazyView[OtherDomain]): LazyView[CommonSuperClass] = new BagUnion[CommonSuperClass, Domain, OtherDomain](left, otherRelation)

    def ∩(otherRelation: LazyView[Domain]): LazyView[Domain] = new BagIntersection[Domain](lazyViewToIndexedView(left), lazyViewToIndexedView(otherRelation))

    def ∖(otherRelation: LazyView[Domain]): LazyView[Domain] = new BagDifference[Domain](lazyViewToIndexedView(left), lazyViewToIndexedView(otherRelation))

}

case class InfixFunctionConcatenator[Domain <: AnyRef, Range <: AnyRef](
                                                                         left: LazyView[Domain],
                                                                         leftFunction: Domain => Range
                                                                         ) {

  import Conversions._

  def ⋈[OtherDomain <: AnyRef, Result <: AnyRef](
                                                  rightKey: OtherDomain => Range,
                                                  otherRelation: LazyView[OtherDomain]
                                                  )(factory: (Domain, OtherDomain) => Result): MaterializedView[Result] =
    new HashEquiJoin(lazyViewToIndexedView(left), lazyViewToIndexedView(otherRelation), leftFunction, rightKey, factory)

}


case class ElementOf[Domain <: AnyRef](relation: MaterializedView[Domain]) extends (Domain => Boolean)
{
    def apply(e : Domain) = relation.contains(e)
}
case class NotElementOf[Domain <: AnyRef](relation: MaterializedView[Domain]) extends (Domain => Boolean)
{
    def apply(e : Domain) = !relation.contains(e)
}

case class ElementConcatenator[Domain <: AnyRef]( element: Domain )
{
  def ∈(relation: LazyView[Domain]) = RelationalAlgebraSyntax.∈(relation)

  def ∉(relation: LazyView[Domain]) = RelationalAlgebraSyntax.∉(relation)
}



object RelationalAlgebraSyntax {

  import sae.collections.QueryResult

  import operators.Conversions._

  // convenience forwarding to not always import conversion, but only the syntax
  implicit def lazyViewToResult[V <: AnyRef](lazyView: LazyView[V]): QueryResult[V] = sae.collections.Conversions.lazyViewToResult(lazyView)

  implicit def viewToConcatenator[Domain <: AnyRef](relation: LazyView[Domain]): InfixConcatenator[Domain] =
    InfixConcatenator(relation)

  implicit def viewAndFunToConcatenator[Domain <: AnyRef, Range <: AnyRef](tuple: (LazyView[Domain], Domain => Range)): InfixFunctionConcatenator[Domain, Range] =
    InfixFunctionConcatenator(tuple._1, tuple._2)

  implicit def valueToConcatenator[Domain <: AnyRef](value: Domain) = ElementConcatenator(value)

  //implicit def functionToConcatenator[Domain <: AnyRef, Range <: AnyRef](f: Domain => Range) = FunctionConcatenator(f)



  object TC {
    // TODO think of better names for start/endVertex functions
    def apply[Domain <: AnyRef, Vertex <: AnyRef](relation: LazyView[Domain])(startVertex: Domain => Vertex, endVertex: Domain => Vertex) = new HashTransitiveClosure[Domain, Vertex](relation, startVertex, endVertex)

    def unapply[Domain <: AnyRef, Vertex <: AnyRef](closure: TransitiveClosure[Domain, Vertex]) =
      Some(closure.source, closure.getHead, closure.getTail)
  }

  object ∈ {
    def apply[Domain <: AnyRef](relation: LazyView[Domain]) = ElementOf(relation)

    def unapply[Domain <: AnyRef](elementOf: ElementOf[Domain]) = Some(elementOf.relation)
  }


  object ∉ {

    def apply[Domain <: AnyRef](relation: LazyView[Domain]) = NotElementOf(relation)

    def unapply[Domain <: AnyRef](notElementOf: NotElementOf[Domain]) = Some(notElementOf.relation)
  }

  /**definitions of selection syntax **/
  object σ {
    def apply[Domain <: AnyRef](filter: Domain => Boolean)
                               (relation: LazyView[Domain]): LazyView[Domain] = filter match
        {
            case ∈(other) => relation ∩ other.asInstanceOf[LazyView[Domain]] // TODO can we forego these typecasts?
            case ∉(other) => relation ∖ other.asInstanceOf[LazyView[Domain]] // TODO can we forego these typecasts?
            case _ => new LazySelection[Domain](filter, relation)
        }

    def unapply[Domain <: AnyRef](s: Selection[Domain]): Option[(Domain => Boolean, LazyView[Domain])] = Some((s.filter, s.relation))

    // polymorhpic selection

    class PolymorphSelection[T <: AnyRef] {

      def apply[Domain >: T <: AnyRef](relation: LazyView[Domain])(implicit m: ClassManifest[T]) =
        new LazySelection[Domain]((e: Domain) => polymorphFilter[Domain](e, m), relation)

      def polymorphFilter[Domain >: T](e: Domain, m: ClassManifest[T]) = m.erasure.isInstance(e)

    }

    def apply[T <: AnyRef] = new PolymorphSelection[T]
  }


  /**definitions of projection syntax **/
  object Π {
    def apply[Domain <: AnyRef, Range <: AnyRef](projection: Domain => Range)
                                                (relation: LazyView[Domain]): LazyView[Range] = new BagProjection[Domain, Range](projection, relation)

    def unapply[Domain <: AnyRef, Range <: AnyRef](p: Projection[Domain, Range]): Option[(Domain => Range, LazyView[Domain])] = Some((p.projection, p.relation))

    // polymorhpic projection
    class PolymorphProjection[T <: AnyRef] {
      def apply[Domain >: T <: AnyRef](relation: LazyView[Domain]) : LazyView[T] =
        new BagProjection[Domain, T](polymorphProjection[Domain] _, relation)

      def polymorphProjection[Domain >: T](e: Domain): T = e.asInstanceOf[T]
    }

    def apply[T <: AnyRef] = new PolymorphProjection[T]


  }

  /**definitions of cross product syntax **/
  // see also infix syntax
  object × {

  }

  object ⋈ {
    def unapply[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef, Key <: AnyRef](join: EquiJoin[DomainA, DomainB, Range, Key]): Option[(LazyView[DomainA], DomainA => Key, LazyView[DomainB], DomainB => Key)] =
      Some((join.left, join.leftKey, join.right, join.rightKey))
  }

  /**definitions of duplicate elimination syntax **/
  object δ {
    def apply[Domain <: AnyRef](relation: LazyView[Domain]): LazyView[Domain] =
      new SetDuplicateElimination(relation)

    def unapply[Domain <: AnyRef](d: DuplicateElimination[Domain]): Option[LazyView[Domain]] = Some(d.relation)
  }


  /**definitions of aggregation syntax **/
  object γ {

    def apply[Domain <: AnyRef, Key <: Any, AggregationValue <: Any, Result <: AnyRef](source: LazyView[Domain],
                                                                                       groupingFunction: Domain => Key,
                                                                                       aggregationFunctionFactory: NotSelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue],
                                                                                       convertKeyAndAggregationValueToResult: (Key, AggregationValue) => Result):
    Aggregation[Domain, Key, AggregationValue, Result, NotSelfMaintainalbeAggregationFunction[Domain, AggregationValue], NotSelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue]] = {
      new AggregationForNotSelfMaintainableFunctions[Domain, Key, AggregationValue, Result](source, groupingFunction, aggregationFunctionFactory, convertKeyAndAggregationValueToResult)
    }


    def apply[Domain <: AnyRef, Key <: Any, AggregationValue <: Any, Result <: AnyRef](source: LazyView[Domain], groupFunction: Domain => Key, aggregationFunctionFactory: SelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue],
                                                                                       convertKeyAndAggregationValueToResult: (Key, AggregationValue) => Result):
    Aggregation[Domain, Key, AggregationValue, Result, SelfMaintainalbeAggregationFunction[Domain, AggregationValue], SelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue]] = {
      new AggregationForSelfMaintainableAggregationFunctions[Domain, Key, AggregationValue, Result](source, groupFunction, aggregationFunctionFactory, convertKeyAndAggregationValueToResult)
    }


    def apply[Domain <: AnyRef, AggregationValue <: Any](source: LazyView[Domain], aggregationFunctionFactory: NotSelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue]) = {
      new AggregationForNotSelfMaintainableFunctions(source, (x: Any) => "a", aggregationFunctionFactory, (x: Any, y: AggregationValue) => Some(y))
    }


    def apply[Domain <: AnyRef, AggregationValue <: Any](source: LazyView[Domain], aggregationFunctionFactory: SelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue]) = {
      new AggregationForSelfMaintainableAggregationFunctions(source, (x: Any) => "a", aggregationFunctionFactory, (x: Any, y: AggregationValue) => Some(y))
    }

    def apply[Domain <: AnyRef, Key <: Any, AggregationValue <: Any](source: LazyView[Domain],
                                                                     groupingFunction: Domain => Key,
                                                                     aggregationFunctionFactory: NotSelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue]):
    Aggregation[Domain, Key, AggregationValue, (Key, AggregationValue), NotSelfMaintainalbeAggregationFunction[Domain, AggregationValue], NotSelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue]] = {
      new AggregationForNotSelfMaintainableFunctions[Domain, Key, AggregationValue, (Key, AggregationValue)](source, groupingFunction, aggregationFunctionFactory, (a: Key, b: AggregationValue) => (a, b))
    }

    def apply[Domain <: AnyRef, Key <: Any, AggregationValue <: Any](source: LazyView[Domain],
                                                                     groupingFunction: Domain => Key,
                                                                     aggregationFunctionFactory: SelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue]):
    Aggregation[Domain, Key, AggregationValue, (Key, AggregationValue), SelfMaintainalbeAggregationFunction[Domain, AggregationValue], SelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue]] = {
      new AggregationForSelfMaintainableAggregationFunctions[Domain, Key, AggregationValue, (Key, AggregationValue)](source, groupingFunction, aggregationFunctionFactory, (a: Key, b: AggregationValue) => (a, b))
    }


  }


  /**definitions of sort syntax **/
  object τ {

  }


  object ∪ {

    def unapply[Range <: AnyRef, DomainA <: Range, DomainB <: Range](union: Union[Range, DomainA, DomainB]): Option[(LazyView[DomainA], LazyView[DomainB])] = Some((union.left, union.right))

  }

}
