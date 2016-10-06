package idb.syntax.iql.impl

import idb.syntax.iql.IR._
import idb.syntax.iql._

/**
 * @author Mirko Köhler
 */
case class AggregateFunctionNotSelfMaintained[Domain : Manifest, AggregateRange : Manifest](
	start : AggregateRange,
	added : Rep[((Domain, AggregateRange, Seq[Domain])) => AggregateRange],
	removed : Rep[((Domain, AggregateRange, Seq[Domain])) => AggregateRange],
	updated : Rep[((Domain, Domain, AggregateRange, Seq[Domain])) => AggregateRange]
) extends AGGREGATE_FUNCTION_NOT_SELF_MAINTAINED[Domain, AggregateRange]
{

}
