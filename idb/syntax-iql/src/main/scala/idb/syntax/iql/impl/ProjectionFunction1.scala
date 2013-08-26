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
case class ProjectionFunction1[Domain : Manifest, Range : Manifest](
	projection : Rep[Domain => Range]
) extends PROJECTION_FUNCTION_1[Domain, Range]
{

}
