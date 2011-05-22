package sae
package operators
import scala.collection.mutable.ListBuffer

import sae.collections._
import scala.collection.JavaConversions._
import scala.collection.mutable.Map

/**
 * An aggregate operator receives a list of functions and a list of attributes.
 * The functions are evaluated on all tuples that coincide in all supplied attributes.
 * Each function has one (or several) attributes as its domain.
 * The list of attributes serves as a grouping key. This key is used to split the relation into
 * a relation that has only distinct combinations in the supplied attributes.
 * (Note that this grouping is a projection in terms of Codds original relational algebra)
 * The list of grouping attributes is optional.
 * If no grouping is supplied, the aggregation functions are applied on the entire relation.
 * If no function is supplied the aggregation has no effect.
 */

trait Aggregation[Domain <: AnyRef, Key <: Any, AggregationValue <: Any, Result <: AnyRef]
    extends LazyView[Result] {

}

class AggregationIntern[Domain <: AnyRef, Key <: Any, AggregationValue <: Any, Result <: AnyRef](val source : LazyView[Domain], val groupFunction : Domain => Key, aggregationFuncFactory : NotSelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue],
                                                                                                 aggregationConstructorFunction : (Key, AggregationValue) => Result)
    extends Aggregation[Domain, Key, AggregationValue, Result] with Observer[Domain] with MaterializedView[Result] {
    //TODO Evaluate cost of wrapping java.iterabel in scala iterable 

    import com.google.common.collect.HashMultiset;
    var groups = Map[Key, (Count, HashMultiset[Domain], AggregationFunction[Domain, AggregationValue], Result)]()

    lazyInitialize
    initialized = true
    def lazyInitialize : Unit = {

        source.lazy_foreach((v : Domain) => {
            //more or less a copy of added (without notify any observers)
            val key = groupFunction(v)
            if (groups.contains(key)) {
                val (count, data, aggFuncs, oldResult) = groups(key)
                data.add(v)
                count.inc
                val aggRes = aggFuncs.add(v, data)
                val res = aggregationConstructorFunction(key, aggRes)
                if (res != oldResult) {
                    //some aggragation valus changed => updated event
                    groups.put(key, (count, data, aggFuncs, res))
                }
            } else {
                val c = new Count
                c.inc
                val data = HashMultiset.create[Domain]()
                data.add(v)
                val aggFuncs = aggregationFuncFactory()
                val aggRes = aggFuncs.add(v, data)
                val res = aggregationConstructorFunction(key, aggRes)
                //groups.add(key, (c,data,aggFuncs, res))
                groups.put(key, (c, data, aggFuncs, res))
            }
        })

    }

    protected def materialized_foreach[T](f : (Result) => T) : Unit = {
        groups.foreach(x => f(x._2._4))
    }

    protected def materialized_size : Int = groups.size
    protected def materialized_singletonValue : Option[Result] =
        {
            if (size != 1)
                None
            else
                Some(groups.head._2._4)
        }

    source.addObserver(this)

    def updated(oldV : Domain, newV : Domain) {
        val oldKey = groupFunction(oldV)
        val newKey = groupFunction(newV)
        if (oldKey == newKey) {
            val (count, data, aggFuncs, oldResult) = groups(oldKey)
            data.remove(oldV)
            data.add(newV)
            val aggRes = aggFuncs.update(oldV, newV, data)
            val res = aggregationConstructorFunction(oldKey, aggRes)
            groups.put(oldKey, (count, data, aggFuncs, res))
            if (oldResult != res)
                element_updated(oldResult, res)
        } else {
            removed(oldV);
            added(newV);
        }
    }

    def removed(v : Domain) {
        val key = groupFunction(v)
        val (count, data, aggFuncs, oldResult) = groups(key)

        if (count.dec == 0) {
            //remove a group
            groups -= key
            element_removed(oldResult)
        } else {
            data.remove(v)
            val aggRes = aggFuncs.remove(v, data)
            val res = aggregationConstructorFunction(key, aggRes)
            if (res != oldResult) {
                //some aggragation valus changed => updated event
                groups.put(key, (count, data, aggFuncs, res))
                element_updated(oldResult, res)
            }
        }
    }

    def added(v : Domain) {
        val key = groupFunction(v)
        if (groups.contains(key)) {
            val (count, data, aggFuncs, oldResult) = groups(key)
            data.add(v)
            count.inc
            val aggRes = aggFuncs.add(v, data)
            val res = aggregationConstructorFunction(key, aggRes) //FIXME name
            if (res != oldResult) {
                //some aggragation valus changed => updated event
                groups.put(key, (count, data, aggFuncs, res))
                element_updated(oldResult, res)
            }
        } else {
            val c = new Count
            c.inc
            val data = HashMultiset.create[Domain]()
            data.add(v)
            val aggFuncs = aggregationFuncFactory()
            val aggRes = aggFuncs.add(v, data)
            val res = aggregationConstructorFunction(key, aggRes)
            groups.put(key, (c, data, aggFuncs, res))
            element_added(res)
        }
    }
}

