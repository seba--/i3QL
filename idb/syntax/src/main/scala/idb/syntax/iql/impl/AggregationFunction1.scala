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
case class AggregationFunction1[Domain : Manifest, Range : Manifest](
	start : Rep[Range],
	added : Rep[(Domain, Range)] => Rep[Range],
	removed : Rep[(Domain, Range)] => Rep[Range],
	updated : Rep[(Domain, Domain, Range)] => Rep[Range]
) extends AGGREGATE_FUNCTION_1[Domain, Range]
{

}
