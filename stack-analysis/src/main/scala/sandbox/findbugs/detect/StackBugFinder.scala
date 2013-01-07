package sandbox.findbugs.detect

import sandbox.stackAnalysis.datastructure.{State, Stack, LocVariables}
import scala.util.control.Breaks._
import sandbox.findbugs.{BugType, BugLogger}
import sae.bytecode.structure.CodeInfo

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 09.11.12
 * Time: 13:54
 * To change this template use File | Settings | File Templates.
 */
trait StackBugFinder {

  def notifyInstruction(pc: Int, codeInfo: CodeInfo, analysis: Array[State], logger: BugLogger)

  def checkForBugs(pc: Int, codeInfo: CodeInfo, analysis: Array[State], logger: BugLogger, checkMethod: (Int, CodeInfo, Stack, LocVariables) => Option[BugType.Value]) = {
    var bug: Option[BugType.Value] = None
    breakable {
      for (stack <- analysis(pc).s.collection) {
        val analysisResult = checkMethod(pc, codeInfo, stack, analysis(pc).l)
        if (analysisResult != None) {
          bug = analysisResult
        } else {
          bug = None
          break
        }
      }
    }
    bug match {
      case Some(x) => logger.log(pc, x)
      case None =>
    }
  }
}
