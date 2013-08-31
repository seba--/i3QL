package idb.syntax.iql.impl


import idb.syntax.iql.IR._
import idb.syntax.iql._

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 13.08.13
 * Time: 14:59
 * To change this template use File | Settings | File Templates.
 */
case class AggregateFunction1[Select : Manifest, Range : Manifest](
	start : Range,
	added : Rep[((Select, Range)) => Range],
	removed : Rep[((Select, Range)) => Range],
	updated : Rep[((Select, Select, Range)) => Range]
) extends AGGREGATE_FUNCTION_1[Select, Range]
{

}
