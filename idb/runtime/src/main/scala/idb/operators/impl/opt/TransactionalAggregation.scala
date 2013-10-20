package idb.operators.impl.opt

import collection.mutable
import idb.Relation
import idb.operators._
import idb.observer.{Observable, NotifyObservers, Observer}
import com.google.common.collect.HashMultimap
import scala.collection.JavaConverters._

/**
 * Transactional aggregation operator.
 *
 * @author Mirko Köhler
 */
class TransactionalAggregation[Domain, Key, AggregateValue, Result](val source: Relation[Domain],
																	val groupingFunction: Domain => Key,
																	val aggregateFunctionFactory: AggregateFunctionFactory[Domain, AggregateValue, AggregateFunction[Domain, AggregateValue]],
																	val convertKeyAndAggregateValueToResult: (Key, AggregateValue) => Result,
																	override val isSet: Boolean)
	extends Aggregation[Domain, Key, AggregateValue, Result, AggregateFunction[Domain, AggregateValue], AggregateFunctionFactory[Domain, AggregateValue, AggregateFunction[Domain, AggregateValue]]]
	with Observer[Domain]
	with NotifyObservers[Result] {

	type Aggregate = AggregateFunction[Domain, AggregateValue]

	source.addObserver(this)

	var additionsMap: HashMultimap[Key, Domain] = HashMultimap.create[Key, Domain]()
	var deletionsMap: HashMultimap[Key, Domain] = HashMultimap.create[Key, Domain]()

	var functionMap: mutable.HashMap[Key, Aggregate] = mutable.HashMap.empty[Key, Aggregate]


	private def getFunctionForKey(key: Key): (Aggregate, Boolean) = {
		functionMap.get(key) match {
			case Some(f) => (f, true)
			case None => {
				val f = aggregateFunctionFactory.apply()
				functionMap.put(key, f)
				(f, false)
			}
		}
	}


	private def clear() {
		additionsMap = HashMultimap.create[Key, Domain]()
		deletionsMap = HashMultimap.create[Key, Domain]()
		functionMap = mutable.HashMap.empty[Key, Aggregate]
	}

	override def endTransaction() {

		//Update additions
		val keyAddIt = additionsMap.keys().iterator()
		for (key <- keyAddIt.asScala) {
			val (aggregateFunction, functionExisted) = getFunctionForKey(key)

			val setAsScala = additionsMap.get(key).asScala
			val oldV = aggregateFunction.get

			for (dom <- setAsScala) {
				aggregateFunction.add(dom, setAsScala)
			}

			val newV = aggregateFunction.get

			if (functionExisted)
				notify_updated(convertKeyAndAggregateValueToResult(key, oldV), convertKeyAndAggregateValueToResult(key, newV))
			else
				notify_added(convertKeyAndAggregateValueToResult(key, newV))
		}

		//Update deletions
		val keyDelIt = deletionsMap.keys().iterator()
		for (key <- keyDelIt.asScala) {
			val (aggregateFunction, functionExisted) = getFunctionForKey(key)

			val setAsScala = deletionsMap.get(key).asScala
			val oldV = aggregateFunction.get

			for (dom <- setAsScala) {
				aggregateFunction.remove(dom, setAsScala)
			}

			val newV = aggregateFunction.get

			if (functionExisted)
				notify_updated(convertKeyAndAggregateValueToResult(key, oldV), convertKeyAndAggregateValueToResult(key, newV))
			else
				notify_removed(convertKeyAndAggregateValueToResult(key, newV))
		}

		clear()
		notify_endTransaction()
	}

	override protected def childObservers(o: Observable[_]): Seq[Observer[_]] = {
		if (o == source) {
			return List(this)
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
		throw new UnsupportedOperationException("Method foreach is not implemented for transactional operators.")
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
		deletionsMap.put(groupingFunction(v), v)
	}

	def added(v: Domain) {
		additionsMap.put(groupingFunction(v), v)
	}

}

object TransactionalAggregation {

}






