package idb.iql.lms.extensions

import scala.virtualization.lms.common.CompileScala

/**
 *
 * @author Ralf Mitschke
 */
trait CompileScalaUtil
  extends CompileScala with FunctionsExpOptBetaReduction
{
  def compileApplied[A: Manifest, B: Manifest] (f: Rep[A => B]): A => B =
    super.compile (doApply (f, _))
}
