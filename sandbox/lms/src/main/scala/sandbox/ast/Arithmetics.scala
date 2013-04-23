package sandbox.ast

import scala.virtualization.lms.common.{ScalaOpsPkgExp, ScalaOpsPkg, LiftAll}

/**
 *
 * @author Ralf Mitschke
 */
object Arithmetics
  extends LiftAll with ScalaOpsPkg with ScalaOpsPkgExp
{

  def shiftLeft (base: Exp[Int]): Exp[Int] =
  {
    // base << 2 // not supported
    base / 2
  }

  def power (base: Exp[Int])(exp: Exp[Int]): Exp[Int] =
  {
    if (exp == 0)
      base
    else
      base * power (base)(exp - 1)
  }

  def fraction (base: Exp[Int], divisor: Exp[Int]): Exp[Double] =
  {
    base / divisor
  }


}
