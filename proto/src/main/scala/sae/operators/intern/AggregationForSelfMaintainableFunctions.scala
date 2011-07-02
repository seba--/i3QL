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
class AggregationForSelfMaintainableAggregationFunctions[Domain <: AnyRef, Key <: Any, AggregationValue <: Any, Result <: AnyRef](val source: LazyView[Domain], val groupingFunction: Domain => Key, val aggregationFunctionFactory: SelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue],
                                                                                                                                  val convertKeyAndAggregationValueToResult: (Key, AggregationValue) => Result)
  extends Aggregation[Domain, Key, AggregationValue, Result, SelfMaintainalbeAggregationFunction[Domain, AggregationValue], SelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue]] with Observer[Domain] with MaterializedView[Result] {


  val groups = Map[Key, (Count, SelfMaintainalbeAggregationFunction[Domain, AggregationValue], Result)]()
  lazyInitialize // is needed
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
    var contained = false
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
      val newResult = convertKeyAndAggregationValueToResult(oldKey, aggregationResult)
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
    val (count, aggFuncs, oldResult) = groups(key)

    if (count.dec == 0) {
      //remove a group
      groups -= key
      element_removed(oldResult)
    } else {
      //remove element from key group
      val aggregationFunction = aggFuncs.remove(v)
      val res = convertKeyAndAggregationValueToResult(key, aggregationFunction)
      if (res != oldResult) {
        //some aggragation valus changed => updated event
        groups.put(key, (count, aggFuncs, res))
        element_updated(oldResult, res)
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
      val res = convertKeyAndAggregationValueToResult(key, aggregationValue)
      if (res != oldResult) {
        //some aggregation values changed => updated event
        groups.put(key, (count, aggregationFunction, res))
        if (notify) element_updated(oldResult, res)
      }
    } else {
      //new key group
      val c = new Count
      c.inc
      val aggregationFunction = aggregationFunctionFactory()
      val aggRes = aggregationFunction.add(v)
      val res = convertKeyAndAggregationValueToResult(key, aggRes)
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