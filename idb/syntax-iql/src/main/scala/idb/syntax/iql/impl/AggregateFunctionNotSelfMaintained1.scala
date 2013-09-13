package idb.syntax.iql.impl

import idb.syntax.iql.IR._
import idb.syntax.iql._

/**
 * @author Mirko Köhler
 */
case class AggregateFunctionNotSelfMaintained1[Domain : Manifest, AggregateRange : Manifest](
	start : AggregateRange,
	added : Rep[((Domain, AggregateRange, Iterable[Domain])) => AggregateRange],
	removed : Rep[((Domain, AggregateRange, Iterable[Domain])) => AggregateRange],
	updated : Rep[((Domain, Domain, AggregateRange, Iterable[Domain])) => AggregateRange]
) extends AGGREGATE_FUNCTION_1_NOT_SELF_MAINTAINED[Domain, AggregateRange]
{

}
