package sandbox.stackAnalysis

import sandbox.dataflowAnalysis.Combinable

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 04.11.12
 * Time: 20:02
 * To change this template use File | Settings | File Templates.
 */
case class StackResult[S, L](stack: List[Stack[S]], lv: List[LocalVars[L]]) extends Combinable[StackResult[S, L]] {
  def combineWith(other: StackResult[S, L]): StackResult[S, L] = {
    StackResult[S, L](stack.combineWith(other.stack), lv.combineWith(other.lv))
  }

  override def equals(other: Any): Boolean = {
    if (other == null)
      return false
    if (!other.isInstanceOf[StackResult[S, L]])
      return false

    val otherSR = other.asInstanceOf[StackResult[S, L]]
    // println("==def: S " + (stack equals otherSR.stack) + " / LV " + (lv equals otherSR.lv))
    return (stack equals otherSR.stack) && (lv equals otherSR.lv)
  }


}
