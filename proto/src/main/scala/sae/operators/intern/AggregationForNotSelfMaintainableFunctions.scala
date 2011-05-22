package sae.operators.intern
import sae._

import sae.operators._
import sae.collections._
import scala.collection.JavaConversions._
import scala.collection.mutable.Map
/**
 * an implementaion of Aggregation that saves for all groups the corresponding domain entries.
 * so that an aggregation function like min can use this data if necessary
 */
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