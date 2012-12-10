package sandbox.findbugs

import detect._
import sandbox.stackAnalysis.StackAnalysis
import sae.Relation
import sae.syntax.sql._

import sae.bytecode.BytecodeDatabase
import sandbox.stackAnalysis.datastructure.State
import sandbox.dataflowAnalysis.MethodResult
import sae.bytecode.structure.CodeInfo


/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 09.11.12
 * Time: 14:20
 * To change this template use File | Settings | File Templates.
 */
object StackBugAnalysis extends (BytecodeDatabase => Relation[BugEntry]) {

  val BUGFINDER_LIST: List[StackBugFinder] = RefComparisonFinder :: FieldSelfComparisonFinder :: LocalSelfAssignmentFinder :: PuzzlerFinder :: BadResultSetAccessFinder :: Nil

  var printResults = false

  def apply(bcd: BytecodeDatabase): Relation[BugEntry] = {
    compile(SELECT((ci: CodeInfo, mr: MethodResult[State]) => BugEntry(ci.declaringMethod, computeBugLogger(ci, mr))) FROM(bcd.code, StackAnalysis(bcd)) WHERE (((_: CodeInfo).declaringMethod) === ((_: MethodResult[State]).declaringMethod)))
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
    if (printResults) {
      println("BugFinder: " + logger.getLog())
    }

    return logger
  }

}

