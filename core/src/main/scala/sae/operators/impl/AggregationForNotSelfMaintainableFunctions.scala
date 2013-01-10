package sae.operators.impl

import sae._

import deltas.{Deletion, Addition, Update}
import sae.operators._
import scala.collection.JavaConversions._
import collection.mutable

/**
 * An implementation of Aggregation that saves for all groups the corresponding domain entries.
 * That allows not self maintainable aggregation to iterate over the domain entries
 *
 * Implementation details:
 * these implementation has a control flow like:
 * added called -> key lookup  ->(new key) create new map entry, create new aggregation function, call aggregation function, collect aggregation result,  save result and notify observer
 * -> (else) call aggregation function, collect aggregation result -> may be notify observer
 *
 * a possible alternative would be:
 * added called -> key lookup -> (new key) create new map entry with a lazyview, create new aggregation function,
 * register aggregation function as an observer on the new lazyview,
 * register the whole aggregation as an observer of the aggregation function
 * -> (else) put the new value into the lazyview
 *
 * a further possible change could be to use an index view als source instead of a lazy view.  If aggregation use a indexed view as source
 * it could use the grouping function as the index function.
 *
 * @author Malte V
 * @author Ralf Mitschke
 */
class AggregationForNotSelfMaintainableFunctions[Domain, Key, AggregateValue, Result](val source: Relation[Domain],
                                                                                      val groupingFunction: Domain => Key,
                                                                                      val aggregateFunctionFactory: NotSelfMaintainableAggregateFunctionFactory[Domain, AggregateValue],
                                                                                      val convertKeyAndAggregateValueToResult: (Key, AggregateValue) => Result)
    extends Aggregation[Domain, Key, AggregateValue, Result, NotSelfMaintainableAggregateFunction[Domain, AggregateValue], NotSelfMaintainableAggregateFunctionFactory[Domain, AggregateValue]]
    with Observer[Domain]
{

    source.addObserver (this)

    def isSet = false

    import com.google.common.collect._

    val groups = mutable.Map[Key, (HashMultiset[Domain], NotSelfMaintainableAggregateFunction[Domain, AggregateValue], Result)]()

    // aggregation need to be isInitialized for update and remove events
    lazyInitialize ()

    override def endTransaction() {
        notifyEndTransaction()
    }

    override protected def childObservers(o: Observable[_]): Seq[Observer[_]] = {
        if (o == source) {
            return List (this)
        }
        Nil
    }


    /**
     *
     */
    def lazyInitialize() {
        source.foreach ((v: Domain) => {
            intern_added (v, notify = false)
        })

    }

    /**
     *
     */
    def foreach[T](f: (Result) => T) {
        groups.foreach (x => f (x._2._3))
    }

    /**
     * Applies f to all elements of the view with their counts
     */
    def foreachWithCount[T](f: (Result, Int) => T) {
        groups.groupBy( _._2._3).foreach(
            g => f(g._1, g._2.size)
        )
    }

    def isDefinedAt(v: Result) = {
        groups.exists( _._2._3 == v)
    }

    /**
     * Returns the count for a given element.
     * In case an add/remove/update event is in progression, this always returns the
     */
    def elementCountAt[T >: Result](v: T) = {
        groups.count( _._2._3 == v)
    }


    /**
     *
     */
    def size: Int = groups.size

    /**
     *
     */
    def singletonValue: Option[Result] = {
        if (size != 1)
            None
        else
            Some (groups.head._2._3)
    }


    /**
     *
     * this implementation runs in O(n)
     */
    def contains(v: Result) = {
        groups.foreach (g => {
            if (g._2._3 == v)
                true
        }
        )
        false
    }

    /**
     *
     */
    def updated(oldV: Domain, newV: Domain) {
        val oldKey = groupingFunction (oldV)
        val newKey = groupingFunction (newV)
        if (oldKey == newKey) {
            val (data, aggregationFunction, oldResult) = groups (oldKey)
            data.remove (oldV)
            data.add (newV)
            val aggregationResult = aggregationFunction.update (oldV, newV, data)
            val res = convertKeyAndAggregateValueToResult (oldKey, aggregationResult)
            groups.put (oldKey, (data, aggregationFunction, res))
            if (oldResult != res)
                element_updated (oldResult, res)
        }
        else
        {
            removed (oldV);
            added (newV);
        }
    }

    /**
     *
     */
    def removed(v: Domain) {
        val key = groupingFunction (v)
        if (!groups.contains (key)) {
            println (v + " => " + key)
        }
        val (data, aggregationFunction, oldResult) = groups (key)

        if (data.size == 1) {
            //remove a group
            groups -= key
            element_removed (oldResult)
        }
        else
        {
            data.remove (v)
            val aggregationResult = aggregationFunction.remove (v, data)
            val res = convertKeyAndAggregateValueToResult (key, aggregationResult)
            if (res != oldResult) {
                //some aggragation valus changed => updated event
                groups.put (key, (data, aggregationFunction, res))
                element_updated (oldResult, res)
            }
        }
    }

    /**
     *
     */
    def added(v: Domain) {
        intern_added (v, notify = true)
    }

    /**
     * internal added method for added
     * @param v : Domain
     * @param notify: true -> notify observers if a change occurs
     *              false -> dont notify any observer
     */
    private def intern_added(v: Domain, notify: Boolean) {
        val key = groupingFunction (v)
        if (groups.contains (key)) {
            val (data, aggregationFunction, oldResult) = groups (key)
            data.add (v)

            val aggRes = aggregationFunction.add (v, data)
            val res = convertKeyAndAggregateValueToResult (key, aggRes)
            if (res != oldResult) {
                //some aggregation value changed => updated event
                groups.put (key, (data, aggregationFunction, res))
                if (notify) element_updated (oldResult, res)
            }
        }
        else
        {

            val data = HashMultiset.create[Domain]()
            data.add (v)
            val aggregationFunction = aggregateFunctionFactory ()
            val aggregationResult = aggregationFunction.add (v, data)
            val res = convertKeyAndAggregateValueToResult (key, aggregationResult)
            groups.put (key, (data, aggregationFunction, res))
            if (notify) element_added (res)
        }
    }

    def updated[U <: Domain](update: Update[U]) {
        throw new UnsupportedOperationException
    }

    def modified[U <: Domain](additions: Set[Addition[U]], deletions: Set[Deletion[U]], updates: Set[Update[U]]) {
        throw new UnsupportedOperationException
    }
}