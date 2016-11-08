package idb.operators

import java.io.PrintStream

import impl.{AggregationForNotSelfMaintainableFunctions, AggregationForSelfMaintainableFunctions}
import idb.{MaterializedView, Relation, View}


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
 *
 * @author Malte V
 * @author Ralf Mitschke
 */
trait Aggregation[Domain, Key, AggregateValue, Result, AggregateFunctionType <: AggregateFunction[Domain, AggregateValue], AggregateFunctionFactoryType <: AggregateFunctionFactory[Domain, AggregateValue, AggregateFunctionType]]
    extends View[Result]
{
    def relation: Relation[Domain]

    def groupingFunction: Domain => Key

    def aggregateFunctionFactory: AggregateFunctionFactoryType

    def convertKeyAndAggregateValueToResult: (Key, AggregateValue) => Result

    override def children = List (relation)

    override protected[idb] def printInternal(out : PrintStream)(implicit prefix: String = " "): Unit = {
        out.println(prefix + s"Aggregation(grouping=$groupingFunction, aggregation=$aggregateFunctionFactory,")
        printNested(out, relation)
        out.println(prefix + ")")
    }

}

/**
 * Object to create an Aggregation over a source Relation
 *
 * @author Malte V
 */
object Aggregation
{
    /**
     * Construct a new Aggregation for NOT self maintainable aggregation functions
     * {@see sae.operators.NotSelfMaintainableAggregateFunctionFactory}
     * @param source: Source relation
     * @param groupingFunction: Grouping function for the Aggregation. The return value is used as a key in a hashmap.
     * @param aggregationFunctionFactory: a factory that creates aggregatonFunctions  { @see sae.functions}
     * @param convertKeyAndAggregationValueToResult: function that defines the return type of the aggregation. (x : Grouping key(s), y : aggregation function return value) => Aggregation return value
     * @return LazyInitializedQueryResult[Type of convertKeyAndAggregationValueToResult retrunvalue] aggregation as LazyInitializedQueryResult
     */
    def apply[Domain, Key, AggregateValue, Result](source: Relation[Domain],
                                                   groupingFunction: Domain => Key,
                                                   aggregateFunctionFactory: NotSelfMaintainableAggregateFunctionFactory[Domain, AggregateValue],
                                                   convertKeyAndAggregateValueToResult: (Key, AggregateValue) => Result,
												   isSet : Boolean):
    Aggregation[Domain, Key, AggregateValue, Result, NotSelfMaintainableAggregateFunction[Domain, AggregateValue], NotSelfMaintainableAggregateFunctionFactory[Domain, AggregateValue]] =
    {
        new AggregationForNotSelfMaintainableFunctions[Domain, Key, AggregateValue, Result](source, groupingFunction, aggregateFunctionFactory, convertKeyAndAggregateValueToResult,isSet)
    }


    /**
     * Construct a new Aggregation for NOT self maintainable aggregation functions
     *
     * {@see sae.operators.NotSelfMaintainableAggregateFunctionFactory}
     * @param source: Source relation
     * @param groupingFunction: Grouping function for the Aggregation. The return value is used as a key in a hashmap.
     * @param aggregationFunctionFactory: a factory that creates aggregatonFunctions  { @see sae.functions}
     * @return LazyInitializedQueryResult[(groupingFunction return type, aggregationFunction retun type] aggregation as LazyInitializedQueryResult
     */
    def apply[Domain, Key, AggregateValue](source: Relation[Domain],
                                           groupingFunction: Domain => Key,
                                           aggregateFunctionFactory: NotSelfMaintainableAggregateFunctionFactory[Domain, AggregateValue],
										   isSet : Boolean):
    Aggregation[Domain, Key, AggregateValue, (Key, AggregateValue), NotSelfMaintainableAggregateFunction[Domain, AggregateValue], NotSelfMaintainableAggregateFunctionFactory[Domain, AggregateValue]] =
    {
        new AggregationForNotSelfMaintainableFunctions[Domain, Key, AggregateValue, (Key, AggregateValue)](source, groupingFunction, aggregateFunctionFactory, (a: Key,
                                                                                                                                                                b: AggregateValue) => (a, b),isSet)
    }

