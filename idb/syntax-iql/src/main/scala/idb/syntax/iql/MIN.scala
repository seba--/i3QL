package idb.syntax.iql

import idb.syntax.iql.IR._

/**
 * An aggregation function that calculates the sum over a set of domain entries
 *
 * @author Ralf Mitschke
 */
case object MIN
    extends AGGREGATE_FUNCTION_FACTORY_NOT_SELF_MAINTAINED[Int, Int]
{
	def start: Int = scala.Int.MinValue

	def added[Domain](
		v: IR.Rep[Domain],
		previousResult: IR.Rep[Int],
		data: IR.Rep[Iterable[Domain]],
		column: (IR.Rep[Domain]) => IR.Rep[Int]
	): IR.Rep[Int] =
		previousResult.min(column (v))

	def removed[Domain](
		v: IR.Rep[Domain],
		previousResult: IR.Rep[Int],
		data: IR.Rep[Iterable[Domain]],
		column: (IR.Rep[Domain]) => IR.Rep[Int]
	): IR.Rep[Int] = ???

	def updated[Domain](oldV: IR.Rep[Domain], newV: IR.Rep[Domain], previousResult: IR.Rep[Int], data: IR.Rep[Iterable[Domain]], column: (IR.Rep[Domain]) => IR.Rep[Int]): IR.Rep[Int] = ???
}