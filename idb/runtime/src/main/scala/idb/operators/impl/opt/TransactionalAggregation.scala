package idb.operators.impl.opt

import collection.mutable
import idb.Relation
import idb.operators._
import idb.observer.{Observable, NotifyObservers, Observer}
import com.google.common.collect.HashMultimap
import scala.collection.JavaConverters._

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
class TransactionalAggregation[Domain, Key, AggregateValue, Result](val source: Relation[Domain],
                                                                    val groupingFunction: Domain => Key,
                                                                    val aggregateFunctionFactory: AggregateFunctionFactory[Domain, AggregateValue, AggregateFunction[Domain, AggregateValue]],
                                                                    val convertKeyAndAggregateValueToResult: (Key, AggregateValue) => Result,
																	override val isSet : Boolean)
    extends Aggregation[Domain, Key, AggregateValue, Result, AggregateFunction[Domain, AggregateValue], AggregateFunctionFactory[Domain, AggregateValue, AggregateFunction[Domain, AggregateValue]]]
    with Observer[Domain]
	with NotifyObservers[Result]
{

	type Aggregate = AggregateFunction[Domain, AggregateValue]

    source.addObserver (this)

    var additionsMap : HashMultimap[Key,Domain] = HashMultimap.create[Key,Domain]()
	var deletionsMap : HashMultimap[Key,Domain] = HashMultimap.create[Key,Domain]()

	var functionMap : mutable.HashMap[Key,Aggregate] = mutable.HashMap.empty[Key,Aggregate]


	private def getFunctionForKey(key : Key) : Aggregate = {
		functionMap.get(key) match {
			case Some(f) => f
			case None => {
				val f = aggregateFunctionFactory.apply()
				functionMap.put(key,f)
				f
			}
		}
	}

	//TODO implement deletion
    override def endTransaction() {

		val keyIt = additionsMap.keys().iterator()
		while (keyIt.hasNext) {
			val key : Key = keyIt.next()

			val aggregateFunction : Aggregate = getFunctionForKey(key)

			val setAsScala = additionsMap.get(key).asScala

			for (dom <- setAsScala) {
				aggregateFunction.add(dom,setAsScala)
			}

			notify_added(convertKeyAndAggregateValueToResult(key,aggregateFunction.get))
		}




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
      /*  source.foreach ((v: Domain) => {
            internal_added (v, notify = false)
        })   */
    }

    /**
     *
     */
     def foreach[T](f: (Result) => T) {

    }


    /**
     *
     */
    def updated(oldV: Domain, newV: Domain) {
		removed(oldV)
		added(newV)
    }

    /**
     *
     */
    def removed(v: Domain) {
		deletionsMap.put(groupingFunction(v),v)
    }

    def added(v: Domain) {
		additionsMap.put(groupingFunction(v),v)
    }

	/*
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
    }        */

}





