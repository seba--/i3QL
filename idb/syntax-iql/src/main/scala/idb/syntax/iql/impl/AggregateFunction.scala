package idb.syntax.iql.impl


import idb.syntax.iql.IR._
import idb.syntax.iql._

/**
 * @author Mirko Köhler
 */
case class AggregateFunction[Select : Manifest, Range : Manifest](
	start : Range,
	added : Rep[((Select, Range)) => Range],
	removed : Rep[((Select, Range)) => Range],
	updated : Rep[((Select, Select, Range)) => Range]
) extends AGGREGATE_FUNCTION[Select, Range]
{

}
