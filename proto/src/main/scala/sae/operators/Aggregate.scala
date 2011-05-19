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
class AggregationIntern[Domain <: AnyRef, Key <: Any, AggregationValue <: Any, Result <: AnyRef](val source : LazyView[Domain], val groupFunction : Domain => Key, aggregationFuncFactory : AggregationFunktionFactory[Domain, AggregationValue],
                                                                                                 aggragationConstructorFunc : (Key, AggregationValue) => Result)
        extends Aggregation[Domain, Key, AggregationValue, Result] with Observer[Domain] with MaterializedView[Result] {
    //TODO Evaluate cost of wrapping java.iterabel in scala iterable 

    import com.google.common.collect.HashMultiset;

    var groups = Map[Key, (Count, HashMultiset[Domain], AggregationFunktion[Domain, AggregationValue], Result)]()

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
                val res = aggragationConstructorFunc(key, aggRes)
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
                val res = aggragationConstructorFunc(key, aggRes)
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

    class Count {
        private var count : Int = 0

        def inc() = { this.count += 1 }
        def dec() : Int = { this.count -= 1; this.count }

        def apply() = this.count
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
            val res = aggragationConstructorFunc(oldKey, aggRes)
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
            val res = aggragationConstructorFunc(key, aggRes)
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
            val res = aggragationConstructorFunc(key, aggRes) //FIXME name
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
            val res = aggragationConstructorFunc(key, aggRes)
            //groups.add(key, (c,data,aggFuncs, res))
            groups.put(key, (c, data, aggFuncs, res))
            element_added(res)
        }
    }
    
    // def toAst = "Aggregation( " + source.toAst + " )"
}

object Aggregation {
    /**
     * @param source: Lasz Source View
     * @param groupFunciton: the grouping function. return value for all elements in one group must be equal by '==' (return value is used in a hashmap) 
     * @param aggregationFuncFactory: a simple or complex aggregation function (factory)  
     * @param aggragationConstructorFunc: (x : Result of grouping function, y : Result of Aggregation Function) => Aggregation return value
     */
    def apply[Domain <: AnyRef, Key <: Any, AggregationValue <: Any, Result <: AnyRef](source : LazyView[Domain], groupFunction : Domain => Key, aggregationFuncFactory : AggregationFunktionFactory[Domain, AggregationValue],
                                                                                       aggragationConstructorFunc : (Key, AggregationValue) => Result) : Aggregation[Domain, Key, AggregationValue, Result] = {
        new AggregationIntern(source, groupFunction, aggregationFuncFactory, aggragationConstructorFunc)
    }

    def apply[Domain <: AnyRef, AggregationValue <: Any](source : LazyView[Domain], aggregationFuncFactory : AggregationFunktionFactory[Domain, AggregationValue]) = {
        new AggregationIntern(source, (x : Any) => "a", aggregationFuncFactory, (x : Any, y : AggregationValue) => Some(y))
    }

}
/*trait AggregationFunktionsFactory[Domain <: AnyRef, AggregationValue] {
    def apply() : AggregationFunktions[Domain, AggregationValue]
}*/
trait AggregationFunktionFactory[Domain <: AnyRef, AggregationValue <: Any] {
    def apply() : AggregationFunktion[Domain, AggregationValue]
}

/*trait AggregationFunktions[Domain <: AnyRef, AggregationValue] {
    def add(newD : Domain, data : Iterable[Domain]) : AggregationValue
    def remove(newD : Domain, data : Iterable[Domain]) : AggregationValue
    def update(oldD : Domain, newD : Domain, data : Iterable[Domain]) : AggregationValue
}*/
trait AggregationFunktion[Domain <: AnyRef, Result] {

    def add(newD : Domain, data : Iterable[Domain]) : Result
    def remove(newD : Domain, data : Iterable[Domain]) : Result
    def update(oldD : Domain, newD : Domain, data : Iterable[Domain]) : Result
}

