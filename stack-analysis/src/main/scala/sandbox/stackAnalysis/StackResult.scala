package sandbox.stackAnalysis

import sandbox.dataflowAnalysis.Combinable

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 04.11.12
 * Time: 20:02
 * To change this template use File | Settings | File Templates.
 */
case class StackResult[S,L](stack : Stack[S], lv : LocalVars[L]) extends Combinable[StackResult[S,L]] {
  def combineWith(other : StackResult[S,L]) : StackResult[S,L] = {
    StackResult[S,L](stack.combineWith(other.stack) , lv.combineWith(other.lv))
  }


}
