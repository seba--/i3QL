package sandbox.findbugs.detect

import de.tud.cs.st.bat.resolved.Instruction
import sandbox.stackAnalysis.datastructure.{State, Stack, LocVariables}
import scala.util.control.Breaks._
import sandbox.findbugs.{BugType, BugLogger}

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 09.11.12
 * Time: 13:54
 * To change this template use File | Settings | File Templates.
 */
trait StackBugFinder {

  def notifyInstruction(pc: Int, instructions: Array[Instruction], analysis: Array[State], logger: BugLogger)

  def checkForBugs(pc: Int, instructions: Array[Instruction], analysis: Array[State], logger: BugLogger, checkMethod: (Int, Array[Instruction], Stack, LocVariables) => Option[BugType.Value]) = {
    var bug: Option[BugType.Value] = None
    breakable {
      for (stack <- analysis(pc).s.collection) {
        val analysisResult = checkMethod(pc, instructions, stack, analysis(pc).l)
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
