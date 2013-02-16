package sandbox.findbugs.detect

import sandbox.stackAnalysis.datastructure.State
import scala.util.control.Breaks._
import sandbox.findbugs._
import sae.bytecode.BytecodeDatabase
import sae.Relation
import sae.syntax.sql._

import sandbox.stackAnalysis.datastructure.Stack
import sae.bytecode.structure.{MethodDeclaration, CodeInfo}
import scala.Some
import sandbox.stackAnalysis.datastructure.LocalVariables
import sandbox.stackAnalysis.codeInfo.CIStackAnalysis
import sandbox.stackAnalysis.codeInfo.CIStackAnalysis.MethodStates
import sae.operators.impl.TransactionalEquiJoinView
import de.tud.cs.st.bat.resolved.Instruction
import sae.bytecode.instructions.InstructionInfo
import sandbox.stackAnalysis.instructionInfo.{StateInfo, IIStackAnalysis}
import sandbox.findbugs.BugInfo
import sandbox.stackAnalysis.datastructure.LocalVariables
import sandbox.stackAnalysis.datastructure.Stack
import sae.bytecode.structure.CodeInfo
import scala.Some
import sandbox.stackAnalysis.StackAnalysis

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 09.11.12
 * Time: 13:54
 * To change this template use File | Settings | File Templates.
 */
trait Detector extends (BytecodeDatabase => Relation[BugInfo]) {

  def getDetectorFunction(instruction: Instruction): (Int, Instruction, Stack, LocalVariables) => Option[BugType.Value]

  def apply(bcd: BytecodeDatabase): Relation[BugInfo] = {
    StateInfoToBugInfoView(StackAnalysis(bcd),this)
  }

  def apply(stateInfos : Relation[StateInfo]) : Relation[BugInfo] = {
    StateInfoToBugInfoView(stateInfos,this)
  }

  def byInstructionInfo(bcd : BytecodeDatabase) : Relation[BugInfo] = {
    StateInfoToBugInfoView(IIStackAnalysis(bcd),this)
  }

  def byCodeInfo(bcd : BytecodeDatabase) : Relation[BugInfo] = {
    StateInfoToBugInfoView(bcd.code,CIStackAnalysis(bcd),this)
  }

  protected def checkNone(pc: Int, instr: Instruction, stack: Stack, lv: LocalVariables): Option[BugType.Value] = {
    return None
  }
}
