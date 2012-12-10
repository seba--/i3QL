package sandbox.stackAnalysis.datastructure

import sandbox.dataflowAnalysis.Combinable

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 29.11.12
 * Time: 14:39
 * To change this template use File | Settings | File Templates.
 */
case class LocVariable(possibilities: List[Item]) extends Combinable[LocVariable] {

  def apply(index: Int): Item =
    possibilities(index)

  def makeContinue(): LocVariable = {
    var resList: List[Item] = Nil
    for (t <- possibilities) {
      resList = Item.createContinue(t) :: resList
    }
    return LocVariable(resList)
  }

  def combineWith(other: LocVariable): LocVariable = {

    LocVariable((possibilities ++ other.possibilities).distinct)
  }

  def equals(other: LocVariable): Boolean = {
    return possibilities.forall(other.possibilities.contains)
  }

  def isOnly(t: Item): Boolean = {
    possibilities.forall(x => t.equals(x))
  }

  override def toString(): String = {
    return possibilities.mkString("{", "/", "}")
  }

}
