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
    extends (BytecodeDatabase => Relation[InvokeInstruction])
    //extends (BytecodeDatabase => Relation[(InvokeInstruction, InheritanceRelation)])
    //extends (BytecodeDatabase => Relation[(InheritanceRelation, MethodDeclaration)])
    //extends (BytecodeDatabase => Relation[(MethodDeclaration, MethodDeclaration)])
{

    val invokeInstruction: InstructionInfo => InvokeInstruction = _.asInstanceOf[InvokeInstruction]

    val invokeTarget: InvokeInstruction => MethodInfo = _.asInstanceOf[MethodInfo]

    val isInvokeInstruction: InstructionInfo => Boolean = _.isInstanceOf[InvokeInstruction]

    val isDynamicInvokeInstruction: InstructionInfo => Boolean = i => i.isInstanceOf[INVOKEVIRTUAL] || i.isInstanceOf[INVOKEINTERFACE]

    val asCallEdge: InvokeInstruction => (MethodInfo, MethodInfo) = i => (i.declaringMethod, i)

    val enclosingMethod: CodeInfo => (MethodDeclaration) = _.declaringMethod


    //def apply(database: BytecodeDatabase): Relation[(InheritanceRelation, MethodDeclaration)] = {
    //def apply(database: BytecodeDatabase): Relation[(MethodDeclaration, MethodDeclaration)] = {
    //def apply(database: BytecodeDatabase): Relation[(InvokeInstruction, InheritanceRelation)] = {
    def apply(database: BytecodeDatabase): Relation[InvokeInstruction] = {
        import database._

        val invokes: Relation[InvokeInstruction] = compile (
            SELECT (invokeInstruction) FROM instructions WHERE (isInvokeInstruction)
        ).forceToSet

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

        /*
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
        */

        /*
        compile (
            SELECT (declaringMethod, invokeTarget) FROM invokes UNION_ALL (
                SELECT (*) FROM dynamicCalls
                )
        )
        */

        /*
                val implementedMethods = compile (
                    SELECT (enclosingMethod) FROM code
                )

        */

        val subTypeMethods = compile (
            SELECT (*) FROM (subTypes, methodDeclarations) WHERE
                (subType === declaringType)
        )

        /*
        val dynamicCalls = compile (
            SELECT ( (i:InvokeInstruction, x:InheritanceRelation) => i ) FROM (invokeDynamics, subTypes) WHERE
                (receiverType === superType)
        )
        */


        /*
        compile (
            SELECT (asCallEdge) FROM invokes UNION_ALL (
                SELECT (*) FROM dynamicCalls
                )
        )
        */

        // //   (i.declaringMethod, x._2)

        val dynamicCalls = compile (
            SELECT ((i: InvokeInstruction, x: (InheritanceRelation, MethodDeclaration)) => i) FROM (invokeDynamics, subTypeMethods) WHERE
                (receiverType === ((_: (InheritanceRelation, MethodDeclaration))._1.superType))
        )

        dynamicCalls
    }

}