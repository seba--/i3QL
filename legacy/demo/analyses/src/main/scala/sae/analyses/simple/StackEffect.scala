package sae.analyses.simple

import sae.bytecode._
import instructions.InstructionInfo
import sae.Relation
import sae.operators.impl.TransactionalFixPointRecursionView

/**
 *
 * @author Ralf Mitschke
 *
 */
object StackEffect
{

    type Stack = AnyRef

    type PredecessorStack = (InstructionInfo, Stack)


    def emptyStack: Stack = null

    def anchor: ((InstructionInfo, InstructionInfo)) => Option[PredecessorStack] = (succ: (InstructionInfo, InstructionInfo)) => {
        val instr = succ._1
        if (instr.pc == 1)
            Some((instr, emptyStack))
        else
            None
    }

    def prevInstruction: ((InstructionInfo, InstructionInfo)) => InstructionInfo = _._1

    def stackInstruction: PredecessorStack => InstructionInfo = _._1

    def applyInstructionToStack(instruction: InstructionInfo, stack: Stack): Stack = {
        null
    }

    def recursionStep: ((InstructionInfo, InstructionInfo), PredecessorStack) => PredecessorStack =
        (sucessorEdge: (InstructionInfo, InstructionInfo), prevInstructionStack: PredecessorStack) => {
            val stack = prevInstructionStack._2
            val nextStack = applyInstructionToStack(sucessorEdge._1, stack)
            (sucessorEdge._2, nextStack)
        }

    def apply(database: BytecodeDatabase): Relation[PredecessorStack] = {
        val successorInstructions: Relation[(InstructionInfo, InstructionInfo)] = null

       /* new TransactionalFixPointRecursionView(
            successorInstructions,
            anchor,
            prevInstruction,
            stackInstruction,
            recursionStep
        )*/
      return null

    }

}