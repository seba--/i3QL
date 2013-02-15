package sandbox.findbugs

import detect._
import sae.Relation
import sae.syntax.sql._

import sae.bytecode.BytecodeDatabase
import sandbox.stackAnalysis.datastructure.State
import sandbox.stackAnalysis.codeInfo.CIStackAnalysis.MethodStates
import sandbox.dataflowAnalysis.MethodCFG
import sae.bytecode.structure.{MethodDeclaration, CodeInfo}
import sandbox.stackAnalysis.codeInfo.CIStackAnalysis
import sae.operators.impl.TransactionalEquiJoinView


/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 09.11.12
 * Time: 14:20
 * To change this template use File | Settings | File Templates.
 */
object CIStackBugAnalysis extends (BytecodeDatabase => Relation[MethodBugs]) with ((Relation[CodeInfo], Relation[MethodStates]) => Relation[MethodBugs]) {

  val BUGFINDER_LIST: List[BugFinder] =
    RC_REF_COMPARISON ::
      SA_FIELD_SELF_COMPARISON ::
      SA_LOCAL_SELF_ASSIGNMENT ::
      SQL_BAD_PREPARED_STATEMENT_ACCESS ::
      DMI_INVOKING_TOSTRING_ON_ARRAY ::
      RV_RETURN_VALUE_IGNORED ::
      DL_SYNCHRONIZATION ::
      Nil

  var printResults = false

  def apply(bcd: BytecodeDatabase): Relation[MethodBugs] = {
    apply(bcd.code, CIStackAnalysis(bcd))
  }

  def apply(ci: Relation[CodeInfo], mtr: Relation[MethodStates]): Relation[MethodBugs] = {
    new TransactionalEquiJoinView[CodeInfo, MethodStates, MethodBugs, MethodDeclaration](
      ci,
      mtr,
      codeInfo => codeInfo.declaringMethod,
      methodResult => methodResult.declaringMethod,
      (codeInfo, methodResult) => MethodBugs(codeInfo.declaringMethod, computeBugLogger(codeInfo, methodResult))
    )

    //compile(SELECT((c: CodeInfo, mr: MethodResult[State]) => BugEntry(c.declaringMethod, computeBugLogger(c, mr))) FROM(ci, mtr) WHERE (((_: CodeInfo).declaringMethod) === ((_: MethodResult[State]).declaringMethod)))
  }


  private def computeBugLogger(ci: CodeInfo, mr: MethodStates): BugLogger = {
    val logger: BugLogger = new BugLogger()

    for (bugFinder <- CIStackBugAnalysis.BUGFINDER_LIST) {
      bugFinder.computeBugLoggerFromCodeInfo(ci, mr, logger)
    }

    if (printResults && logger.hasLogs) {
      println("Bugs in " + ci.declaringMethod + "\n\t --> " + logger.getLog)
    }

    return logger
  }


}