class AggregationForSelfMaintainableAggregationFunctions[Domain <: AnyRef, Key <: Any, AggregationValue <: Any, Result <: AnyRef](val source : LazyView[Domain], val groupFunction : Domain => Key, aggregationFuncFactory : SelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue],
                                                                                                                                  aggregationConstructorFunction : (Key, AggregationValue) => Result)
    extends Aggregation[Domain, Key, AggregationValue, Result] with Observer[Domain] with MaterializedView[Result] {
    //TODO Evaluate cost of wrapping java.iterabel in scala iterable 

    import com.google.common.collect.HashMultiset;
    var groups = Map[Key, (Count, AggregationFunction[Domain, AggregationValue], Result)]()
    lazyInitialize
    initialized = true
    def lazyInitialize : Unit = {

        source.lazy_foreach((v : Domain) => {
            //more or less a copy of added (without notify any observers)
            val key = groupFunction(v)
            if (groups.contains(key)) {
                val (count, aggFuncs, oldResult) = groups(key)

                count.inc
                val aggRes = aggFuncs.add(v, null)
                val res = aggregationConstructorFunction(key, aggRes)
                if (res != oldResult) {
                    //some aggragation valus changed => updated event
                    groups.put(key, (count, aggFuncs, res))
                }
            } else {
                val c = new Count
                c.inc
                val aggFuncs = aggregationFuncFactory()
                val aggRes = aggFuncs.add(v, null)
                val res = aggregationConstructorFunction(key, aggRes)
                groups.put(key, (c, aggFuncs, res))
            }
        })

    }

    protected def materialized_foreach[T](f : (Result) => T) : Unit = {
        groups.foreach(x => f(x._2._3))
    }

    protected def materialized_size : Int = groups.size
    protected def materialized_singletonValue : Option[Result] =
        {
            if (size != 1)
                None
            else
                Some(groups.head._2._3)
        }
    //
    //    //lazyInitialize
    //    //initialized = true
    //    def lazyInitialize : Unit = {
    //
    //        source.lazy_foreach((v : Domain) => {
    //            //more or less a copy of added (without notify any observers)
    //            val key = groupFunction(v)
    //            if (groups.contains(key)) {
    //                val (count, aggFuncs, oldResult) = groups(key)
    //                count.inc
    //                val aggRes = aggFuncs.add(v, null)
    //                val res = aggregationConstructorFunction(key, aggRes)
    //                if (res != oldResult) {
    //                    //some aggragation valus changed => updated event
    //                    groups.put(key, (count, aggFuncs, res))
    //                }
    //            } else {
    //                val c = new Count
    //                c.inc
    //                val aggFuncs = aggregationFuncFactory()
    //                val aggRes = aggFuncs.add(v, null)
    //                val res = aggregationConstructorFunction(key, aggRes)
    //                groups.put(key, (c, aggFuncs, res))
    //            }
    //        })
    //
    //    }
    //    def lazy_foreach[T](f : (Result => T)) {
    //        if (initialized == false) {
    //            lazyInitialize
    //            initialized = true
    //        }
    //        groups.foreach((x : (Key, (Count, AggregationFunction[Domain, AggregationValue], Result))) => f(x._2._3))
    //    }

    source.addObserver(this)

    def updated(oldV : Domain, newV : Domain) {
        val oldKey = groupFunction(oldV)
        val newKey = groupFunction(newV)
        if (oldKey == newKey) {
            val (count, aggFuncs, oldResult) = groups(oldKey)
            val aggRes = aggFuncs.update(oldV, newV, null)
            val res = aggregationConstructorFunction(oldKey, aggRes)
            groups.put(oldKey, (count, aggFuncs, res))
            if (oldResult != res)
                element_updated(oldResult, res)
        } else {
            removed(oldV);
            added(newV);
        }
    }

    def removed(v : Domain) {
        val key = groupFunction(v)
        val (count, aggFuncs, oldResult) = groups(key)

        if (count.dec == 0) {
            //remove a group
            groups -= key
            element_removed(oldResult)
        } else {
            val aggRes = aggFuncs.remove(v, null)
            val res = aggregationConstructorFunction(key, aggRes)
            if (res != oldResult) {
                //some aggragation valus changed => updated event
                groups.put(key, (count, aggFuncs, res))
                element_updated(oldResult, res)
            }
        }
    }

    def added(v : Domain) {
        val key = groupFunction(v)
        if (groups.contains(key)) {
            val (count, aggFuncs, oldResult) = groups(key)
            count.inc
            val aggRes = aggFuncs.add(v, null)
            val res = aggregationConstructorFunction(key, aggRes) //FIXME name
            if (res != oldResult) {
                //some aggragation valus changed => updated event
                groups.put(key, (count, aggFuncs, res))
                element_updated(oldResult, res)
            }
        } else {
            val c = new Count
            c.inc
            val aggFuncs = aggregationFuncFactory()
            val aggRes = aggFuncs.add(v, null)
            val res = aggregationConstructorFunction(key, aggRes)
            groups.put(key, (c, aggFuncs, res))
            element_added(res)
        }
    }
}

