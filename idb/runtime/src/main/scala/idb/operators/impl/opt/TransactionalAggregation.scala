package idb.operators.impl.opt

import collection.mutable
import idb.Relation
import idb.operators._
import idb.observer.{Observable, NotifyObservers, Observer}
import com.google.common.collect.HashMultimap
import scala.collection.JavaConverters._

/**
 * Transactional aggregation operator for self maintained and not self maintained functions
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

object TransactionalAggregationSelfMaintained {

	def apply[Domain, Key, AggregateValue, Result](
		source: Relation[Domain],
		grouping: Domain => Key,
		start: AggregateValue,
		added: ((Domain, AggregateValue)) => AggregateValue,
		removed: ((Domain, AggregateValue)) => AggregateValue,
		updated: ((Domain, Domain, AggregateValue)) => AggregateValue,
		convert: ((Key, AggregateValue)) => Result,
		isSet: Boolean
	): Relation[Result] = {
		val factory: SelfMaintainableAggregateFunctionFactory[Domain, AggregateValue] = new SelfMaintainableAggregateFunctionFactory[Domain, AggregateValue] {
			override def apply(): SelfMaintainableAggregateFunction[Domain, AggregateValue] = {
				new SelfMaintainableAggregateFunction[Domain, AggregateValue] {
					private var aggregate: AggregateValue = start

					def add(newD: Domain): AggregateValue = {
						val a = added((newD, aggregate))
						aggregate = a
						a
					}

					def remove(newD: Domain): AggregateValue = {
						val a = removed((newD, aggregate))
						aggregate = a
						a
					}

					def update(oldD: Domain, newD: Domain): AggregateValue = {
						val a = updated((oldD, newD, aggregate))
						aggregate = a
						a
					}

					def get: AggregateValue =
						aggregate
				}
			}
		}

		return new TransactionalAggregation[Domain, Key, AggregateValue, Result](
			source,
			grouping,
			factory,
			(x, y) => convert((x, y)),
			isSet
		)
	}

	def apply[Domain, Key, Range](
		source: Relation[Domain],
		grouping: Domain => Key,
		start: Range,
		added: ((Domain, Range)) => Range,
		removed: ((Domain, Range)) => Range,
		updated: ((Domain, Domain, Range)) => Range,
		isSet: Boolean
	): Relation[Range] = {
		apply(
			source,
			grouping,
			start,
			added,
			removed,
			updated,
			Function.tupled((x: Key, y: Range) => y),
			isSet
		)
	}

	def applyTupled[Domain, Key, RangeA, RangeB, Range](
		source: Relation[Domain],
		grouping: Domain => Key,
		start: RangeB,
		added: ((Domain, RangeB)) => RangeB,
		removed: ((Domain, RangeB)) => RangeB,
		updated: ((Domain, Domain, RangeB)) => RangeB,
		convertKey: Key => RangeA,
		convert: ((RangeA, RangeB)) => Range,
		isSet: Boolean
	): Relation[Range] = {
		apply[Domain, Key, RangeB, Range](
			source,
			grouping,
			start,
			added,
			removed,
			updated,
			Function.tupled((x: Key, y: RangeB) => convert((convertKey(x), y))),
			isSet
		)
	}


	def apply[Domain, Result](
		source: Relation[Domain],
		start: Result,
		added: ((Domain, Result)) => Result,
		removed: ((Domain, Result)) => Result,
		updated: ((Domain, Domain, Result)) => Result,
		isSet: Boolean
	): Relation[Result] = {
		apply(source,
			(x: Domain) => true,
			start,
			added,
			removed,
			updated,
			Function.tupled((x: Boolean, y: Result) => y),
			isSet)
	}

	def apply[Domain, Result](
		source: Relation[Domain],
		grouping: Domain => Result,
		isSet: Boolean
	): Relation[Result] = {
		apply(
			source,
			grouping,
			true,
			Function.tupled((x: Domain, y: Boolean) => true),
			Function.tupled((x: Domain, y: Boolean) => true),
			Function.tupled((x: Domain, y: Domain, z: Boolean) => true),
			Function.tupled((x: Result, y: Boolean) => x),
			isSet
		)
	}
}

object TransactionalAggregationNotSelfMaintained {

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

		return new TransactionalAggregation[Domain,Key,AggregateValue,Range](
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

	def applyTupled[Domain, Key, RangeA, RangeB, Range] (
		source: Relation[Domain],
		grouping: Domain => Key,
		start : RangeB,
		added : ((Domain, RangeB, Iterable[Domain])) => RangeB,
		removed : ((Domain, RangeB, Iterable[Domain])) => RangeB,
		updated: ((Domain, Domain, RangeB, Iterable[Domain])) => RangeB,
		convertKey : Key => RangeA,
		convert : ((RangeA, RangeB)) => Range,
		isSet : Boolean
	): Relation[Range] = {
		apply[Domain, Key, RangeB, Range] (
			source,
			grouping,
			start,
			added,
			removed,
			updated,
			Function.tupled((x : Key, y : RangeB) => convert ((convertKey (x), y)) ),
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






