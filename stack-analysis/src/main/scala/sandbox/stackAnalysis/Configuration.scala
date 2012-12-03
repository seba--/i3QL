package sandbox.stackAnalysis

import sandbox.dataflowAnalysis.Combinable

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 04.11.12
 * Time: 20:02
 */
case class Configuration(s: Stacks, l: LocVariables) extends Combinable[Configuration] {


  def combineWith(other: Configuration): Configuration = {
    new Configuration(s.combineWith(other.s), l.combineWith(other.l))
  }

  override def equals(other: Any): Boolean = {
    if (other == null)
      return false
    if (!other.isInstanceOf[Configuration])
      return false

    val otherSR = other.asInstanceOf[Configuration]

    return (s equals otherSR.s) && (l equals otherSR.l)
  }


}
