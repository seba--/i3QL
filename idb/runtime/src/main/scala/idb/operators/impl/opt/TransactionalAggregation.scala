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

	def apply[Domain, Key, AggregateValue, Result](
		source: Relation[Domain],
		grouping: Domain => Key,
		added: Domain => AggregateValue,
		removed: Domain => AggregateValue,
		updated: ((Domain, Domain)) => AggregateValue,
		convert: ((Key, AggregateValue)) => Result,
		isSet: Boolean
	): Relation[Result] = {
		val factory =
			new SelfMaintainableAggregateFunctionFactory[Domain, AggregateValue] {
				override def apply(): SelfMaintainableAggregateFunction[Domain, AggregateValue] = {
					new SelfMaintainableAggregateFunction[Domain, AggregateValue] {
						private var aggregate: Option[AggregateValue] = None

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
							val a = updated((oldD, newD))
							aggregate = Some(a)
							a
						}

						def get: AggregateValue = {
							aggregate match {
								case Some(a) => a
								//TODO Make this better.
								case None => throw new IllegalArgumentException("Aggregation value is not initialized.")
							}
						}
					}
				}
			}

		return new TransactionalAggregation[Domain, Key, AggregateValue, Result](source, grouping, factory.asInstanceOf[AggregateFunctionFactory[Domain,AggregateValue,AggregateFunction[Domain,AggregateValue]]], (x, y) => convert((x, y)), isSet)

	}

	def apply[Domain, Key, AggregateValue, Result](
		source : Relation[Domain],
		grouping : Domain => Key,
		start : AggregateValue,
		added : ((Domain, AggregateValue)) => AggregateValue,
		removed : ((Domain, AggregateValue)) => AggregateValue,
		updated : ((Domain, Domain, AggregateValue)) => AggregateValue,
		convert : ((Key,AggregateValue)) => Result,
		isSet : Boolean
	): Relation[Result] = {
		val factory : SelfMaintainableAggregateFunctionFactory[Domain,AggregateValue] = new SelfMaintainableAggregateFunctionFactory[Domain,AggregateValue] {
			override def apply() : SelfMaintainableAggregateFunction[Domain,AggregateValue] = {
				new SelfMaintainableAggregateFunction[Domain,AggregateValue] {
					private var aggregate : AggregateValue = start

					def add(newD: Domain): AggregateValue = {
						val a = added( (newD, aggregate) )
						aggregate = a
						a
					}

					def remove(newD: Domain): AggregateValue = {
						val a = removed( (newD, aggregate) )
						aggregate = a
						a
					}

					def update(oldD: Domain, newD: Domain): AggregateValue = {
						val a = updated( (oldD, newD, aggregate) )
						aggregate = a
						a
					}

					def get : AggregateValue =
						aggregate
				}
			}
		}

		return new TransactionalAggregation[Domain,Key,AggregateValue,Result](source,grouping,factory.asInstanceOf[AggregateFunctionFactory[Domain,AggregateValue,AggregateFunction[Domain,AggregateValue]]],(x,y) => convert((x,y)),isSet)
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

	def apply[Domain, Result](
		source : Relation[Domain],
		start : Result,
		added : ((Domain, Result)) => Result,
		removed : ((Domain, Result)) => Result,
		updated : ((Domain, Domain, Result)) => Result,
		isSet : Boolean
	): Relation[Result] = {
		apply(source,
			(x : Domain) => true,
			start,
			added,
			removed,
			updated,
			Function.tupled((x : Boolean, y : Result) => y),
			isSet)
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

	def apply[Domain, Result](
		source: Relation[Domain],
		grouping: Domain => Result,
		isSet: Boolean
	): Relation[Result] = {
		apply(source,
			grouping,
			(x :Domain) => true,
			(x :Domain) => true,
			Function.tupled((x : Domain, y : Domain) => true),
			Function.tupled((x : Result, y : Boolean) => x),
			isSet
		)
	}
}






