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
    RC_REF_COMPARISON ::
      SA_FIELD_SELF_COMPARISON ::
      SA_LOCAL_SELF_ASSIGNMENT ::
      SQL_BAD_PREPARED_STATEMENT_ACCESS ::
      DMI_INVOKING_TOSTRING_ON_ARRAY ::
      RV_RETURN_VALUE_IGNORED ::
      DL_SYNCHRONIZATION ::
      Nil

  var printResults = false

  def apply(bcd: BytecodeDatabase): Relation[BugEntry] = {
    apply(bcd.code, StackAnalysis(bcd))
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
        bugFinder.notifyInstruction(currentPC, ci, analysisArray, logger)
      }

      currentPC = instructionArray(currentPC).indexOfNextInstruction(currentPC, ci.code)
    }


    if (printResults && logger.hasLogs) {
      println("Bugs in " + ci.declaringMethod + "\n\t --> " + logger.getLog)
    }

    return logger
  }


}

