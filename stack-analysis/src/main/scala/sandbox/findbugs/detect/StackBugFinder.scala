package sandbox.findbugs.detect

import sandbox.stackAnalysis.datastructure.State
import scala.util.control.Breaks._
import sandbox.findbugs.{BugType, BugLogger}
import sae.bytecode.BytecodeDatabase
import sae.Relation
import sae.syntax.sql._
import sandbox.findbugs.BugEntry
import sandbox.dataflowAnalysis.MethodResult
import sandbox.stackAnalysis.datastructure.Stack
import sae.bytecode.structure.CodeInfo
import scala.Some
import sandbox.stackAnalysis.datastructure.LocVariables
import sandbox.stackAnalysis.codeInfo.StackAnalysis

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 09.11.12
 * Time: 13:54
 * To change this template use File | Settings | File Templates.
 */
trait StackBugFinder extends (BytecodeDatabase => Relation[BugEntry]) with ((Relation[CodeInfo], Relation[MethodResult[State]]) => Relation[BugEntry]) {

  def apply(bcd: BytecodeDatabase): Relation[BugEntry] = {
    apply(bcd.code, StackAnalysis(bcd))
  }

  def apply(ci: Relation[CodeInfo], mtr: Relation[MethodResult[State]]): Relation[BugEntry] = {
    compile(SELECT((c: CodeInfo, mr: MethodResult[State]) => BugEntry(c.declaringMethod, computeBugLogger(c, mr))) FROM(ci, mtr) WHERE (((_: CodeInfo).declaringMethod) === ((_: MethodResult[State]).declaringMethod)))
  }

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


  private def computeBugLogger(ci: CodeInfo, mr: MethodResult[State]): BugLogger = {

    val logger: BugLogger = new BugLogger()
    val instructionArray = ci.code.instructions
    val analysisArray = mr.resultArray

    var currentPC = 0

    while (currentPC < instructionArray.length && currentPC != -1) {
      notifyInstruction(currentPC, ci, analysisArray, logger)
      currentPC = instructionArray(currentPC).indexOfNextInstruction(currentPC, ci.code)
    }

    return logger
  }
}
