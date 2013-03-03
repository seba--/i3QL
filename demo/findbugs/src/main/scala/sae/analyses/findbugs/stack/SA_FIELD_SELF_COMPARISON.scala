package sae.analyses.findbugs.stack

import sae.bytecode.BytecodeDatabase
import sae.Relation
import sae.bytecode.instructions.{POP2, POP, InstructionInfo}
import sae.syntax.sql._
import structure.Stack
import de.tud.cs.st.bat.resolved._
import de.tud.cs.st.bat.resolved.IFLT
import de.tud.cs.st.bat.resolved.INVOKEVIRTUAL
import de.tud.cs.st.bat.resolved.IFNE
import de.tud.cs.st.bat.resolved.IFNONNULL
import de.tud.cs.st.bat.resolved.IFGT
import de.tud.cs.st.bat.resolved.IFNULL
import de.tud.cs.st.bat.resolved.INVOKEINTERFACE
import de.tud.cs.st.bat.resolved.IFGE
import de.tud.cs.st.bat.resolved.IFEQ
import de.tud.cs.st.bat.resolved.IFLE


/**
 * @author Mirko
 * @author Ralf Mitschke
 */
object SA_FIELD_SELF_COMPARISON
    extends (BytecodeDatabase => Relation[InstructionInfo])
{


    private def isBranchInstruction(instr: Instruction): Boolean = {
        instr.isInstanceOf[ConditionalBranchInstruction] &&
            !instr.isInstanceOf[IFNE] &&
            !instr.isInstanceOf[IFEQ] &&
            !instr.isInstanceOf[IFGT] &&
            !instr.isInstanceOf[IFGE] &&
            !instr.isInstanceOf[IFLE] &&
            !instr.isInstanceOf[IFLT] &&
            !instr.isInstanceOf[IFNONNULL] &&
            !instr.isInstanceOf[IFNULL]
    }

    //Comparison using compareTo or equals
    private def isCompareCall(instr: Instruction): Boolean = {
        if (instr.isInstanceOf[INVOKEVIRTUAL]) {
            val invoke = instr.asInstanceOf[INVOKEVIRTUAL]
            return (invoke.name == "equals" && invoke.methodDescriptor.parameterTypes.size == 1 && invoke.methodDescriptor.returnType.isInstanceOf[BooleanType]) ||
                (invoke.name == "compareTo" && invoke.methodDescriptor.parameterTypes.size == 1 && invoke.methodDescriptor.returnType.isInstanceOf[IntegerType])
        }

        if (instr.isInstanceOf[INVOKEINTERFACE]) {
            val invoke = instr.asInstanceOf[INVOKEINTERFACE]
            return invoke.name == "compareTo" && invoke.methodDescriptor.parameterTypes.size == 1 && invoke.methodDescriptor.returnType.isInstanceOf[IntegerType]
        }

        false
    }

    private def makesComparison(instruction: Instruction): Boolean =
        isBranchInstruction (instruction) || isCompareCall (instruction)



    def apply(database: BytecodeDatabase): Relation[InstructionInfo] = {
        val dataFlow = DataFlow(database)


        SELECT (instruction) FROM dataFlow WHERE
            (info => makesComparison(info.instruction.instruction)) AND
            (info => info.state.s.collection.exists(isSelfComparison))

    }



    private def isSelfComparison(stack: Stack): Boolean = {
        if (stack.size < 2)
            return false

        val rhs = stack.get (0)
        val lhs = stack.get (1)

        if (rhs.getPC != -1 && lhs.getPC != -1) {
            if (rhs.isFromField && lhs.isFromField) {
                if (rhs.getFieldName.equals(lhs.getFieldName)) {
                    return true
                }
            }
        }

        false
    }

}
