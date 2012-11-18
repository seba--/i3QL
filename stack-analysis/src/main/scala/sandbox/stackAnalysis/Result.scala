package sandbox.stackAnalysis

import sandbox.dataflowAnalysis.Combinable

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 04.11.12
 * Time: 20:02
 */
case class Result[T, V](s: Stacks[T,V], l: LocalVars[T,V]) extends Combinable[Result[T,V]] {


  def combineWith(other: Result[T, V]): Result[T, V] = {
    new Result[T, V](s.combineWith(other.s), l.combineWith(other.l))
  }

  override def equals(other: Any): Boolean = {
    if (other == null)
      return false
    if (!other.isInstanceOf[Result[T, V]])
      return false

    val otherSR = other.asInstanceOf[Result[T, V]]
    // println("==def: S " + (s equals otherSR.s) + " / LV " + (l equals otherSR.l))
    return (s equals otherSR.s) && (l equals otherSR.l)
  }


}
