package sandbox.ast

import scala.virtualization.lms.common._

/**
 *
 * @author: Ralf Mitschke
 */
package object sql
  extends LiftAll with ScalaOpsPkg with ScalaOpsPkgExp
{
  val ir = this
}
