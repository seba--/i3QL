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
import sae.bytecode.structure.{MethodDeclaration, CodeInfo}
import scala.Some
import sandbox.stackAnalysis.datastructure.LocalVariables
import sandbox.stackAnalysis.codeInfo.CIStackAnalysis
import sae.operators.impl.TransactionalEquiJoinView
import de.tud.cs.st.bat.resolved.Instruction
import sae.bytecode.instructions.InstructionInfo
import sandbox.stackAnalysis.instructionInfo.{IIStackAnalysis, InstructionState}

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 09.11.12
 * Time: 13:54
 * To change this template use File | Settings | File Templates.
 */
trait StackBugFinder extends (BytecodeDatabase => Relation[BugEntry]) {

  def checkBugs(pc: Int, instruction: Instruction, state: State): (Int, Instruction, Stack, LocalVariables) => Option[BugType.Value]

  private val USE_INSTRUCTIONINFO = false

  def apply(bcd: BytecodeDatabase): Relation[BugEntry] = {
    if (USE_INSTRUCTIONINFO) {
      applyII(bcd.instructions, IIStackAnalysis(bcd))
    } else {
      applyCI(bcd.code, CIStackAnalysis(bcd))
    }
  }

  def applyCI(ci: Relation[CodeInfo], mtr: Relation[MethodResult[State]]): Relation[BugEntry] = {
    new TransactionalEquiJoinView[CodeInfo, MethodResult[State], BugEntry, MethodDeclaration](
      ci,
      mtr,
      codeInfo => codeInfo.declaringMethod,
      methodResult => methodResult.declaringMethod,
      (codeInfo, methodResult) => BugEntry(codeInfo.declaringMethod, computeBugLoggerFromCodeInfo(codeInfo, methodResult, new BugLogger()))
    )

    //compile(SELECT((c: CodeInfo, mr: MethodResult[State]) => BugEntry(c.declaringMethod, computeBugLogger(c, mr))) FROM(ci, mtr) WHERE (((_: CodeInfo).declaringMethod) === ((_: MethodResult[State]).declaringMethod)))
  }

  def applyII(ii: Relation[InstructionInfo], cfv: Relation[InstructionState]): Relation[BugEntry] = {
    new TransactionalEquiJoinView[InstructionInfo, InstructionState, BugEntry, InstructionInfo](
      ii,
      cfv,
      instructionInfo => instructionInfo,
      vertex => vertex.instruction,
      (instructionInfo, vertex) => BugEntry(instructionInfo.declaringMethod, computeBugLoggerFromInstructionInfo(instructionInfo, vertex.state, new BugLogger()))
    )

    //compile(SELECT((c: CodeInfo, mr: MethodResult[State]) => BugEntry(c.declaringMethod, computeBugLogger(c, mr))) FROM(ci, mtr) WHERE (((_: CodeInfo).declaringMethod) === ((_: MethodResult[State]).declaringMethod)))
  }


  def computeBugLoggerFromCodeInfo(ci: CodeInfo, mr: MethodResult[State], logger: BugLogger): BugLogger = {

    val instructionArray = ci.code.instructions
    val analysisArray = mr.resultArray

    var currentPC = 0

    while (currentPC < instructionArray.length && currentPC != -1) {
      val checkMethod = checkBugs(currentPC, instructionArray(currentPC), analysisArray(currentPC))
      for (stack <- analysisArray(currentPC).stacks.stacks) {
        val check = checkMethod(currentPC, instructionArray(currentPC), stack, analysisArray(currentPC).variables)
        check match {
          case Some(x) => logger.log(currentPC, x)
          case None => None
        }
      }
      currentPC = instructionArray(currentPC).indexOfNextInstruction(currentPC, ci.code)
    }

    return logger
  }

  def computeBugLoggerFromInstructionInfo(ii: InstructionInfo, state: State, logger: BugLogger): BugLogger = {
    val checkMethod = checkBugs(ii.pc, ii.instruction, state)
    for (stack <- state.stacks.stacks) {
      val check = checkMethod(ii.pc, ii.instruction, stack, state.variables)
      check match {
        case Some(x) => logger.log(ii.pc, x)
        case None => None
      }
    }

    return logger
  }

  protected def checkNone(pc: Int, instr: Instruction, stack: Stack, lv: LocalVariables): Option[BugType.Value] = {
    return None
  }
}