    /**
     * Construct a new Aggregation for SELF maintainable aggregation functions
     * {@see sae.operators.SelfMaintainableAggregateFunctionFactory}
     * @param source: Source relation
     * @param groupingFunction: Grouping function for the Aggregation. The return value is used as a key in a hashmap.
     * @param aggregationFunctionFactory: a factory that creates aggregatonFunctions  { @see sae.functions}.
     * @param convertKeyAndAggregationValueToResult: fucntion that defines the return type of the aggregation. (x : Grouping key(s), y : aggregation function return value) => Aggregation return value
     * @return LazyInitializedQueryResult[convertKeyAndAggregationValueToResult returnvalue type] aggregation as LazyInitializedQueryResult
     */
    def apply[Domain, Key, AggregateValue, Result](source: Relation[Domain],
                                                   groupFunction: Domain => Key,
                                                   aggregationFunctionFactory: SelfMaintainableAggregateFunctionFactory[Domain, AggregateValue],
                                                   convertKeyAndAggregationValueToResult: (Key, AggregateValue) => Result,
												   isSet : Boolean):
    Aggregation[Domain, Key, AggregateValue, Result, SelfMaintainableAggregateFunction[Domain, AggregateValue], SelfMaintainableAggregateFunctionFactory[Domain, AggregateValue]] =
    {
        new AggregationForSelfMaintainableFunctions[Domain, Key, AggregateValue, Result](source, groupFunction, aggregationFunctionFactory, convertKeyAndAggregationValueToResult,isSet)
    }

    /**
     * Construct a new Aggregation for SELF maintainable aggregation functions
     * {@see sae.operators.SelfMaintainableAggregateFunctionFactory}
     * @param source: Source relation
     * @param groupingFunction: Grouping function for the Aggregation. The return value is used as a key in a hashmap.
     * @param aggregationFunctionFactory: a factory that creates aggregatonFunctions  { @see sae.functions}.
     * @return LazyInitializedQueryResult[(groupingFunction return type,  aggregationFunctionFactory return type] aggregation as LazyInitializedQueryResult
     */
    def apply[Domain, Key, AggregateValue](source: Relation[Domain],
                                           groupingFunction: Domain => Key,
                                           aggregateFunctionFactory: SelfMaintainableAggregateFunctionFactory[Domain, AggregateValue],
										   isSet : Boolean):
    Aggregation[Domain, Key, AggregateValue, (Key, AggregateValue), SelfMaintainableAggregateFunction[Domain, AggregateValue], SelfMaintainableAggregateFunctionFactory[Domain, AggregateValue]] =
    {
        new AggregationForSelfMaintainableFunctions[Domain, Key, AggregateValue, (Key, AggregateValue)](source, groupingFunction, aggregateFunctionFactory, (a: Key,
                                                                                                                                                                        b: AggregateValue) => (a, b),isSet)
    }

    /**
     * Construct a new Aggregation for aggregation function that are NOT self maintainable without any grouping
     * (the aggregation function is used on the whole source relation)
     * {@see sae.operators.NotSelfMaintainableAggregateFunctionFactory}
     * @param source: Source relation
     * @param groupingFunction: Grouping function for the Aggregation. The return value is used as a key in a hashmap.
     * @param aggregationFunctionFactory: a factory that creates aggregatonFunctions  { @see sae.functions}.
     * @return LazyInitializedQueryResult[(groupingFunction return type, Option[aggregationFunction return type])] aggregation as LazyInitializedQueryResult
     */
    def apply[Domain, AggregateValue](source: Relation[Domain],
                                      aggregateFunctionFactory: NotSelfMaintainableAggregateFunctionFactory[Domain, AggregateValue],
									  isSet : Boolean) =
    {
        new AggregationForNotSelfMaintainableFunctions (source, (x: Any) => 0, aggregateFunctionFactory, (x: Any,
                                                                                                            y: AggregateValue) => Some (y),isSet)
    }

    /**
     * Construct a new Aggregation for aggregation function that are SELF maintainable without any grouping
     * (the aggregation function is used on the whole source relation)
     * {@see sae.operators.SelfMaintainableAggregateFunctionFactory}
     * @param source: Source relation
     * @param groupingFunction: Grouping function for the Aggregation. The return value is used as a key in a hashmap.
     * @param aggregationFunctionFactory: a factory that creates aggregatonFunctions  { @see sae.functions}.
     * @return LazyInitializedQueryResult[(groupingFunction return type, Option[aggregationFunction return type])] aggregation as LazyInitializedQueryResult
     */
    def apply[Domain, AggregateValue](source: Relation[Domain],
                                      aggregateFunctionFactory: SelfMaintainableAggregateFunctionFactory[Domain, AggregateValue],
									  isSet : Boolean) =
    {
        new AggregationForSelfMaintainableFunctions (source, (x: Any) => 0, aggregateFunctionFactory, (x: Any,
                                                                                                                    y: AggregateValue) => Some (y), isSet)
    }

}

