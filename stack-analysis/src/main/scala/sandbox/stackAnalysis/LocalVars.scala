package sandbox.stackAnalysis

import sandbox.dataflowAnalysis.Combinable

/**
 * This class implements the store for local variables for the stack analysis.
 *
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 25.10.12
 * Time: 14:10
 */

case class LocalVars[T](varStore: Array[List[T]]) extends Combinable[LocalVars[T]]{

  def this(i: Int) = this(Array.fill[List[T]](i)(Nil))

  /**
   * Gets all possibilities for variables that a stored at a specified index.
   * @param index The index of the local variable.
   * @return A list with all possible values.
   */
  def getVar(index: Int): List[T] =
    varStore(index)

  def setVar(index: Int, value: T): LocalVars[T] = {
    val res: Array[List[T]] = varStore.clone()
    res.update(index, value :: Nil)
    new LocalVars[T](res)
  }

  def combineWith(other : LocalVars[T]) : LocalVars[T] = {
    if (other == null)
      this
    else
      LocalVars[T](combineWithArray(varStore, other.varStore))
  }

  private def combineWithArray(a : Array[List[T]], b : Array[List[T]]) : Array[List[T]] = {
    val resLength : Int = if (a.length > b.length) b.length else a.length
    val res : Array[List[T]] = Array.fill[List[T]](resLength)(Nil)

    for(i <- 0 until resLength) {
      res(i) = a(i) ++ b(i)
    }

    res
  }

  override def toString = "[" + varStore.mkString(", ") + "]"

}
