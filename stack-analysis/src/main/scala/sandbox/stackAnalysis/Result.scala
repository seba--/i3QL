package sandbox.stackAnalysis

import sandbox.dataflowAnalysis.Combinable

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 04.11.12
 * Time: 20:02
 */
case class Result[S, L](s: Stack[S], l: LocalVars[L]) extends Combinable[Result[S, L]] {


  def combineWith(other: Result[S, L]): Result[S, L] = {
    new Result[S, L](s.combineWith(other.s), l.combineWith(other.l))
  }

  override def equals(other: Any): Boolean = {
    if (other == null)
      return false
    if (!other.isInstanceOf[Result[S, L]])
      return false

    val otherSR = other.asInstanceOf[Result[S, L]]
    // println("==def: S " + (s equals otherSR.s) + " / LV " + (l equals otherSR.l))
    return (s equals otherSR.s) && (l equals otherSR.l)
  }


}
