package sae
package operators

import intern._
import scala.collection.mutable.ListBuffer

import sae.collections._
import scala.collection.mutable.Map
import collection.Iterable

import com.google.common.collect.HashMultiset
import collection.JavaConversions._
import java.security.DomainCombiner


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

trait Aggregation[Domain <: AnyRef, Key <: Any, AggregationValue <: Any, Result <: AnyRef,AggregationFunctionType <: AggregationFunction[Domain,AggregationValue], AggregationFunctionFactoryType <: AggregationFunctionFactory[Domain,AggregationValue,AggregationFunctionType]]
  extends LazyView[Result] {
  val source: LazyView[Domain]
  val groupFunction: Domain => Key
  val aggregationFunctionFactory: AggregationFunctionFactoryType
  val convertKeyAndAggregationValueToResult: (Key, AggregationValue) => Result

}




object Aggregation {
  /**
   * @param source: Lasz Source View
   * @param groupFunciton: the grouping function. return value for all elements in one group must be equal by '==' (return value is used in a hashmap)
   * @param aggregationFuncFactory: a simple or complex aggregation function (factory)
   * @param aggregationConstructorFunction: (x : Result of grouping function, y : Result of Aggregation Function) => Aggregation return value
   */
  def apply[Domain <: AnyRef, Key <: Any, AggregationValue <: Any, Result <: AnyRef](source: LazyView[Domain],
                                                                                     groupFunction: Domain => Key,
                                                                                     aggregationFuncFactory: NotSelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue],
                                                                                     aggregationConstructorFunction: (Key, AggregationValue) => Result) :
  Aggregation[Domain, Key,AggregationValue,Result,NotSelfMaintainalbeAggregationFunction[Domain,AggregationValue],NotSelfMaintainalbeAggregationFunctionFactory[Domain,AggregationValue]]= {
    new AggregationForNotSelfMaintainableFunctions[Domain,Key,AggregationValue,Result](source, groupFunction, aggregationFuncFactory, aggregationConstructorFunction)
  }

  def apply[Domain <: AnyRef, Key <: Any, AggregationValue <: Any, Result <: AnyRef](source: LazyView[Domain], groupFunction: Domain => Key, aggregationFuncFactory: SelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue],
                                                                                     aggregationConstructorFunction: (Key, AggregationValue) => Result) :
  Aggregation[Domain, Key,AggregationValue,Result,SelfMaintainalbeAggregationFunction[Domain,AggregationValue],SelfMaintainalbeAggregationFunctionFactory[Domain,AggregationValue]]= {
    new AggregationForSelfMaintainableAggregationFunctions[Domain,Key,AggregationValue,Result](source, groupFunction, aggregationFuncFactory, aggregationConstructorFunction)
  }


  def apply[Domain <: AnyRef, AggregationValue <: Any](source: LazyView[Domain], aggregationFuncFactory: NotSelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue]) = {
    new AggregationForNotSelfMaintainableFunctions(source, (x: Any) => "a", aggregationFuncFactory, (x: Any, y: AggregationValue) => Some(y))
  }

  def apply[Domain <: AnyRef, AggregationValue <: Any](source: LazyView[Domain], aggregationFuncFactory: SelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue]) = {
    new AggregationForSelfMaintainableAggregationFunctions(source, (x: Any) => "a", aggregationFuncFactory, (x: Any, y: AggregationValue) => Some(y))
  }

}

