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

	/**
	  * Takes the key of a an element, its aggregate value and the last processed event to create a new result.
	  */
    def convertToResult: (Key, AggregateValue, Domain) => Result

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
object Aggregation {

	def create[Domain, Key, RangeA, RangeB, Range](
		relation : Relation[Domain],
		grouping : Domain => Key,
		start : RangeB,
		added : (Domain, RangeB, Seq[Domain]) => RangeB,
		removed : (Domain, RangeB, Seq[Domain]) => RangeB,
		updated : ( Domain, Domain, RangeB, Seq[Domain]) => RangeB,
		convertKey : Key => RangeA,
		convert : (RangeA, RangeB, Domain) => Range,
		isSet : Boolean
	): AggregationForNotSelfMaintainableFunctions[Domain, Key, RangeB, Range] = {

		val factory  = AggregateFunctionFactory.create(start, added, removed, updated)
		val f = (k : Key, rB : RangeB, d : Domain) => convert(convertKey(k), rB, d)

		new AggregationForNotSelfMaintainableFunctions[Domain, Key, RangeB, Range](
			relation, grouping, factory, f, isSet
		)
	}

	def create[Domain, Key, RangeA, RangeB, Range](
		relation : Relation[Domain],
		grouping : Domain => Key,
		start : RangeB,
		added : (Domain, RangeB) => RangeB,
		removed : (Domain, RangeB) => RangeB,
		updated : (Domain, Domain, RangeB) => RangeB,
		convertKey : Key => RangeA,
		convert : (RangeA, RangeB, Domain) => Range,
		isSet : Boolean
	): AggregationForSelfMaintainableFunctions[Domain, Key, RangeB, Range] = {

		val factory  = AggregateFunctionFactory.create(start, added, removed, updated)
		val f = (k : Key, rB : RangeB, d : Domain) => convert(convertKey(k), rB, d)

		new AggregationForSelfMaintainableFunctions[Domain, Key, RangeB, Range](
			relation, grouping, factory, f, isSet
		)
	}

	def create[Domain, Result](
		relation: Relation[Domain],
		grouping: Domain => Result,
		isSet: Boolean
	): Relation[Result] = {
		create[Domain, Result, Result, Boolean, Result] (
			relation,
			grouping,
			true,
			(d : Domain, rB : Boolean) => true,
			(d : Domain, rB : Boolean) => true,
			(d1 : Domain, d2 : Domain, rB : Boolean) => true,
			(k : Result) => k,
			(k : Result, rB : Boolean, d : Domain) => k,
			isSet
		)
	}

}

