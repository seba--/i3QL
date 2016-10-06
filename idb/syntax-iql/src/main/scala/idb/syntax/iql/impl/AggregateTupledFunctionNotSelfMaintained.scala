package idb.syntax.iql.impl

import idb.syntax.iql.IR._
import idb.syntax.iql._

/**
 * @author Mirko Köhler
 */
case class AggregateTupledFunctionNotSelfMaintained[Select : Manifest, Domain : Manifest, RangeA, RangeB, Range : Manifest](
	start : RangeB,
	added : Rep[((Domain, RangeB, Seq[Domain])) => RangeB],
	removed : Rep[((Domain, RangeB, Seq[Domain])) => RangeB],
	updated : Rep[((Domain, Domain, RangeB, Seq[Domain])) => RangeB],
	project : Rep[Select => RangeA],
	convert : Rep[((RangeA, RangeB)) => Range]
) extends AGGREGATE_TUPLED_FUNCTION_NOT_SELF_MAINTAINED[Select, Domain, RangeA, RangeB, Range]
{


}
