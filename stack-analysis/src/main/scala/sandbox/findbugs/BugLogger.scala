package sandbox.findbugs


/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 09.11.12
 * Time: 14:15
 * To change this template use File | Settings | File Templates.
 */
class BugLogger {

  private var logList: List[(Int, BugType.Value)] = Nil

  def log(pc: Int, bug: BugType.Value) = {
    logList = (pc, bug) :: logList
  }

  def getLog: List[(Int, BugType.Value)] = {
    logList
  }

  def hasLogs: Boolean = {
    !logList.isEmpty
  }
}
