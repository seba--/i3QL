package idb.operators.impl


import collection.mutable
import idb.operators._
import idb.observer.{Observable, NotifyObservers, Observer}
import idb.{MaterializedView, Relation}
import scala.Some

/**
 * An implementation of Aggregation that saves for all groups the corresponding domain entries.
 * That allows not self maintainable aggregation to iterate over the domain entries
 *
 * Implementation details:
 * these implementation has a control flow like:
 * added called -> key lookup  ->(new key) create new map entry, create new aggregation function,
 * call aggregation function, collect aggregation newResult,  save newResult and notify observer
 * -> (else) call aggregation function, collect aggregation newResult -> may be notify observer
 *
 * a possible alternative would be:
 * added called -> key lookup -> (new key) create new map entry with a lazyview, create new aggregation function,
 * register aggregation function as an observer on the new lazyview,
 * register the whole aggregation as an observer of the aggregation function
 * -> (else) put the new value into the lazyview
 *
 * a further possible change could be to use an index view als source instead of a lazy view.  If aggregation use a
 * indexed view as source
 * it could use the grouping function as the index function.
 *
 * @author Malte V
 * @author Ralf Mitschke
 */
class AggregationForNotSelfMaintainableFunctions[Domain, Key, AggregateValue, Result] (val source: Relation[Domain],
    val groupingFunction: Domain => Key,
    val aggregateFunctionFactory: NotSelfMaintainableAggregateFunctionFactory[Domain, AggregateValue],
    val convertKeyAndAggregateValueToResult: (Key, AggregateValue) => Result,
    override val isSet: Boolean
)
    extends Aggregation[Domain, Key, AggregateValue, Result, NotSelfMaintainableAggregateFunction[Domain,
        AggregateValue], NotSelfMaintainableAggregateFunctionFactory[Domain, AggregateValue]]
    with Observer[Domain]
    with NotifyObservers[Result]
	with MaterializedView[Result]
{

    source.addObserver (this)


    import com.google.common.collect._

    val groups = mutable
        .Map[Key, (HashMultiset[Domain], NotSelfMaintainableAggregateFunction[Domain, AggregateValue], Result)]()

    // aggregation need to be isInitialized for update and remove events
    lazyInitialize ()

    override def endTransaction () {
        notify_endTransaction ()
    }

    override protected def childObservers (o: Observable[_]): Seq[Observer[_]] = {
        if (o == source) {
            return List (this)
        }
        Nil
    }


    /**
     *
     */
    def lazyInitialize () {
        source.foreach ((v: Domain) => {
            intern_added (v, notify = false)
        })

    }

    /**
     *
     */
    def foreach[T] (f: (Result) => T) {
        groups.foreach (x => f (x._2._3))
    }

    /**
     * Applies f to all elements of the view with their counts
     */
    def foreachWithCount[T] (f: (Result, Int) => T) {
        groups.groupBy (_._2._3).foreach (
            g => f (g._1, g._2.size)
        )
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
     * Returns the count for a given element.
     * In case an add/remove/update event is in progression, this always returns the value before the event.
     */
    def count[U >: Result] (element: U): Int = {
        groups.count (_._2._3 == element)
    }

    /**
     *
     * this implementation runs in O(n)
     */
    def contains[U >: Result] (element: U): Boolean = {
        groups.foreach (g => {
            if (g._2._3 == element)
                return true
        }
        )
        false
    }

    /**
     *
     */
    def updated (oldV: Domain, newV: Domain) {
        import scala.collection.JavaConversions.collectionAsScalaIterable
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
                notify_updated (oldResult, res)
        }
        else
        {
            removed (oldV)
            added (newV)
        }
    }

    /**
     *
     */
    def removed (v: Domain) {
        import scala.collection.JavaConversions.collectionAsScalaIterable
        val key = groupingFunction (v)
        if (!groups.contains (key)) {
            println ()
            System.err.println (v + " => " + key)
            return
        }
        val (data, aggregationFunction, oldResult) = groups (key)

        if (data.size == 1) {
            //remove a group
            groups -= key
            notify_removed (oldResult)
        }
        else
        {
            data.remove (v)
            val aggregationResult = aggregationFunction.remove (v, data)
            val res = convertKeyAndAggregateValueToResult (key, aggregationResult)
            if (res != oldResult) {
                //some aggragation valus changed => updated event
                groups.put (key, (data, aggregationFunction, res))
                notify_updated (oldResult, res)
            }
        }
    }

    /**
     *
     */
    def added (v: Domain) {
        intern_added (v, notify = true)
    }

    /**
     * internal added method for added
     * @param v : Domain
     * @param notify: true -> notify observers if a change occurs
     *              false -> dont notify any observer
     */
    private def intern_added (v: Domain, notify: Boolean) {
        import scala.collection.JavaConversions.collectionAsScalaIterable
        val key = groupingFunction (v)
        if (groups.contains (key)) {
            val (data, aggregationFunction, oldResult) = groups (key)
            data.add (v)

            val aggRes = aggregationFunction.add (v, data)
            val res = convertKeyAndAggregateValueToResult (key, aggRes)
            if (res != oldResult) {
                //some aggregation value changed => updated event
                groups.put (key, (data, aggregationFunction, res))
                if (notify) notify_updated (oldResult, res)
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
            if (notify) notify_added (res)
        }
    }
}

object AggregationForNotSelfMaintainableFunctions {

	def apply[Domain, Key, AggregateValue, Range](
		source : Relation[Domain],
		grouping : Domain => Key,
		start : AggregateValue,
		added : ((Domain, AggregateValue, Iterable[Domain])) => AggregateValue,
		removed : ((Domain, AggregateValue, Iterable[Domain])) => AggregateValue,
		updated : ( (Domain, Domain, AggregateValue, Iterable[Domain]) ) => AggregateValue,
		convert : ((Key,AggregateValue)) => Range,
		isSet : Boolean
	): Relation[Range] = {
		val factory : NotSelfMaintainableAggregateFunctionFactory[Domain,AggregateValue] =
			new NotSelfMaintainableAggregateFunctionFactory[Domain,AggregateValue]
			{
				override def apply() : NotSelfMaintainableAggregateFunction[Domain,AggregateValue] = {
					new NotSelfMaintainableAggregateFunction[Domain,AggregateValue] {
						private var aggregate : AggregateValue = start

						def add(newD: Domain, data : Iterable[Domain]): AggregateValue = {
							val a = added( (newD, aggregate, data) )
							aggregate = a
							a
						}

						def remove(newD: Domain, data : Iterable[Domain]): AggregateValue = {
							val a = removed( (newD, aggregate, data) )
							aggregate = a
							a
						}

						def update(oldD: Domain, newD: Domain, data : Iterable[Domain]): AggregateValue = {
							val a = updated( (oldD, newD, aggregate, data) )
							aggregate = a
							a
						}

						def get : AggregateValue =
							aggregate
					}
				}
			}

		return new AggregationForNotSelfMaintainableFunctions[Domain,Key,AggregateValue,Range](
			source,
			grouping,
			factory,
			(x,y) => convert((x,y)),
			isSet
		)
	}

	def apply[Domain, Key, Range] (
		source: Relation[Domain],
		grouping: Domain => Key,
		start : Range,
		added : ((Domain, Range, Iterable[Domain])) => Range,
		removed : ((Domain, Range, Iterable[Domain])) => Range,
		updated : ( (Domain, Domain, Range, Iterable[Domain]) ) => Range,
		isSet : Boolean
	): Relation[Range] = {
		apply (
			source,
			grouping,
			start,
			added,
			removed,
			updated,
			Function.tupled((x : Key, y : Range) => y),
			isSet
		)
	}

	def applyTupled[Domain, Key, RangeA, RangeB] (
		source: Relation[Domain],
		grouping: Domain => Key,
		start : RangeB,
		added : ((Domain, RangeB, Iterable[Domain])) => RangeB,
		removed : ((Domain, RangeB, Iterable[Domain])) => RangeB,
		updated : ( (Domain, Domain, RangeB, Iterable[Domain]) ) => RangeB,
		convertKey : Key => RangeA,
		isSet : Boolean
	): Relation[(RangeA, RangeB)] = {
		apply[Domain, Key, RangeB, (RangeA, RangeB)] (
			source,
			grouping,
			start,
			added,
			removed,
			updated,
			Function.tupled((x : Key, y : RangeB) => (convertKey (x), y)),
			isSet
		)
	}



	def apply[Domain, Range](
		source : Relation[Domain],
		start : Range,
		added : ((Domain, Range, Iterable[Domain])) => Range,
		removed : ((Domain, Range, Iterable[Domain])) => Range,
		updated : ( (Domain, Domain, Range, Iterable[Domain]) ) => Range,
		isSet : Boolean
	): Relation[Range] = {
		apply(source,
			(x : Domain) => true,
			start,
			added,
			removed,
			updated,
			Function.tupled((x : Boolean, y : Range) => y),
			isSet)
	}
}