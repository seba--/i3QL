package sandbox.analysis

import collection.immutable.HashSet

/**
 * This class implements the store for local variables for the stack analysis.
 *
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 25.10.12
 * Time: 14:10
 * To change this template use File | Settings | File Templates.
 */
// TODO use lists
case class AnalysisLocalVars[T: Manifest](a: Array[Set[T]]) {


  def this(i: Int) = this(Array.fill[Set[T]](i)(new HashSet[T]))

  def getVar(index: Int): Set[T] =
    varStore(index)

  def setVar(index: Int, value: T): AnalysisLocalVars[T] = {
    val res: Array[Set[T]] = varStore.clone()
    res.update(index, new HashSet[T] + value)
    new AnalysisLocalVars[T](res)
  }

  override def toString = "[" + varStore.mkString(", ") + "]"

}
