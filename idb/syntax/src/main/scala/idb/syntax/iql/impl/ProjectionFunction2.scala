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
case class ProjectionFunction2[-DomainA : Manifest, -DomainB : Manifest, Range : Manifest](
	projection : (Rep[DomainA], Rep[DomainB]) => Rep[Range]
) extends PROJECTION_FUNCTION_2[DomainA, DomainB, Range] {

}
