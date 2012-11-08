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

case class LocalVars[T](varStore: Array[List[Option[T]]]) extends Combinable[LocalVars[T]] {


  def this(i: Int, l: List[Option[T]]) = this(Array.fill[List[Option[T]]](i)(l))

  /**
   * Gets all possibilities for variables that a stored at a specified index.
   * @param index The index of the local variable.
   * @return A list with all possible values.
   */
  def getVar(index: Int): List[Option[T]] =
    varStore(index)

  def addVar(index: Int, value: Option[T]): LocalVars[T] = {
    if (varStore(index).contains(value))
      return this

    var res: Array[List[Option[T]]] = Array.ofDim[List[Option[T]]](varStore.length)
    System.arraycopy(varStore, 0, res, 0, varStore.length)
    res.update(index, value :: res(index))

    new LocalVars[T](res)
  }

  def addVar(index: Int, value: T): LocalVars[T] = {
    addVar(index, Some(value))
  }

  def removeVar(index: Int, value: Option[T]): LocalVars[T] = {
    if (!varStore(index).contains(value))
      return this

    var res: Array[List[Option[T]]] = Array.ofDim[List[Option[T]]](varStore.length)
    System.arraycopy(varStore, 0, res, 0, varStore.length)
    res.update(index, res(index).filter(a => !(a equals value)))

    new LocalVars[T](res)
  }

  def setVar(index: Int, value: Option[T]): LocalVars[T] = {
    var res: Array[List[Option[T]]] = Array.ofDim[List[Option[T]]](varStore.length)
    System.arraycopy(varStore, 0, res, 0, varStore.length)
    res.update(index, value :: Nil)

    new LocalVars[T](res)
  }

  def setVar(index: Int, value: T): LocalVars[T] = {
    setVar(index, Some(value))
  }

  def length(): Int = {
    varStore.length
  }

  def combineWith(other: LocalVars[T]): LocalVars[T] = {
    if (other == null)
      this
    else
      LocalVars[T](combineWithArray[Option[T]](varStore, other.varStore))
  }

  private def combineWithArray[S](a: Array[List[S]], b: Array[List[S]]): Array[List[S]] = {
    val resLength: Int = if (a.length > b.length) b.length else a.length
    val res: Array[List[S]] = Array.ofDim[List[S]](resLength)

    for (i <- 0 until resLength) {
      res(i) = CodeInfoTools.distinctAppend(a(i), b(i))
    }

    res
  }

  override def toString = {
    var stringList: List[String] = Nil
    for (l <- varStore) {
      var stringList2: List[String] = Nil
      for (o <- l)
        o match {
          case None => stringList2 = "None" :: stringList2
          case Some(x) => stringList2 = x.toString :: stringList2
        }
      stringList = stringList2.mkString("{", ",", "}") :: stringList
    }



    stringList.reverse.mkString("LV: [", "; ", "]")
  }

  def equals(other: LocalVars[T]): Boolean = {
    if (length() != other.length())
      return false

    for (i <- 0 until length()) {
      if (!(varStore(i) == other.varStore(i)))
        return false
    }

    return true
  }

}
