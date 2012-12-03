package sandbox.stackAnalysis

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 29.11.12
 * Time: 15:20
 * To change this template use File | Settings | File Templates.
 */
case class StackEntry(value : TypeOption) {
  private var continue = false

  def makeContinue() : StackEntry  = {
    val res = StackEntry(value)
    res.continue = true
    return res
  }

  def isContinue : Boolean = {
    continue
  }

  def size : Int = {
    value.size
  }

  override def toString() : String = {
    if(isContinue)
      return "Continue"
    else
      return value.toString
  }
}
