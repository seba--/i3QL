package sandbox.stackAnalysis

import sandbox.dataflowAnalysis.Combinable
import sandbox.stackAnalysis.TypeOption.{ContinueType, SomeType, NoneType}

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 29.11.12
 * Time: 14:39
 * To change this template use File | Settings | File Templates.
 */
case class LocVariable(possibilities : List[TypeOption]) extends Combinable[LocVariable] {

  def apply(index : Int) : TypeOption =
    possibilities(index)

  def makeContinue() : LocVariable  = {
    var resList : List[TypeOption] = Nil
    for(t <- possibilities) {
      if(t.isInstanceOf[SomeType]) {
        resList = ContinueType(t.asInstanceOf[SomeType]) :: resList
      } else {
        resList = t :: resList
      }
    }
    return LocVariable(resList)
  }

  def combineWith(other : LocVariable) : LocVariable = {

    LocVariable((possibilities ++ other.possibilities).distinct)
  }

  def equals(other: LocVariable): Boolean = {
    return possibilities.forall(other.possibilities.contains)
  }

  def declaredType : TypeOption = {
    var res : TypeOption = NoneType
    for(t <- possibilities) {
      res = res.max(t)
    }
    return res
  }

  override def toString() : String = {
    return possibilities.mkString("{","/","}")
  }

}
