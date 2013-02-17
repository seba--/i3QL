package sae.analyses.findbugs.stack

import sae.Relation
import sae.bytecode._
import instructions.InstructionInfo
import sae.operators.impl.{WITH_RECURSIVE, RecursiveBase, TransactionalEquiJoinView}
import sae.bytecode.structure._
import structure._
import sae.syntax.sql._
import de.tud.cs.st.bat.resolved.ObjectType

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 17.01.13
 * Time: 16:10
 * To change this template use File | Settings | File Templates.
 */
object DataFlow extends (BytecodeDatabase => Relation[StateInfo])
{
    /**
     * The state before the next instruction is executed is derived
     * by applying the current instruction to the state before the current instruction
     */
    private def nextState(stateInfo: StateInfo, edge: ControlFlowEdge): StateInfo = {
        //println (edge.current.pc + ":" + edge.current.instruction)
        //println (stateInfo.state)
        //println (edge.next.pc + ":" + edge.next.instruction)
        StateInfo (
            edge.next,
            BytecodeTransformer (stateInfo.state, edge.current.pc, edge.current.instruction)
        )
    }

    private def startState(startInstruction: InstructionInfo, codeAttribute: CodeAttribute): StateInfo = {
        StateInfo (
            startInstruction,
            createStartState (codeAttribute)
        )
    }

    private def createStartState(codeAttribute: CodeAttribute): State = {
        val method = codeAttribute.declaringMethod
        var stacks = Stacks (codeAttribute.max_stack, Nil).addStack ()

        var lvs = if (method.isStatic)
                      LocVariables (Array.fill[Item](codeAttribute.max_locals)(Item (ItemType.None, -1, Item.FLAG_IS_NOT_INITIALIZED)))
                  else
                      LocVariables (Array.fill[Item](codeAttribute.max_locals)(Item (ItemType.None, -1, Item.FLAG_IS_NOT_INITIALIZED))).setVar (0, Item (ItemType.SomeRef (ObjectType.Class), -1, Item.FLAG_IS_PARAMETER))

        var i: Int = if (method.isStatic) -1 else 0

        for (t <- method.parameterTypes) {
            i = i + 1
            lvs = lvs.setVar (i, Item (ItemType.fromType (t), -1, Item.FLAG_IS_PARAMETER))
        }

        State (stacks, lvs)
    }

    def apply(database: BytecodeDatabase): Relation[StateInfo] = {
        import database._
        val controlFlow = ControlFlow (database)

        val startInstructions = compile (
            SELECT (*) FROM instructions WHERE (_.pc == 0)
        )

        val startStates =
            RecursiveBase (
                new TransactionalEquiJoinView (
                    startInstructions,
                    codeAttributes,
                    declaringMethod,
                    (_: CodeAttribute).declaringMethod,
                    startState
                ).named ("startStates")
            )


        WITH_RECURSIVE (
            startStates,
            compile (
                SELECT DISTINCT (*) FROM new TransactionalEquiJoinView (
                    startStates.relation,
                    controlFlow,
                    (_: StateInfo).instruction,
                    (_: ControlFlowEdge).current,
                    nextState
                ).named ("dataflow")
            )
        )
    }
}