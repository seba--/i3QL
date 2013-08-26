package sae.analyses.findbugs.stack

import sae.bytecode.BytecodeDatabase
import sae.Relation
import sae.bytecode.instructions.{POP2, POP, InstructionInfo}
import sae.syntax.sql._
import structure.Stack
import de.tud.cs.st.bat.resolved.{VoidType, MethodDescriptor, INVOKEVIRTUAL, Instruction}


/**
 * @author Mirko
 * @author Ralf Mitschke
 */
object MWN_MISMATCHED_NOTIFY
    extends (BytecodeDatabase => Relation[InstructionInfo])
{

    private def isNotify(instruction: Instruction): Boolean = {
        if (!instruction.isInstanceOf[INVOKEVIRTUAL])
            return false

        val instr = instruction.asInstanceOf[INVOKEVIRTUAL]
        (instr.name == "notify" || instr.name == "notifyAll") && (instr.methodDescriptor == MethodDescriptor (Nil, VoidType))
    }
    def apply(database: BytecodeDatabase): Relation[InstructionInfo] = {
        val dataFlow = DataFlow(database)


        SELECT (instruction) FROM dataFlow WHERE
            (info => isNotify(info.instruction.instruction))

        //SELECT (*) FROM dataFlow WHERE  (_.instruction.declaringMethod.name == "<clinit>")
    }


}
