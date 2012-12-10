package sandbox.stackAnalysis.datastructure

import sandbox.dataflowAnalysis.Combinable

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
