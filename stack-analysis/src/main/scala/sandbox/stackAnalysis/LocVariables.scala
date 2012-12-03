package sandbox.stackAnalysis

import sandbox.dataflowAnalysis.Combinable
import de.tud.cs.st.bat.resolved.Type
import sandbox.stackAnalysis.TypeOption.SomeType

/**
 * This class implements the store for local variables for the s analysis.
 *
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 25.10.12
 * Time: 14:10
 */

case class LocVariables(varStore: Array[LocVariable])(implicit m: Manifest[LocVariable]) extends Combinable[LocVariables] {


  //def this(size: Int, t: Option[T], value: List[Option[V]])(implicit m: Manifest[T]) = this(Array.fill[Option[T]](size)(t), Array.fill[List[Option[V]]](size)(value))

  def apply(index : Int) : LocVariable =
     varStore(index)

  private def setVar(index: Int, size : Int, variable : LocVariable) : LocVariables = {
    val resV: Array[LocVariable] = Array.ofDim[LocVariable](varStore.length)

    Array.copy(varStore, 0, resV, 0, varStore.length)

    resV(index) = variable
    for(i <- 1 until size)
      resV(index + i) = variable.makeContinue()

    new LocVariables(resV)
  }

  def setVar(index: Int, t: TypeOption): LocVariables = {
    setVar(index, t.size, LocVariable(t :: Nil))
  }

  def setVar(index: Int, t: Type, value: Int): LocVariables =
    setVar(index, t.computationalType.operandSize, LocVariable(SomeType(t, value) :: Nil))




  def length(): Int = {
    varStore.length
  }

  def combineWith(other: LocVariables): LocVariables = {
    if (other == null)
      this
    else {
      //TODO: this check can be used for testing
 /*     if (length != other.length)
        throw new IllegalArgumentException("Varstores need to have the same length")       */

      val res: Array[LocVariable] = Array.ofDim[LocVariable](length)

      for (i <- 0 until length) {
        res(i) = varStore(i).combineWith(other.varStore(i))
      }

      LocVariables(res)

    }


  }


  def equals(other: LocVariables): Boolean = {
    if (length() != other.length())
      return false

    for (i <- 0 until length()) {
      if (!varStore(i).equals(other.varStore(i)))
        return false
    }

    return true
  }


  override def toString() : String = {
    return varStore.mkString("LocalVars<"+varStore.length+">: ["," || ","]")
  }

}
