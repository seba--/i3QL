package idb.operators.impl


import scala.collection.mutable
import idb.{MaterializedView, Relation}
import idb.operators._
import idb.observer.{NotifyObservers, Observable, Observer}

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
	*
 *
 * @author Malte V
 * @author Ralf Mitschke
 */
case class AggregationForSelfMaintainableFunctions[Domain, Key, AggregateValue, Result](
	relation: Relation[Domain],
	groupingFunction: Domain => Key,
	aggregateFunctionFactory: SelfMaintainableAggregateFunctionFactory[Domain, AggregateValue],
	convertToResult: (Key, AggregateValue, Domain) => Result,
	isSet : Boolean
) extends Aggregation[Domain, Key, AggregateValue, Result, SelfMaintainableAggregateFunction[Domain, AggregateValue], SelfMaintainableAggregateFunctionFactory[Domain, AggregateValue]]
    with Observer[Domain]
	with NotifyObservers[Result]
	with MaterializedView[Result] {

    relation.addObserver (this)

    val groups = mutable.HashMap.empty[Key, (Count, SelfMaintainableAggregateFunction[Domain, AggregateValue], Result)]


	override def resetInternal(): Unit = {
		groups.clear()
	}

    override protected def childObservers(o: Observable[_]): Seq[Observer[_]] = {
        if (o == relation) {
            return List (this)
        }
        Nil
    }

     /**
     *
     */
     override def foreach[T](f: (Result) => T) {
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

     def size: Int = groups.size

     def singletonValue: Option[Result] = {
        if (size != 1)
            None
        else
            Some (groups.head._2._3)
    }

	def contains[U >: Result](element: U) : Boolean = {
		groups.foreach(g => {
			if (g._2._3 == element)
				return true
		})
		false
	}


     def updated(oldV: Domain, newV: Domain) {
        val oldKey = groupingFunction (oldV)
        val newKey = groupingFunction (newV)
        if (oldKey == newKey) {
			try {
				val (count, aggregationFunction, oldResult) = groups (oldKey)
				val aggregationResult = aggregationFunction.update (oldV, newV)
				val newResult = convertToResult (oldKey, aggregationResult, newV)
				groups.put (oldKey, (count, aggregationFunction, newResult))
				if (oldResult != newResult)
					notify_updated (oldResult, newResult)
			} catch {
				case ex : NoSuchElementException => throw new IllegalStateException("No element to be updated.")
			}
        }
        else
        {
            removed (oldV)
            added (newV)
        }
    }


	def removed (v: Domain) {
		intern_removed (v) match {
			case None =>
			case Some((key, Left(removed))) => notify_removed(removed)
			case Some((key, Right((old, res)))) => notify_updated(old, res)
		}
	}

	def removedAll (vs: Seq[Domain]) {

		var removed = Map[Key,Result]()
		var updated = Map[Key, (Result,Result)]()

		vs foreach { v => intern_removed(v) match {
			case None =>
			case Some((key, Left(r))) => removed += key -> r
			case Some((key, Right((old, res)))) =>
				removed.get(key) match {
					case Some(`old`) => removed += key -> res
					case Some(old2) => throw new RuntimeException(s"unexpected old value $old2, expected $old")
					case None =>
						updated.get(key) match {
							case Some((oldold,`old`)) => updated += key -> (oldold, res)
							case Some((oldold, old2)) => throw new RuntimeException(s"unexpected old value $old2, expected $old")
							case None => updated += key -> (old, res)
						}
				}
		}}

		notify_removedAll(removed.values.toSeq)
		updated.values foreach (v => notify_updated(v._1, v._2))
	}

	def intern_removed (v: Domain): Option[(Key, Either[Result, (Result,Result)])] = {
		val key = groupingFunction (v)
		try {
			val (count, aggregationFunction, oldResult) = groups (key)

			if (count.dec == 0) {
				//remove a group
				groups -= key
				Some((key, Left(oldResult)))
			}
			else
			{
				//remove element from key group
				val aggregationResult = aggregationFunction.remove (v)
				val newResult = convertToResult (key, aggregationResult, v)
				if (newResult != oldResult) {
					//some aggregation values changed => updated event
					groups.put (key, (count, aggregationFunction, newResult))
					Some((key, Right((oldResult, newResult))))
				}
				else
					None
			}

		} catch {
			case ex : NoSuchElementException => throw new IllegalStateException("No element to be updated.")
		}

	}

	def added (v: Domain) {
		internal_added (v) match {
			case None =>
			case Some((key, Left(added))) => notify_added(added)
			case Some((key, Right((old, res)))) => notify_updated(old, res)
		}
	}

	def addedAll (vs: Seq[Domain]) {

		var added = Map[Key,Result]()
		var updated = Map[Key, (Result,Result)]()

		vs foreach { v => internal_added(v) match {
			case None =>
			case Some((key, Left(r))) => added += key -> r
			case Some((key, Right((old, res)))) =>
				added.get(key) match {
					case Some(`old`) => added += key -> res
					case Some(old2) => throw new RuntimeException(s"unexpected old value $old2, expected $old")
					case None =>
						updated.get(key) match {
							case Some((oldold,`old`)) => updated += key -> (oldold, res)
							case Some((oldold, old2)) => throw new RuntimeException(s"unexpected old value $old2, expected $old")
							case None => updated += key -> (old, res)
						}
				}
		}}

		notify_addedAll(added.values.toSeq)
		updated.values foreach (v => notify_updated(v._1, v._2))
	}


	private def internal_added(v: Domain): Option[(Key, Either[Result, (Result,Result)])] = {
		val key = groupingFunction (v)
		if (groups.contains (key)) {
			//update key group
			val (count, aggregationFunction, oldResult) = groups (key)
			count.inc ()
			val aggregationValue = aggregationFunction.add (v)
			val res = convertToResult (key, aggregationValue, v)
			if (res != oldResult) {
				//some aggregation values changed => updated event
				groups.put (key, (count, aggregationFunction, res))
				Some((key, Right((oldResult, res))))
			} else {
				None
			}
		} else {
			//new key group
			val c = new Count
			c.inc ()
			val aggregationFunction = aggregateFunctionFactory ()
			val aggRes = aggregationFunction.add (v)
			val res = convertToResult (key, aggRes, v)
			groups.put (key, (c, aggregationFunction, res))
			Some((key, Left(res)))
		}
	}

}

class Count
{
	private var count: Int = 0

	def inc() : Int = {
		this.count += 1
		this.count
	}

	def dec() : Int = {
		this.count -= 1
		this.count
	}

	def apply() = this.count
}



