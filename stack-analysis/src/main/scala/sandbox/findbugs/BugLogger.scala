package sandbox.findbugs

import sae.bytecode.structure.MethodDeclaration

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 09.11.12
 * Time: 14:15
 * To change this template use File | Settings | File Templates.
 */
class BugLogger {

  private var logList: List[(MethodDeclaration, Int, BugType.Value)] = Nil

  def log(m: MethodDeclaration, pc: Int, bug: BugType.Value) = {
    logList = (m, pc, bug) :: logList
  }

  def getLog(): List[(MethodDeclaration, Int, BugType.Value)] = {
    logList
  }
}
