package idb.syntax.iql.impl

import idb.syntax.iql.IR._
import idb.syntax.iql._

/**
 * @author Mirko Köhler
 */
case class AggregateTupledFunctionSelfMaintained[Select : Manifest, Domain : Manifest, RangeA, RangeB, Range : Manifest](
	start : RangeB,
	added : Rep[((Domain, RangeB)) => RangeB],
	removed : Rep[((Domain, RangeB)) => RangeB],
	updated : Rep[((Domain, Domain, RangeB)) => RangeB],
	project : Rep[Select => RangeA],
	convert : Rep[((RangeA, RangeB)) => Range]
) extends AGGREGATE_TUPLED_FUNCTION_SELF_MAINTAINED[Select, Domain, RangeA, RangeB, Range]
{


}
