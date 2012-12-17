package sandbox.findbugs

import detect._
import sae.Relation
import sae.syntax.sql._

import sae.bytecode.BytecodeDatabase
import sandbox.stackAnalysis.datastructure.State
import sandbox.dataflowAnalysis.MethodResult
import sae.bytecode.structure.CodeInfo
import sandbox.stackAnalysis.codeInfo.StackAnalysis


/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 09.11.12
 * Time: 14:20
 * To change this template use File | Settings | File Templates.
 */
object StackBugAnalysis extends (BytecodeDatabase => Relation[BugEntry]) with ((Relation[CodeInfo], Relation[MethodResult[State]]) => Relation[BugEntry]) {

  val BUGFINDER_LIST: List[StackBugFinder] =
    RefComparisonFinder ::
      FieldSelfComparisonFinder ::
      LocalSelfAssignmentFinder ::
      BadResultSetAccessFinder ::
      ArrayToStringFinder ::
      ReturnValueIgnoredFinder ::
      SynchronizeBoxedPrimitiveFinder ::
      Nil

  var printResults = false

  def apply(bcd: BytecodeDatabase): Relation[BugEntry] = {
    compile(SELECT((ci: CodeInfo, mr: MethodResult[State]) => BugEntry(ci.declaringMethod, computeBugLogger(ci, mr))) FROM(bcd.code, StackAnalysis(bcd)) WHERE (((_: CodeInfo).declaringMethod) === ((_: MethodResult[State]).declaringMethod)))
  }

  def apply(ci: Relation[CodeInfo], mtr: Relation[MethodResult[State]]): Relation[BugEntry] = {
    compile(SELECT((c: CodeInfo, mr: MethodResult[State]) => BugEntry(c.declaringMethod, computeBugLogger(c, mr))) FROM(ci, mtr) WHERE (((_: CodeInfo).declaringMethod) === ((_: MethodResult[State]).declaringMethod)))
  }


  private def computeBugLogger(ci: CodeInfo, mr: MethodResult[State]): BugLogger = {

    val logger: BugLogger = new BugLogger()
    val instructionArray = ci.code.instructions
    val analysisArray = mr.resultArray

    var currentPC = 0

    while (currentPC < instructionArray.length && currentPC != -1) {
      for (bugFinder <- StackBugAnalysis.BUGFINDER_LIST) {
        bugFinder.notifyInstruction(currentPC, instructionArray, analysisArray, logger)
      }

      currentPC = instructionArray(currentPC).indexOfNextInstruction(currentPC, ci.code)


    }


    if (printResults && logger.hasLogs) {
      println("Bugs in " + ci.declaringMethod + "\n\t --> " + logger.getLog)
    }

    return logger
  }


}

