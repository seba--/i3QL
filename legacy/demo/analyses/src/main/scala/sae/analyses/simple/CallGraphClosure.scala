package sae.analyses.simple

import sae.bytecode._
import sae.Relation
import sae.bytecode.structure.MethodInfo
import sae.operators.impl.CyclicTransitiveClosureView
import sae.syntax.sql._
import sae.bytecode.instructions.{InvokeInstruction, InstructionInfo}

/**
 * 
 * @author Ralf Mitschke
 *
 * Calls graph via Class Hierarchy Analysis.
 * This is the simplest an least precise call graph.
 * It just takes all call edges.
 *
 * See Tip & Palsberg for a good overview
 */
object CallGraphClosure
    extends  (BytecodeDatabase => Relation[(MethodInfo, MethodInfo)])
{

    def invokeInstruction : InstructionInfo => InvokeInstruction = _.asInstanceOf[InvokeInstruction]

    def isInvokeInstruction : InstructionInfo => Boolean = _.isInstanceOf[InvokeInstruction]

    def apply(database:BytecodeDatabase) : Relation[(MethodInfo, MethodInfo)] = {

        val invokes = compile(
            SELECT (invokeInstruction) FROM database.instructions WHERE (isInvokeInstruction)
        )

        new CyclicTransitiveClosureView(invokes, declaringMethod, referencedMethod)
    }

}