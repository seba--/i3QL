package sae.analyses.simple

import sae.bytecode._
import sae.Relation
import structure.{CodeInfo, MethodDeclaration, InheritanceRelation, MethodInfo}
import sae.syntax.sql._
import sae.bytecode.instructions.{InvokeInstruction, InstructionInfo, INVOKEVIRTUAL, INVOKEINTERFACE}

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
    extends (BytecodeDatabase => Relation[(MethodDeclaration, MethodInfo)])
    //extends (BytecodeDatabase => Relation[InvokeInstruction])
{

    val invokeInstruction: InstructionInfo => InvokeInstruction = _.asInstanceOf[InvokeInstruction]

    val invokeTarget: InvokeInstruction => MethodInfo = _.asInstanceOf[MethodInfo]

    val isInvokeInstruction: InstructionInfo => Boolean = _.isInstanceOf[InvokeInstruction]

    val isDynamicInvokeInstruction: InstructionInfo => Boolean = i => i.isInstanceOf[INVOKEVIRTUAL] || i.isInstanceOf[INVOKEINTERFACE]

    val asCallEdge: InvokeInstruction => (MethodDeclaration, MethodInfo) = i => (i.declaringMethod, i)

    val enclosingMethod: CodeInfo => (MethodDeclaration) = _.declaringMethod

    def apply(database: BytecodeDatabase): Relation[(MethodDeclaration, MethodInfo)] = {
    //def apply(database: BytecodeDatabase): Relation[InvokeInstruction] = {
        import database._

        val invokes: Relation[InvokeInstruction] = compile (
            SELECT (invokeInstruction) FROM instructions WHERE (isInvokeInstruction)
        ).forceToSet

        val invokeDynamics = compile (
            SELECT (*) FROM invokes WHERE (isDynamicInvokeInstruction)
        )

        val subTypeMethods = compile (
            SELECT (*) FROM (subTypes, methodDeclarations) WHERE
                (subType === declaringType)
        )

        val dynamicCalls = compile (
            SELECT ((i: InvokeInstruction, x: (InheritanceRelation, MethodDeclaration)) => (i.declaringMethod, x._2)) FROM (invokeDynamics, subTypeMethods) WHERE
                (receiverType === ((_: (InheritanceRelation, MethodDeclaration))._1.superType)) AND
                (((_:InvokeInstruction).name) === ((_: (InheritanceRelation, MethodDeclaration))._2.name)) AND
                (((_:InvokeInstruction).returnType) === ((_: (InheritanceRelation, MethodDeclaration))._2.returnType)) AND
                (((_:InvokeInstruction).parameterTypes) === ((_: (InheritanceRelation, MethodDeclaration))._2.parameterTypes))
        ).asInstanceOf[Relation[(MethodDeclaration, MethodInfo)]]

        compile (
            SELECT (asCallEdge) FROM invokes UNION_ALL (
                SELECT (*) FROM dynamicCalls
                )
        )

    }

}