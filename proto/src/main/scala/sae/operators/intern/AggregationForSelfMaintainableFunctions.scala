package sae.operators.intern
import sae._

import sae.operators._
import sae.collections._
import scala.collection.JavaConversions._
import scala.collection.mutable.Map

/**
 * an inplementaion of Aggregation that only saves the result of aggregation function (aggregationFuncFactory)
 */
class AggregationForSelfMaintainableAggregationFunctions[Domain <: AnyRef, Key <: Any, AggregationValue <: Any, Result <: AnyRef](val source : LazyView[Domain], val groupFunction : Domain => Key, val aggregationFuncFactory : SelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue],
                                                                                                                                  val aggregationConstructorFunction : (Key, AggregationValue) => Result)
    extends Aggregation[Domain, Key, AggregationValue, Result] with Observer[Domain] with MaterializedView[Result] {


    import com.google.common.collect.HashMultiset;
    val groups = Map[Key, (Count, SelfMaintainalbeAggregationFunction[Domain, AggregationValue], Result)]()
    lazyInitialize

    def lazyInitialize : Unit = {
         if(!initialized){
        source.lazy_foreach((v : Domain) => {
            //more or less a copy of added (without notify any observers)
            val key = groupFunction(v)
            if (groups.contains(key)) {
                val (count, aggFuncs, oldResult) = groups(key)

                count.inc
                val aggRes = aggFuncs.add(v)
                val res = aggregationConstructorFunction(key, aggRes)
                if (res != oldResult) {
                    //some aggragation valus changed => updated event
                    groups.put(key, (count, aggFuncs, res))
                }
            } else {
                val c = new Count
                c.inc
                val aggFuncs = aggregationFuncFactory()
                val aggRes = aggFuncs.add(v)
                val res = aggregationConstructorFunction(key, aggRes)
                groups.put(key, (c, aggFuncs, res))
            }
        })
          initialized = true    }
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

    // TODO try giving a more efficient implementation
    protected def materialized_contains(v: Result) =
    {
        var contained = false
        groups.foreach( g =>
            {
                if( g._2._3 == v)
                    true
            }
        )
        false
    }

    source.addObserver(this)

    def updated(oldV : Domain, newV : Domain) {
        val oldKey = groupFunction(oldV)
        val newKey = groupFunction(newV)
        if (oldKey == newKey) {
            val (count, aggFuncs, oldResult) = groups(oldKey)
            val aggRes = aggFuncs.update(oldV, newV)
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
            val aggRes = aggFuncs.remove(v)
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
            val aggRes = aggFuncs.add(v)
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
            val aggRes = aggFuncs.add(v)
            val res = aggregationConstructorFunction(key, aggRes)
            groups.put(key, (c, aggFuncs, res))
            element_added(res)
        }
    }
}