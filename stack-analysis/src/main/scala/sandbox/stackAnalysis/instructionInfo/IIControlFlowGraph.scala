package sandbox.stackAnalysis.instructionInfo

import sae.bytecode.BytecodeDatabase
import sae.Relation
import sae.syntax.sql._
import sae.bytecode.instructions.InstructionInfo
import de.tud.cs.st.bat.resolved._
import sae.bytecode.structure.{MethodDeclaration, CodeAttribute}
import sae.operators.impl.TransactionalEquiJoinView

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

    val instructionMethodAndIndex: InstructionInfo => (MethodDeclaration, Int) =
      (i: InstructionInfo) => (i.declaringMethod, i.sequenceIndex)

    val instructionMethodAndPrevIndex: InstructionInfo => (MethodDeclaration, Int) =
      (i: InstructionInfo) => (i.declaringMethod, i.sequenceIndex - 1)

    import sae.syntax.RelationalAlgebraSyntax._
    //Relation that stores all possible control flow edges as InstructionPairs.
    val relEdges: Relation[InstructionPair] =
    //control flow for normal instructions and unconditional branches assuming no branch
      new TransactionalEquiJoinView(
        relNormal,
        bcd.instructions,
        instructionMethodAndIndex,
        instructionMethodAndPrevIndex,
        ((current: InstructionInfo, next: InstructionInfo) => InstructionPair(current, next))
        //control flow for unconditional branches
      ) ⊎ new TransactionalEquiJoinView[InstructionInfo, InstructionInfo, InstructionPair, (MethodDeclaration, Int)](
        relUnconditionalBranchs,
        bcd.instructions,
        (i: InstructionInfo) => (i.declaringMethod, getUnconditionalNextPC(i)),
        (i: InstructionInfo) => (i.declaringMethod, i.pc),
        ((current: InstructionInfo, next: InstructionInfo) => InstructionPair(current, next))
        //control flow for conditional branches assuming branch
      ) ⊎ new TransactionalEquiJoinView[InstructionInfo, InstructionInfo, InstructionPair, (MethodDeclaration, Int)](
        relConditionalBranchs,
        bcd.instructions,
        (i: InstructionInfo) => (i.declaringMethod, getConditionalNextPCAssumingBranch(i)),
        (i: InstructionInfo) => (i.declaringMethod, i.pc),
        ((current: InstructionInfo, next: InstructionInfo) => InstructionPair(current, next))
      )

    /*

    compile(
      //control flow for normal instructions and unconditional branches assuming no branch
      (SELECT ((current: InstructionInfo, next: InstructionInfo) => InstructionPair(current, next)) FROM
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
          (bcd.instructions) WHERE (((_: InstructionInfo).pc) === 0))*/

    */

    //Relation that computes the real ControlFlowEdges from instruction pairs.
    val result: Relation[ControlFlowEdge] = SELECT((instrPair: InstructionPair, attribute: CodeAttribute) => getEdge(instrPair.current, instrPair.next, attribute)) FROM(relEdges, bcd.codeAttributes) WHERE (((_: InstructionPair).getDeclaringMethod) === ((_: CodeAttribute).declaringMethod))

    return result


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

    if (current.pc == 0)
      return AnchorControlFlowEdge(current, next, attribute)
    else
      return DefaultControlFlowEdge(current, next)

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
