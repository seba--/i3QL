package idb.operators.impl


import collection.mutable
import idb.{MaterializedView, Relation}
import idb.operators.{AggregateFunction, SelfMaintainableAggregateFunctionFactory, Aggregation, SelfMaintainableAggregateFunction}
import idb.observer.{Observable, NotifyObservers, Observer}

/**
 * An implementation of Aggregation that only saves the newResult of aggregation function (aggregationFunction)
 *
 * Implementation detail:
 * these implementation has a control flow like:
 * method added called -> key lookup  ->(new key) create new map entry, create new aggregation function, call aggregation function, collect aggregation newResult,  save newResult and notify observer
 * -> (else) call aggregation function, collect aggregation newResult -> may be notify observer
 *
 * a possible alternative would be:
 * method added called -> key lookup -> (new key) create new map entry with a lazyview, create new aggregation function,
 * register aggregation function as an observer on the new lazyview,
 * register the whole aggregation as an observer of the aggregation function
 * -> (else) put the new value into the lazyview
 *
 * @author Malte V
 * @author Ralf Mitschke
 */
class AggregationForSelfMaintainableFunctions[Domain, Key, AggregateValue, Result](val source: Relation[Domain],
                                                                                   val groupingFunction: Domain => Key,
                                                                                   val aggregateFunctionFactory: SelfMaintainableAggregateFunctionFactory[Domain, AggregateValue],
                                                                                   val convertKeyAndAggregateValueToResult: (Key, AggregateValue) => Result,
																				   override val isSet : Boolean)
    extends Aggregation[Domain, Key, AggregateValue, Result, SelfMaintainableAggregateFunction[Domain, AggregateValue], SelfMaintainableAggregateFunctionFactory[Domain, AggregateValue]]
    with Observer[Domain]
	with NotifyObservers[Result]
	with MaterializedView[Result]
{


    source.addObserver (this)


    val groups = mutable.Map[Key, (Count, SelfMaintainableAggregateFunction[Domain, AggregateValue], Result)]()

    // aggregation need to be isInitialized for update and remove events
     lazyInitialize()

    override def endTransaction() {
        notify_endTransaction()
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
            internal_added (v, notify = false)
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

    /**
     * Returns the count for a given element.
     * In case an add/remove/update event is in progression, this always returns the
     */
    def count[T >: Result](v: T) = {
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
     */
     def contains[U >: Result](element: U) : Boolean = {
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
    def updated(oldV: Domain, newV: Domain) {
        val oldKey = groupingFunction (oldV)
        val newKey = groupingFunction (newV)
        if (oldKey == newKey) {
            val (count, aggregationFunction, oldResult) = groups (oldKey)
            val aggregationResult = aggregationFunction.update (oldV, newV)
            val newResult = convertKeyAndAggregateValueToResult (oldKey, aggregationResult)
            groups.put (oldKey, (count, aggregationFunction, newResult))
            if (oldResult != newResult)
                notify_updated (oldResult, newResult)
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
    def removed(v: Domain) {
        val key = groupingFunction (v)
        val (count, aggregationFunction, oldResult) = groups (key)

        if (count.dec == 0) {
            //remove a group
            groups -= key
            notify_removed (oldResult)
        }
        else
        {
            //remove element from key group
            val aggregationResult = aggregationFunction.remove (v)
            val newResult = convertKeyAndAggregateValueToResult (key, aggregationResult)
            if (newResult != oldResult) {
                //some aggregation values changed => updated event
                groups.put (key, (count, aggregationFunction, newResult))
				notify_updated (oldResult, newResult)
            }
        }
    }

    /**
     *
     */
    def added(v: Domain) {
        internal_added (v, notify = true)
    }

    private def internal_added(v: Domain, notify: Boolean) {
        val key = groupingFunction (v)
        if (groups.contains (key)) {
            //update key group
            val (count, aggregationFunction, oldResult) = groups (key)
            count.inc ()
            val aggregationValue = aggregationFunction.add (v)
            val res = convertKeyAndAggregateValueToResult (key, aggregationValue)
            if (res != oldResult) {
                //some aggregation values changed => updated event
                groups.put (key, (count, aggregationFunction, res))
                if (notify) notify_updated (oldResult, res)
            }
        }
        else
        {
            //new key group
            val c = new Count
            c.inc ()
            val aggregationFunction = aggregateFunctionFactory ()
            val aggRes = aggregationFunction.add (v)
            val res = convertKeyAndAggregateValueToResult (key, aggRes)
            groups.put (key, (c, aggregationFunction, res))
            if (notify) notify_added (res)
        }
    }

}

object AggregationForSelfMaintainableFunctions {

	def apply[Domain, Key, AggregateValue, Result](
		source : Relation[Domain],
		grouping : Domain => Key,
		added : Domain => AggregateValue,
		removed : Domain => AggregateValue,
		updated : ((Domain, Domain)) => AggregateValue,
		convert : ((Key,AggregateValue)) => Result,
		isSet : Boolean
	): Relation[Result] = {
		val factory : SelfMaintainableAggregateFunctionFactory[Domain,AggregateValue] = new SelfMaintainableAggregateFunctionFactory[Domain,AggregateValue] {
			 override def apply() : SelfMaintainableAggregateFunction[Domain,AggregateValue] = {
				 new SelfMaintainableAggregateFunction[Domain,AggregateValue] {
					 private var aggregate : Option[AggregateValue] = None

					 def add(newD: Domain): AggregateValue = {
						 val a = added(newD)
						 aggregate = Some(a)
						 a
					 }


					 def remove(newD: Domain): AggregateValue = {
						 val a = removed(newD)
						 aggregate = Some(a)
						 a
					 }


					 def update(oldD: Domain, newD: Domain): AggregateValue = {
						 val a = updated( (oldD, newD) )
						 aggregate = Some(a)
						 a
					 }

					 def get : AggregateValue = {
						 aggregate match {
							 case Some(a) => a
							 //TODO Make this better.
							 case None => throw new IllegalArgumentException("Aggregation value is not initialized.")
						 }
					 }
				 }
			 }
		}

		return new AggregationForSelfMaintainableFunctions[Domain,Key,AggregateValue,Result](source,grouping,factory,(x,y) => convert((x,y)),isSet)

	}

	def apply[Domain, Result](
		source: Relation[Domain],
		added: Domain => Result,
		removed: Domain => Result,
		updated: ((Domain, Domain)) => Result,
		isSet: Boolean
	): Relation[Result] = {
		apply(source,
			(x : Domain) => true,
			added,
			removed,
			updated,
			Function.tupled((x : Boolean, y : Result) => y),
			isSet
		)
	}

	def apply[Domain, Key, Result](
		source: Relation[Domain],
		grouping: Domain => Key,
		convert: Key => Result,
		isSet: Boolean
  	): Relation[Result] = {
		apply(source,
			grouping,
			(x :Domain) => true,
			(x :Domain) => true,
			Function.tupled((x : Domain, y : Domain) => true),
			Function.tupled((x : Key, y : Boolean) => convert(x)),
			isSet
		)
	}
}



