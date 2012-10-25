package sandbox.analysis

/**
 * This class implements the store for local variables for the stack analysis.
 *
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 25.10.12
 * Time: 14:10
 * To change this template use File | Settings | File Templates.
 */
class AnalysisLocalVars[T: Manifest](a: Array[T]) {
  private val varStore: Array[T] = a

  def this(i: Int) = this(new Array[T](i))

  def getVar(index: Int): T =
    varStore(index)

  def setVar(index: Int, value: T): AnalysisLocalVars[T] = {
    val res: Array[T] = varStore.clone()
    res.update(index, value)
    new AnalysisLocalVars[T](res)
  }

  override def toString = "[" + varStore.mkString(", ") + "]"

}
