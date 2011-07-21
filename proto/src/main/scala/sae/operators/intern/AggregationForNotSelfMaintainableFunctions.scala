package sae.operators.intern

import sae._

import sae.operators._
import scala.collection.JavaConversions._
import scala.collection.mutable.Map

/**
 * An implementation of Aggregation that saves for all groups the corresponding domain entries.
 * That allows not self maintainable aggregation to iterate over the domain entries
 *
 * Implementation details:
 * these implementation has a control flow like:
 *  added called -> key lookup  ->(new key) create new map entry, create new aggregation function, call aggregation function, collect aggregation result,  save result and notify observer
 *                              -> (else) call aggregation function, collect aggregation result -> may be notify observer
 *
 * a possible alternative would be:
 *  added called -> key lookup -> (new key) create new map entry with a lazyview, create new aggregation function,
 *                                register aggregation function as an observer on the new lazyview,
 *                                register the whole aggregation as an observer of the aggregation function
 *                              -> (else) put the new value into the lazyview
 *
 *  a further possible change could be to use an index view als source instead of a lazy view.  If aggregation use a indexed view as source
 *  it could use the grouping function as the index function.
 *
 * @author Malte V
 */


class AggregationForNotSelfMaintainableFunctions[Domain <: AnyRef, Key <: Any, AggregateValue <: Any, Result <: AnyRef]
          (val source: LazyView[Domain],
           val groupingFunction: Domain => Key,
           val aggregateFunctionFactory: NotSelfMaintainalbeAggregateFunctionFactory[Domain, AggregateValue],
          val convertKeyAndAggregateValueToResult: (Key, AggregateValue) => Result)
  extends Aggregation[Domain, Key, AggregateValue, Result, NotSelfMaintainalbeAggregateFunction[Domain, AggregateValue], NotSelfMaintainalbeAggregateFunctionFactory[Domain, AggregateValue]]
  with Observer[Domain] with MaterializedView[Result] {


  import com.google.common.collect._;

  val groups = Map[Key, (HashMultiset[Domain], NotSelfMaintainalbeAggregateFunction[Domain, AggregateValue], Result)]()
  lazyInitialize // aggregation need to be initialized for update and remove events
  source.addObserver(this)

   /**
   * {@inheritDoc}
   */
  def lazyInitialize: Unit = {
    if (!initialized) {
      source.lazy_foreach((v: Domain) => {
        intern_added(v, false)
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
   * these implementation runs in O(n)
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
      val (data, aggregationFunction, oldResult) = groups(oldKey)
      data.remove(oldV)
      data.add(newV)
      val aggregationResult = aggregationFunction.update(oldV, newV, data)
      val res = convertKeyAndAggregateValueToResult(oldKey, aggregationResult)
      groups.put(oldKey, (data, aggregationFunction, res))
      if (oldResult != res)
        element_updated(oldResult, res)
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
    val (data, aggregationFunction, oldResult) = groups(key)

    if (data.size == 1) {
      //remove a group
      groups -= key
      element_removed(oldResult)
    } else {
      data.remove(v)
      val aggregationResult = aggregationFunction.remove(v, data)
      val res = convertKeyAndAggregateValueToResult(key, aggregationResult)
      if (res != oldResult) {
        //some aggragation valus changed => updated event
        groups.put(key, (data, aggregationFunction, res))
        element_updated(oldResult, res)
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  def added(v: Domain) {
    intern_added(v, true)
  }

  /**
   * internal added method for added
   * @param v : Domain
   * @param notify: true -> notify observers if a change occurs
   *                false -> dont notify any observer
   */
  private def intern_added(v: Domain, notify: Boolean) {
    val key = groupingFunction(v)
    if (groups.contains(key)) {
      val (data, aggregationFunction, oldResult) = groups(key)
      data.add(v)

      val aggRes = aggregationFunction.add(v, data)
      val res = convertKeyAndAggregateValueToResult(key, aggRes)
      if (res != oldResult) {
        //some aggregation value changed => updated event
        groups.put(key, (data, aggregationFunction, res))
        if (notify) element_updated(oldResult, res)
      }
    } else {

      val data = HashMultiset.create[Domain]()
      data.add(v)
      val aggregationFunction = aggregateFunctionFactory()
      val aggregationResult = aggregationFunction.add(v, data)
      val res = convertKeyAndAggregateValueToResult(key, aggregationResult)
      groups.put(key, (data, aggregationFunction, res))
      if (notify) element_added(res)
    }
  }
}