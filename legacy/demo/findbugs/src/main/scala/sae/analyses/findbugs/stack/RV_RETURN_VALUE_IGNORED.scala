package sae.analyses.findbugs.stack

import sae.bytecode.BytecodeDatabase
import sae.Relation
import sae.bytecode.instructions.{POP2, POP, InstructionInfo}
import sae.syntax.sql._
import sae.operators.impl.TransactionalEquiJoinView
import structure.{StateInfo, Stack}


/**
 * @author Mirko
 * @author Ralf Mitschke
 */
object RV_RETURN_VALUE_IGNORED
    extends (BytecodeDatabase => Relation[InstructionInfo])
{

    def isPopInstruction : InstructionInfo => Boolean = i => i.isInstanceOf[POP] || i.isInstanceOf[POP2]

    private def returnValueIgnored(stack: Stack): Boolean = {
        stack.size > 0 && (stack.get (0).isReturnValue || stack.get (0).isCreatedByNew)
    }

    def apply(database: BytecodeDatabase): Relation[InstructionInfo] = {
        val dataFlow = DataFlow(database)


        SELECT (instruction) FROM dataFlow WHERE
            (info => isPopInstruction(info.instruction)) AND
            (info => info.state.s.collection.exists(returnValueIgnored))

        //SELECT (*) FROM dataFlow WHERE  (_.instruction.declaringMethod.name == "<clinit>")
    }


}
