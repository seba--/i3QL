package sae
package operators

import intern._



/**
 * An aggregate operator receives a list of functions and a list of attributes.
 * The functions are evaluated on all tuples that coincide in all supplied attributes.
 * Each function has one (or several) attributes as its domain.
 * The list of attributes serves as a grouping key. This key is used to split the relation into
 * a relation that has only distinct combinations in the supplied attributes.
 * (Note that this grouping is a projection in terms of Codds original relational algebra)
 * The list of grouping attributes is optional.
 * If no grouping is supplied, the aggregation functions are applied on the entire relation.
 * If no function is supplied the aggregation has no effect.
 */
trait Aggregation[Domain <: AnyRef, Key <: Any, AggregationValue <: Any, Result <: AnyRef, AggregationFunctionType <: AggregationFunction[Domain, AggregationValue], AggregationFunctionFactoryType <: AggregationFunctionFactory[Domain, AggregationValue, AggregationFunctionType]]
  extends LazyView[Result] {
  val source: LazyView[Domain]
  val groupingFunction: Domain => Key
  val aggregationFunctionFactory: AggregationFunctionFactoryType
  val convertKeyAndAggregationValueToResult: (Key, AggregationValue) => Result

}


object Aggregation {
  /**
   * Construct a new Aggregation for aggregation function that are NOT self maintainable
   * {@see sae.operators.NotSelfMaintainalbeAggregationFunctionFactory}
   * @param source: Source relation
   * @param groupingFunction: Grouping function for the Aggregation. The return value is used as a key in a hashmap.
   * @param aggregationFunctionFactory: a factory that creates aggregatonFunctions  {@see sae.functions}
   * @param convertKeyAndAggregationValueToResult: fucntion that defines the return type of the aggregation. (x : Grouping key(s), y : aggregation function return value) => Aggregation return value
   */
  def apply[Domain <: AnyRef, Key <: Any, AggregationValue <: Any, Result <: AnyRef](source: LazyView[Domain],
                                                                                     groupingFunction: Domain => Key,
                                                                                     aggregationFunctionFactory: NotSelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue],
                                                                                     convertKeyAndAggregationValueToResult: (Key, AggregationValue) => Result):
  Aggregation[Domain, Key, AggregationValue, Result, NotSelfMaintainalbeAggregationFunction[Domain, AggregationValue], NotSelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue]] = {
    new AggregationForNotSelfMaintainableFunctions[Domain, Key, AggregationValue, Result](source, groupingFunction, aggregationFunctionFactory, convertKeyAndAggregationValueToResult)
  }


  def apply[Domain <: AnyRef, Key <: Any, AggregationValue <: Any](source: LazyView[Domain],
                                                                                     groupingFunction: Domain => Key,
                                                                                     aggregationFunctionFactory: NotSelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue]):
  Aggregation[Domain, Key, AggregationValue, (Key,AggregationValue), NotSelfMaintainalbeAggregationFunction[Domain, AggregationValue], NotSelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue]] = {
    new AggregationForNotSelfMaintainableFunctions[Domain, Key, AggregationValue, (Key,AggregationValue)](source, groupingFunction, aggregationFunctionFactory, (a : Key, b : AggregationValue) => (a,b))
  }

  /**
   * Construct a new Aggregation for aggregation function that are SELF maintainable
   * {@see sae.operators.SelfMaintainalbeAggregationFunctionFactory}
   * @param source: Source relation
   * @param groupingFunction: Grouping function for the Aggregation. The return value is used as a key in a hashmap.
   * @param aggregationFunctionFactory: a factory that creates aggregatonFunctions  {@see sae.functions}.
   * @param convertKeyAndAggregationValueToResult: fucntion that defines the return type of the aggregation. (x : Grouping key(s), y : aggregation function return value) => Aggregation return value
   */
  def apply[Domain <: AnyRef, Key <: Any, AggregationValue <: Any, Result <: AnyRef](source: LazyView[Domain], groupFunction: Domain => Key, aggregationFunctionFactory: SelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue],
                                                                                     convertKeyAndAggregationValueToResult: (Key, AggregationValue) => Result):
  Aggregation[Domain, Key, AggregationValue, Result, SelfMaintainalbeAggregationFunction[Domain, AggregationValue], SelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue]] = {
    new AggregationForSelfMaintainableAggregationFunctions[Domain, Key, AggregationValue, Result](source, groupFunction, aggregationFunctionFactory, convertKeyAndAggregationValueToResult)
  }


    def apply[Domain <: AnyRef, Key <: Any, AggregationValue <: Any](source: LazyView[Domain],
                                                                                     groupingFunction: Domain => Key,
                                                                                     aggregationFunctionFactory: SelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue]):
  Aggregation[Domain, Key, AggregationValue, (Key,AggregationValue), SelfMaintainalbeAggregationFunction[Domain, AggregationValue], SelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue]] = {
    new AggregationForSelfMaintainableAggregationFunctions[Domain, Key, AggregationValue,  (Key,AggregationValue)](source, groupingFunction, aggregationFunctionFactory, (a : Key, b : AggregationValue) => (a,b))
  }

  /**
   * Construct a new Aggregation for aggregation function that are NOT self maintainable without any grouping
   * (the aggregation function is used on the whole source relation)
   * {@see sae.operators.NotSelfMaintainalbeAggregationFunctionFactory}
   * @param source: Source relation
   * @param groupingFunction: Grouping function for the Aggregation. The return value is used as a key in a hashmap.
   * @param aggregationFunctionFactory: a factory that creates aggregatonFunctions  {@see sae.functions}.
   * @param convertKeyAndAggregationValueToResult: fucntion that defines the return type of the aggregation. (x : Grouping key(s), y : aggregation function return value) => Aggregation return value
   */
  def apply[Domain <: AnyRef, AggregationValue <: Any](source: LazyView[Domain], aggregationFunctionFactory: NotSelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue]) = {
    new AggregationForNotSelfMaintainableFunctions(source, (x: Any) => "a", aggregationFunctionFactory, (x: Any, y: AggregationValue) => Some(y))
  }

  /**
   * Construct a new Aggregation for aggregation function that are SELF maintainable without any grouping
   * (the aggregation function is used on the whole source relation)
   * {@see sae.operators.SelfMaintainalbeAggregationFunctionFactory}
   * @param source: Source relation
   * @param groupingFunction: Grouping function for the Aggregation. The return value is used as a key in a hashmap.
   * @param aggregationFunctionFactory: a factory that creates aggregatonFunctions  {@see sae.functions}.
   * @param convertKeyAndAggregationValueToResult: fucntion that defines the return type of the aggregation. (x : Grouping key(s), y : aggregation function return value) => Aggregation return value
   */
  def apply[Domain <: AnyRef, AggregationValue <: Any](source: LazyView[Domain], aggregationFunctionFactory: SelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue]) = {
    new AggregationForSelfMaintainableAggregationFunctions(source, (x: Any) => "a", aggregationFunctionFactory, (x: Any, y: AggregationValue) => Some(y))
  }

}

