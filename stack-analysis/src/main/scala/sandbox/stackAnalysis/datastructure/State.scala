package sandbox.stackAnalysis.datastructure

import sae.operators.Combinable

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 04.11.12
 * Time: 20:02
 */
case class State(s: Stacks, l: LocVariables) extends Combinable[State] {

  def combineWith(other: State): State = {
    new State(s.combineWith(other.s), l.combineWith(other.l))
  }

  override def equals(other: Any): Boolean = {
    if (other == null)
      return false
    if (!other.isInstanceOf[State])
      return false

    val otherSR = other.asInstanceOf[State]

    return (s equals otherSR.s) && (l equals otherSR.l)
  }


}

case object State {
  def createEmptyState(maxStack: Int, maxLoc: Int): State =
    State(Stacks(maxStack, Nil), LocVariables(Array.ofDim[Item](maxLoc)))

  def createStartState(maxStack: Int, maxLoc: Int): State =
    State(Stacks(maxStack, Nil).addStack, LocVariables(Array.ofDim[Item](maxLoc)))
}
