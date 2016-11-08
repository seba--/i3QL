package idb.algebra.compiler.boxing

import java.io.PrintStream

import idb.Relation
import idb.lms.extensions.ScalaCodegenExt
import idb.operators.impl.{AggregationForSelfMaintainableFunctions, EquiJoinView}

/**
  * Created by mirko on 18.10.16.
  */
case class BoxedAggregationSelfMaintained[Domain, Key, RangeA, RangeB, Range](
	relation: Relation[Domain],
	grouping: Domain => Key,
	start : RangeB,
	added : ((Domain, RangeB)) => RangeB,
	removed : ((Domain, RangeB)) => RangeB,
	updated: ((Domain, Domain, RangeB)) => RangeB,
	convertKey : Key => RangeA,
	convert : ((RangeA, RangeB)) => Range,
	isSet : Boolean
) extends Relation[Range] {

	@transient var aggregation : AggregationForSelfMaintainableFunctions[Domain, Key, RangeB, Range] = null

	def compile(compiler : ScalaCodegenExt): Unit = {

		aggregation = AggregationForSelfMaintainableFunctions.applyTupled[Domain, Key, RangeA, RangeB, Range] (
			relation,
			grouping = BoxedFunction.compile(grouping, compiler),
			start = start,
			added = BoxedFunction.compile(added, compiler),
			removed = BoxedFunction.compile(removed, compiler),
			updated = BoxedFunction.compile(updated, compiler),
			convertKey = BoxedFunction.compile(convertKey, compiler),
			convert = BoxedFunction.compile(convert, compiler),
			isSet = isSet
		)

		observers.foreach(o => aggregation.addObserver(o))
		observers.clear()
	}

	override def foreach[T](f: (Range) => T): Unit = aggregation.foreach(f)

	override def children: Seq[Relation[_]] = aggregation.children

	override protected def resetInternal(): Unit = aggregation.resetInternal()

	override protected[idb] def printInternal(out : PrintStream)(implicit prefix: String = " "): Unit =
		aggregation.printInternal(out)
}
