package sae.operators.intern

import sae._

import sae.operators._
import scala.collection.mutable.Map

/**
 * An implementation of Aggregation that only saves the result of aggregation function (aggregationFunction)
 *
 * Implementation detail:
 * these implementation has a control flow like:
 *  method added called -> key lookup  ->(new key) create new map entry, create new aggregation function, call aggregation function, collect aggregation result,  save result and notify observer
 *                              -> (else) call aggregation function, collect aggregation result -> may be notify observer
 *
 * a possible alternative would be:
 *  method added called -> key lookup -> (new key) create new map entry with a lazyview, create new aggregation function,
 *                                register aggregation function as an observer on the new lazyview,
 *                                register the whole aggregation as an observer of the aggregation function
 *                              -> (else) put the new value into the lazyview
 *
 * @author Malte V
 */
class AggregationForSelfMaintainableAggregationFunctions[Domain <: AnyRef, Key <: Any, AggregateValue <: Any, Result <: AnyRef](val source: LazyView[Domain], val groupingFunction: Domain => Key, val aggregateFunctionFactory: SelfMaintainalbeAggregateFunctionFactory[Domain, AggregateValue],
                                                                                                                                  val convertKeyAndAggregateValueToResult: (Key, AggregateValue) => Result)
  extends Aggregation[Domain, Key, AggregateValue, Result, SelfMaintainalbeAggregateFunction[Domain, AggregateValue], SelfMaintainalbeAggregateFunctionFactory[Domain, AggregateValue]] with Observer[Domain] with MaterializedView[Result] {


  val groups = Map[Key, (Count, SelfMaintainalbeAggregateFunction[Domain, AggregateValue], Result)]()
  lazyInitialize // aggregation need to be initialized for update and remove events
  source.addObserver(this)

  /**
   * {@inheritDoc}
   */
  def lazyInitialize: Unit = {
    if (!initialized) {
      source.lazy_foreach((v: Domain) => {
        internal_added(v, false)
      })
      initialized = true
    }

  }

  /**
   * {@inheritDoc}
   */
  protected def materialized_foreach[T](f: (Result) => T): Unit = {
    groups.foreach(x => f(x._2._3))
  }

  /**
   * {@inheritDoc}
   */
  protected def materialized_size: Int = groups.size

  /**
   * {@inheritDoc}
   */
  protected def materialized_singletonValue: Option[Result] = {
    if (size != 1)
      None
    else
      Some(groups.head._2._3)
  }

  /**
   * {@inheritDoc}
   */
  protected def materialized_contains(v: Result) = {
    groups.foreach(g => {
      if (g._2._3 == v)
        true
    }
    )
    false
  }


  /**
   * {@inheritDoc}
   */
  def updated(oldV: Domain, newV: Domain) {
    val oldKey = groupingFunction(oldV)
    val newKey = groupingFunction(newV)
    if (oldKey == newKey) {
      val (count, aggregationFunction, oldResult) = groups(oldKey)
      val aggregationResult = aggregationFunction.update(oldV, newV)
      val newResult = convertKeyAndAggregateValueToResult(oldKey, aggregationResult)
      groups.put(oldKey, (count, aggregationFunction, newResult))
      if (oldResult != newResult)
        element_updated(oldResult, newResult)
    } else {
      removed(oldV);
      added(newV);
    }
  }

  /**
   * {@inheritDoc}
   */
  def removed(v: Domain) {
    val key = groupingFunction(v)
    val (count, aggregationFunction, oldResult) = groups(key)

    if (count.dec == 0) {
      //remove a group
      groups -= key
      element_removed(oldResult)
    } else {
      //remove element from key group
      val aggregationResult = aggregationFunction.remove(v)
      val newResult = convertKeyAndAggregateValueToResult(key, aggregationResult)
      if (newResult != oldResult) {
        //some aggregation values changed => updated event
        groups.put(key, (count, aggregationFunction, newResult))
        element_updated(oldResult, newResult)
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  def added(v: Domain) {
    internal_added(v, true)
  }

  private def internal_added(v: Domain, notify: Boolean) {
    val key = groupingFunction(v)
    if (groups.contains(key)) {
      //update key group
      val (count, aggregationFunction, oldResult) = groups(key)
      count.inc
      val aggregationValue = aggregationFunction.add(v)
      val res = convertKeyAndAggregateValueToResult(key, aggregationValue)
      if (res != oldResult) {
        //some aggregation values changed => updated event
        groups.put(key, (count, aggregationFunction, res))
        if (notify) element_updated(oldResult, res)
      }
    } else {
      //new key group
      val c = new Count
      c.inc
      val aggregationFunction = aggregateFunctionFactory()
      val aggRes = aggregationFunction.add(v)
      val res = convertKeyAndAggregateValueToResult(key, aggRes)
      groups.put(key, (c, aggregationFunction, res))
      if (notify) element_added(res)
    }
  }
}

protected class Count {
  private var count: Int = 0

  def inc() = {
    this.count += 1
  }

  def dec(): Int = {
    this.count -= 1;
    this.count
  }

  def apply() = this.count
}