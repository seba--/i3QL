package sae.analyses.findbugs.stack

import sae.bytecode.BytecodeDatabase
import sae.Relation
import sae.syntax.sql._
import sae.bytecode.instructions._
import sae.bytecode.structure.MethodDeclaration
import sae.operators.impl.{EquiJoinView, TransactionalEquiJoinView}
import structure.ControlFlowEdge

/**
 * @author Mirko
 * @author Ralf Mitschke
 */
object ControlFlow extends (BytecodeDatabase => Relation[ControlFlowEdge])
{
    private def gotoInstruction: InstructionInfo => GotoBranchInstructionInfo = _.asInstanceOf[GotoBranchInstructionInfo]

    private def branchTarget: InstructionInfo => IfBranchInstructionInfo = _.asInstanceOf[IfBranchInstructionInfo]

    private def switchInstruction: InstructionInfo => SwitchInstructionInfo = _.asInstanceOf[SwitchInstructionInfo]

    private def switchTarget: (SwitchInstructionInfo, Some[Int]) => (SwitchInstructionInfo, Int) =
        (switch: SwitchInstructionInfo, jumpOffset: Some[Int]) => (switch, switch.pc + jumpOffset.get)

    private val methodAndIndex: InstructionInfo => (MethodDeclaration, Int) =
        (i: InstructionInfo) => (i.declaringMethod, i.pc)

    private val methodAndSequenceIndex: InstructionInfo => (MethodDeclaration, Int) =
        (i: InstructionInfo) => (i.declaringMethod, i.sequenceIndex)


    private val methodAndNextSequenceIndex: InstructionInfo => (MethodDeclaration, Int) =
        (i: InstructionInfo) => (i.declaringMethod, i.sequenceIndex + 1)

    private val methodUnconditionalBranchIndex: GotoBranchInstructionInfo => (MethodDeclaration, Int) =
        (i: GotoBranchInstructionInfo) => (i.declaringMethod, i.pc + i.branchOffset)

    private val methodConditionalBranchIndex: IfBranchInstructionInfo => (MethodDeclaration, Int) =
        (i: IfBranchInstructionInfo) => (i.declaringMethod, i.pc + i.branchOffset)


    private val methodSwitchIndex: ((SwitchInstructionInfo, Int)) => (MethodDeclaration, Int) =
        (e: (SwitchInstructionInfo, Int)) => (e._1.declaringMethod, e._1.pc + e._2)


    private def controlFlowEdge: (InstructionInfo, InstructionInfo) => ControlFlowEdge =
        (current: InstructionInfo, next: InstructionInfo) => ControlFlowEdge (current, next)

    //println(current + " => " + next);


    def apply(database: BytecodeDatabase): Relation[ControlFlowEdge] = {
        import database._

        //Relation that stores all unconditional branch targets (GOTO etc.)
        val unconditionalBranches = compile (
            SELECT (gotoInstruction) FROM (instructions) WHERE (_.isInstanceOf[GotoBranchInstructionInfo])
        )

        //Relation that stores all conditional branches (IFEQ etc.)
        val conditionalBranches = compile (
            SELECT (branchTarget) FROM (instructions) WHERE (_.isInstanceOf[IfBranchInstructionInfo])
        )

        //Relation that stores switch instructions
        val switchInstructions = compile (
            SELECT (switchInstruction) FROM (instructions) WHERE (_.isInstanceOf[SwitchInstructionInfo])
        )

        val switchJumpTargets = compile (
            SELECT (switchTarget) FROM (switchInstructions, ((_: SwitchInstructionInfo).jumpOffsets.map (Some (_))) IN switchInstructions)
        )

        //Relation that stores all "normal" instructions, i.e. all instructions which next instructions is at the next sequence index (e.g. no jumps).
        // TODO this is a simplification that does not take exception handling into account
        val fallThroughInstructions = compile (
            SELECT (*) FROM (instructions) WHERE
                (!_.isInstanceOf[SwitchInstructionInfo]) AND
                (!_.isInstanceOf[GotoBranchInstructionInfo]) AND
                (!_.isInstanceOf[ReturnInstructionInfo]) AND
                (!_.isInstanceOf[ATHROW])
        )


        //Relation that stores all possible control flow edges as InstructionPairs.

        import sae.syntax.RelationalAlgebraSyntax._
        val controlFlowEdges =
        //control flow for normal instructions and unconditional branches assuming no branch
            new TransactionalEquiJoinView (
                fallThroughInstructions,
                instructions,
                methodAndNextSequenceIndex,
                methodAndSequenceIndex,
                controlFlowEdge
                //control flow for unconditional branches
            ).named ("fallThroughInstructions") ⊎ new TransactionalEquiJoinView (
                unconditionalBranches,
                instructions,
                methodUnconditionalBranchIndex,
                methodAndIndex,
                controlFlowEdge
                //control flow for conditional branches assuming branch
            ).named ("unconditionalBranches") ⊎ new TransactionalEquiJoinView (
                conditionalBranches,
                instructions,
                methodConditionalBranchIndex,
                methodAndIndex,
                controlFlowEdge
            ).named ("conditionalBranches") ⊎ new TransactionalEquiJoinView (
                switchJumpTargets,
                instructions,
                methodSwitchIndex,
                methodAndIndex,
                (current: (SwitchInstructionInfo, Int), next: InstructionInfo) => ControlFlowEdge (current._1, next)
            ).named ("switchJumpTargets")

        controlFlowEdges
    }


