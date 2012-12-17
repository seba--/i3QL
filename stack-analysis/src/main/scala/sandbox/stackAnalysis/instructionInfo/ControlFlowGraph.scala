package sandbox.stackAnalysis.instructionInfo

import sae.bytecode.BytecodeDatabase
import sae.Relation
import sae.syntax.sql._
import ast.UnionAll
import sae.bytecode.instructions.InstructionInfo
import de.tud.cs.st.bat.resolved._

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 16.12.12
 * Time: 13:18
 * To change this template use File | Settings | File Templates.
 */
object ControlFlowGraph extends (BytecodeDatabase => Relation[ControlFlowEdge]) {

  def apply(bcd: BytecodeDatabase): Relation[ControlFlowEdge] = {

    val relNormal = computeNormalInstructions(bcd)
    val relUnconditionalBranchs = computeUnconditionalBranchs(bcd)
    val relConditionalBranchs = computeConditionalBranchs(bcd)

    compile(

      UnionAll(
        //control flow for normal instructions and unconditional branches assuming no branch
        SELECT((current: InstructionInfo, next: InstructionInfo) => getEdge(current, next)) FROM
          (relNormal, bcd.instructions) WHERE
          (((_: InstructionInfo).declaringMethod) === ((_: InstructionInfo).declaringMethod)) AND
          (((_: InstructionInfo).sequenceIndex) === ((_: InstructionInfo).sequenceIndex - 1)),
        UnionAll(
          //control flow for unconditional branches
          SELECT((current: InstructionInfo, next: InstructionInfo) => getEdge(current, next)) FROM
            (relUnconditionalBranchs, bcd.instructions) WHERE
            (((_: InstructionInfo).declaringMethod) === ((_: InstructionInfo).declaringMethod)) AND
            ((getUnconditionalNextPC(_: InstructionInfo)) === ((_: InstructionInfo).pc)),

          UnionAll(
            //control flow for conditional branches assuming branch
            SELECT((current: InstructionInfo, next: InstructionInfo) => getEdge(current, next)) FROM
              (relConditionalBranchs, bcd.instructions) WHERE
              (((_: InstructionInfo).declaringMethod) === ((_: InstructionInfo).declaringMethod)) AND
              ((getConditionalNextPCAssumingBranch(_: InstructionInfo)) === ((_: InstructionInfo).pc)),
            //control flow from starting edges
            SELECT((next: InstructionInfo) => getStartingEdge(next)) FROM
              (bcd.instructions) WHERE (((_: InstructionInfo).pc) === 0)))))
  }

  private def computeNormalInstructions(bcd: BytecodeDatabase): Relation[InstructionInfo] = {
    compile(SELECT(*) FROM (bcd.instructions) WHERE (!(_: InstructionInfo).instruction.isInstanceOf[ControlTransferInstruction]) OR ((_: InstructionInfo).instruction.isInstanceOf[ConditionalBranchInstruction]))
  }

  private def computeUnconditionalBranchs(bcd: BytecodeDatabase): Relation[InstructionInfo] = {
    compile(SELECT(*) FROM (bcd.instructions) WHERE ((_: InstructionInfo).instruction.isInstanceOf[UnconditionalBranchInstruction]))
  }

  private def computeConditionalBranchs(bcd: BytecodeDatabase): Relation[InstructionInfo] = {
    compile(SELECT(*) FROM (bcd.instructions) WHERE ((_: InstructionInfo).instruction.isInstanceOf[ConditionalBranchInstruction]))
  }

  private def getStartingEdge(next: InstructionInfo): ControlFlowEdge = {
    val res = ControlFlowEdge(new ControlFlowVertex(), new ControlFlowVertex(next))
    println(res)
    return res
  }

  private def getEdge(current: InstructionInfo, next: InstructionInfo): ControlFlowEdge = {
    val res = ControlFlowEdge(new ControlFlowVertex(current), new ControlFlowVertex(next))
    println(res)
    return res
  }

  private def getUnconditionalNextPC(ii: InstructionInfo): Int = {
    val branchInstr = ii.instruction.asInstanceOf[UnconditionalBranchInstruction]
    return ii.pc + branchInstr.branchoffset
  }

  private def getConditionalNextPCAssumingBranch(ii: InstructionInfo): Int = {
    val branchInstr = ii.instruction.asInstanceOf[ConditionalBranchInstruction]
    return ii.pc + branchInstr.branchoffset
  }


}