object Aggregation {
    /**
     * @param source: Lasz Source View
     * @param groupFunciton: the grouping function. return value for all elements in one group must be equal by '==' (return value is used in a hashmap)
     * @param aggregationFuncFactory: a simple or complex aggregation function (factory)
     * @param aggregationConstructorFunction: (x : Result of grouping function, y : Result of Aggregation Function) => Aggregation return value
     */
    def apply[Domain <: AnyRef, Key <: Any, AggregationValue <: Any, Result <: AnyRef](source : LazyView[Domain], groupFunction : Domain => Key, aggregationFuncFactory : NotSelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue],
                                                                                       aggregationConstructorFunction : (Key, AggregationValue) => Result) : Aggregation[Domain, Key, AggregationValue, Result] = {
        new AggregationIntern(source, groupFunction, aggregationFuncFactory, aggregationConstructorFunction)
    }

    def apply[Domain <: AnyRef, Key <: Any, AggregationValue <: Any, Result <: AnyRef](source : LazyView[Domain], groupFunction : Domain => Key, aggregationFuncFactory : SelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue],
                                                                                       aggregationConstructorFunction : (Key, AggregationValue) => Result) : Aggregation[Domain, Key, AggregationValue, Result] = {
        new AggregationForSelfMaintainableAggregationFunctions(source, groupFunction, aggregationFuncFactory, aggregationConstructorFunction)
    }
    def apply[Domain <: AnyRef, AggregationValue <: Any](source : LazyView[Domain], aggregationFuncFactory : NotSelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue]) = {
        new AggregationIntern(source, (x : Any) => "a", aggregationFuncFactory, (x : Any, y : AggregationValue) => Some(y))
    }
    def apply[Domain <: AnyRef, AggregationValue <: Any](source : LazyView[Domain], aggregationFuncFactory : SelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue]) = {
        new AggregationForSelfMaintainableAggregationFunctions(source, (x : Any) => "a", aggregationFuncFactory, (x : Any, y : AggregationValue) => Some(y))
    }

}

class Count {
    private var count : Int = 0
    def inc() = { this.count += 1 }
    def dec() : Int = { this.count -= 1; this.count }
    def apply() = this.count
}