package sandbox.findbugs

import detect._
import sae.Relation
import sae.syntax.sql._

import sae.bytecode.BytecodeDatabase
import sandbox.stackAnalysis.datastructure.State
import sandbox.dataflowAnalysis.{MethodCFG, MethodResult}
import sae.bytecode.structure.{MethodDeclaration, CodeInfo}
import sandbox.stackAnalysis.codeInfo.CIStackAnalysis
import sae.operators.impl.TransactionalEquiJoinView
import sae.bytecode.instructions.InstructionInfo
import sandbox.stackAnalysis.instructionInfo.{InstructionState, IIStackAnalysis}


/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 09.11.12
 * Time: 14:20
 * To change this template use File | Settings | File Templates.
 */
object IIStackBugAnalysis extends (BytecodeDatabase => Relation[(InstructionInfo, BugLogger)]) {

  var printResults = false

  def apply(bcd: BytecodeDatabase): Relation[(InstructionInfo, BugLogger)] = {
    SELECT((cfv: InstructionState) => (cfv.instruction, computeBugLogger(cfv.instruction, cfv.state))) FROM (IIStackAnalysis(bcd))
  }


  private def computeBugLogger(ii: InstructionInfo, state: State): BugLogger = {
    val logger: BugLogger = new BugLogger()

    for (bugFinder <- CIStackBugAnalysis.BUGFINDER_LIST) {
      bugFinder.computeBugLoggerFromInstructionInfo(ii, state, logger)
    }

    if (printResults && logger.hasLogs) {
      println("Bugs in " + ii.declaringMethod + "\n\t --> " + logger.getLog)
    }

    return logger
  }


}