    def materialized(database: BytecodeDatabase): Relation[ControlFlowEdge] = {
        import database._

        //Relation that stores all unconditional branch targets (GOTO etc.)
        val unconditionalBranches = compile (
            SELECT (gotoInstruction) FROM (instructions) WHERE (_.isInstanceOf[GotoBranchInstructionInfo])
        )

        //Relation that stores all conditional branches (IFEQ etc.)
        val conditionalBranches = compile (
            SELECT (branchTarget) FROM (instructions) WHERE (_.isInstanceOf[IfBranchInstructionInfo])
        )

        //Relation that stores switch instructions
        val switchInstructions = compile (
            SELECT (switchInstruction) FROM (instructions) WHERE (_.isInstanceOf[SwitchInstructionInfo])
        )

        val switchJumpTargets = compile (
            SELECT (switchTarget) FROM (switchInstructions, ((_: SwitchInstructionInfo).jumpOffsets.map (Some (_))) IN switchInstructions)
        )

        //Relation that stores all "normal" instructions, i.e. all instructions which next instructions is at the next sequence index (e.g. no jumps).
        // TODO this is a simplification that does not take exception handling into account
        val fallThroughInstructions = compile (
            SELECT (*) FROM (instructions) WHERE
                (!_.isInstanceOf[SwitchInstructionInfo]) AND
                (!_.isInstanceOf[GotoBranchInstructionInfo]) AND
                (!_.isInstanceOf[ReturnInstructionInfo]) AND
                (!_.isInstanceOf[ATHROW])
        )


        //Relation that stores all possible control flow edges as InstructionPairs.

        import sae.syntax.RelationalAlgebraSyntax._
        val controlFlowEdges =
        //control flow for normal instructions and unconditional branches assuming no branch
            new EquiJoinView (
                fallThroughInstructions,
                instructions,
                methodAndNextSequenceIndex,
                methodAndSequenceIndex,
                controlFlowEdge
                //control flow for unconditional branches
            ).named ("fallThroughInstructions") ⊎ new EquiJoinView (
                unconditionalBranches,
                instructions,
                methodUnconditionalBranchIndex,
                methodAndIndex,
                controlFlowEdge
                //control flow for conditional branches assuming branch
            ).named ("unconditionalBranches") ⊎ new EquiJoinView (
                conditionalBranches,
                instructions,
                methodConditionalBranchIndex,
                methodAndIndex,
                controlFlowEdge
            ).named ("conditionalBranches") ⊎ new EquiJoinView (
                switchJumpTargets,
                instructions,
                methodSwitchIndex,
                methodAndIndex,
                (current: (SwitchInstructionInfo, Int), next: InstructionInfo) => ControlFlowEdge (current._1, next)
            ).named ("switchJumpTargets")

        controlFlowEdges
    }

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
    /*
  val result: Relation[ControlFlowEdge] = SELECT ((instrPair: InstructionPair, attribute: CodeAttribute) => getEdge (instrPair.current, instrPair.next, attribute)) FROM (relEdges, database.codeAttributes) WHERE (((_: InstructionPair).getDeclaringMethod) === ((_: CodeAttribute).declaringMethod))

  return result
    */


}
