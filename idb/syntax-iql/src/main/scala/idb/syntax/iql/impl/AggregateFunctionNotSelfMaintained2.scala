package idb.syntax.iql.impl

import idb.syntax.iql.IR._
import idb.syntax.iql._

/**
 * @author Mirko Köhler
 */
case class AggregateFunctionNotSelfMaintained2[DomainA : Manifest, DomainB : Manifest, AggregateRange : Manifest](
	start : AggregateRange,
	added : Rep[( ((DomainA, DomainB), AggregateRange, Iterable[(DomainA, DomainB)]) ) => AggregateRange],
	removed : Rep[( ((DomainA, DomainB), AggregateRange, Iterable[(DomainA, DomainB)]) ) => AggregateRange],
	updated : Rep[( ((DomainA, DomainB), (DomainA, DomainB), AggregateRange, Iterable[(DomainA, DomainB)]) ) => AggregateRange]
) extends AGGREGATE_FUNCTION_2_NOT_SELF_MAINTAINED[DomainA, DomainB, AggregateRange]
{

}
