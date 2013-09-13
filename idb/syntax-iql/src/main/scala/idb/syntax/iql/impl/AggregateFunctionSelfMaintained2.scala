package idb.syntax.iql.impl

import idb.syntax.iql.IR._
import idb.syntax.iql._

/**
 * @author Mirko Köhler
 */
case class AggregateFunctionSelfMaintained2[DomainA : Manifest, DomainB : Manifest, AggregateRange : Manifest](
	start : AggregateRange,
	added : Rep[( ((DomainA, DomainB), AggregateRange) ) => AggregateRange],
	removed : Rep[( ((DomainA, DomainB), AggregateRange) ) => AggregateRange],
	updated : Rep[( ((DomainA, DomainB), (DomainA, DomainB), AggregateRange) ) => AggregateRange]
) extends AGGREGATE_FUNCTION_2_SELF_MAINTAINED[DomainA, DomainB, AggregateRange]
{

}
