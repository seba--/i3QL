package sae.analyses.simple

import sae.bytecode._
import sae.Relation
import structure.{MethodDeclaration, InheritanceRelation, MethodInfo}
import sae.syntax.sql._
import sae.bytecode.instructions.{InvokeInstruction, InstructionInfo, INVOKEVIRTUAL, INVOKEINTERFACE}
import sae.operators.impl.ThreeWayJoinView

/**
 * Compute the call graph via Class Hierarchy Analysis (CHA).
 * This is the simplest an least precise call graph.
 * It just takes all call edges to subclasses into account
 *
 * See Tip & Palsberg - "Scalable Propagation-Based Call Graph Construction Algorithms"
 * for a good explanation and overview
 *
 * @author Ralf Mitschke
 */
object CHA
    extends (BytecodeDatabase => Relation[(MethodInfo, MethodInfo)])
{

    val invokeInstruction: InstructionInfo => InvokeInstruction = _.asInstanceOf[InvokeInstruction]

    val invokeTarget: InvokeInstruction => MethodInfo = _.asInstanceOf[MethodInfo]

    val isInvokeInstruction: InstructionInfo => Boolean = _.isInstanceOf[InvokeInstruction]

    val isDynamicInvokeInstruction: InstructionInfo => Boolean = i => i.isInstanceOf[INVOKEVIRTUAL] || i.isInstanceOf[INVOKEINTERFACE]

    val asCallEdge : InvokeInstruction => (MethodInfo, MethodInfo) = i => (i.declaringMethod, i)

    def apply(database: BytecodeDatabase): Relation[(MethodInfo, MethodInfo)] = {
        import database._

        val invokes = compile (
            SELECT (invokeInstruction) FROM instructions WHERE (isInvokeInstruction)
        )

        val invokeDynamics = compile (
            SELECT (*) FROM invokes WHERE (isDynamicInvokeInstruction)
        )

        /*
        val dynamicCalls = compile (
            SELECT (*) FROM (invokeDynamics, subTypes, methodDeclarations) WHERE
                (receiverType == superType) AND
                (subType == declaringType)
        )
        */


        val dynamicCalls =
            new ThreeWayJoinView (
                invokeDynamics, // left
                subTypes, // middle
                methodDeclarations, // right
                receiverType, // left key
                superType, // middle to left key
                subType, // middle to right key
                declaringType, // right key
                (invoke: InvokeInstruction, inheritance: InheritanceRelation, declaredMethod: MethodDeclaration) =>
                    (invoke.declaringMethod, declaringMethod)
            ).asInstanceOf[Relation[(MethodInfo, MethodInfo)]]

        /*
        compile (
            SELECT (declaringMethod, invokeTarget) FROM invokes UNION_ALL (
                SELECT (*) FROM dynamicCalls
                )
        )
        */


        compile (
            SELECT (asCallEdge) FROM invokes UNION_ALL (
                SELECT (*) FROM dynamicCalls
                )
        )
    }

}