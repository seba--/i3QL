package sandbox.findbugs

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 09.11.12
 * Time: 14:15
 * To change this template use File | Settings | File Templates.
 */
class BugLogger {

  private var logList : List[String] = Nil

  def log(s : String) {
    logList = s :: logList
  }

  def getLog() : List[String] = {
    logList
  }
}
