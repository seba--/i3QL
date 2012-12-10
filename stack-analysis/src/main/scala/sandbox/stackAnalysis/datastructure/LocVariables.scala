package sandbox.stackAnalysis.datastructure

import sandbox.dataflowAnalysis.Combinable
import de.tud.cs.st.bat.resolved.Type


/**
 * This class implements the store for local variables for the s analysis.
 *
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 25.10.12
 * Time: 14:10
 */

case class LocVariables(varStore: Array[Item])(implicit m: Manifest[Item]) extends Combinable[LocVariables] {


  //def this(size: Int, t: Option[T], value: List[Option[V]])(implicit m: Manifest[T]) = this(Array.fill[Option[T]](size)(t), Array.fill[List[Option[V]]](size)(value))

  def apply(index: Int): Item =
    if (varStore(index) == null)
      Item.createNoneItem
    else
      varStore(index)

  private def setVar(index: Int, size: Int, variable: Item): LocVariables = {
    val resV: Array[Item] = Array.ofDim[Item](varStore.length)

    Array.copy(varStore, 0, resV, 0, varStore.length)

    resV(index) = variable
    for (i <- 1 until size)
      resV(index + i) = Item.createContinue(variable)

    new LocVariables(resV)
  }

  def setVar(index: Int, t: Item): LocVariables = {
    setVar(index, t.size, t)
  }

  def setVar(index: Int, t: Type, pc: Int): LocVariables =
    setVar(index, t.computationalType.operandSize, Item.createItem(ItemType.fromType(t), pc))


  def length(): Int = {
    varStore.length
  }

  def combineWith(other: LocVariables): LocVariables = {
    if (other == null)
      this
    else {

      val res: Array[Item] = Array.ofDim[Item](length)

      for (i <- 0 until length) {
        if (varStore(i) == null)
          res(i) = other.varStore(i)
        else
          res(i) = varStore(i).combineWith(other.varStore(i))
      }

      LocVariables(res)

    }


  }


  def equals(other: LocVariables): Boolean = {
    if (length() != other.length())
      return false

    for (i <- 0 until length()) {
      if (varStore(i) == null)
        return other.varStore(i) == null
      if (!varStore(i).equals(other.varStore(i)))
        return false
    }

    return true
  }


  override def toString(): String = {
    return varStore.mkString("LocalVars<" + varStore.length + ">: [", " || ", "]")
  }

}
