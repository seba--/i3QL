package sandbox.stackAnalysis.instructionInfo

import sae.bytecode.BytecodeDatabase
import sae.Relation
import sae.syntax.sql._
import sae.bytecode.instructions.InstructionInfo
import de.tud.cs.st.bat.resolved._
import sae.bytecode.structure.{MethodDeclaration, CodeAttribute}

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 16.12.12
 * Time: 13:18
 * To change this template use File | Settings | File Templates.
 */
object IIControlFlowGraph extends (BytecodeDatabase => Relation[ControlFlowEdge]) {

  private case class InstructionPair(current: InstructionInfo, next: InstructionInfo) {
    def getDeclaringMethod: MethodDeclaration = next.declaringMethod
  }


  def apply(bcd: BytecodeDatabase): Relation[ControlFlowEdge] = {

    //Relation that stores all "normal" instructions, i.e. all instructions which next instructions is at the next sequence index (e.g. no jumps).
    val relNormal = computeNormalInstructions(bcd)
    //Relation that stores all unconditional branches (GOTO etc.)
    val relUnconditionalBranchs = computeUnconditionalBranchs(bcd)
    //Relation that stores all conditional branches (IFEQ etc.)
    val relConditionalBranchs = computeConditionalBranchs(bcd)

    //Relation that stores all possible control flow edges as InstructionPairs.
    val relEdges = compile(
      //control flow for normal instructions and unconditional branches assuming no branch
      (SELECT((current: InstructionInfo, next: InstructionInfo) => InstructionPair(current, next)) FROM
        (relNormal, bcd.instructions) WHERE
        (((_: InstructionInfo).declaringMethod) === ((_: InstructionInfo).declaringMethod)) AND
        (((_: InstructionInfo).sequenceIndex) === ((_: InstructionInfo).sequenceIndex - 1))) UNION_ALL

        //control flow for unconditional branches
        (SELECT((current: InstructionInfo, next: InstructionInfo) => InstructionPair(current, next)) FROM
          (relUnconditionalBranchs, bcd.instructions) WHERE
          (((_: InstructionInfo).declaringMethod) === ((_: InstructionInfo).declaringMethod)) AND
          ((getUnconditionalNextPC(_: InstructionInfo)) === ((_: InstructionInfo).pc))) UNION_ALL

        //control flow for conditional branches assuming branch
        (SELECT((current: InstructionInfo, next: InstructionInfo) => InstructionPair(current, next)) FROM
          (relConditionalBranchs, bcd.instructions) WHERE
          (((_: InstructionInfo).declaringMethod) === ((_: InstructionInfo).declaringMethod)) AND
          ((getConditionalNextPCAssumingBranch(_: InstructionInfo)) === ((_: InstructionInfo).pc))))

           //UNION_ALL
        //control flow from starting edges
        /*(SELECT((next: InstructionInfo) => InstructionPair(null, next)) FROM
          (bcd.instructions) WHERE (((_: InstructionInfo).pc) === 0)))*/

    //Relation that computes the real ControlFlowEdges from instruction pairs.
    return compile(
      SELECT((instrPair: InstructionPair, attribute: CodeAttribute) => getEdge(instrPair.current, instrPair.next, attribute)) FROM(relEdges, bcd.codeAttributes) WHERE (((_: InstructionPair).getDeclaringMethod) === ((_: CodeAttribute).declaringMethod))
    )
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


  private def getEdge(current: InstructionInfo, next: InstructionInfo, attribute: CodeAttribute): ControlFlowEdge = {
    val res = ControlFlowEdge(current, next)
    println("CFGEdge: " + res)
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
