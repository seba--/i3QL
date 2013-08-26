package sae.analyses.findbugs.stack

import sae.bytecode.BytecodeDatabase
import sae.Relation
import sae.bytecode.instructions.InstructionInfo
import sae.syntax.sql._
import structure.Stack
import de.tud.cs.st.bat.resolved._
import de.tud.cs.st.bat.resolved.INVOKEVIRTUAL


/**
 * @author Mirko
 * @author Ralf Mitschke
 */
object DMI_INVOKING_TOSTRING_ON_ARRAY
    extends (BytecodeDatabase => Relation[InstructionInfo])
{

    private def isToString(instr: INVOKEVIRTUAL): Boolean =
        (instr.name == "toString") && (instr.methodDescriptor == MethodDescriptor (Nil, ObjectType.String))

    private def isAppendStringBuilder(instr: INVOKEVIRTUAL): Boolean =
        (instr.name == "append") &&
            (instr.declaringClass == ObjectType ("java/lang/StringBuilder")) &&
            (instr.methodDescriptor == MethodDescriptor (ObjectType.Object :: Nil, ObjectType ("java/lang/StringBuilder")))

    private def isAppendStringBuffer(instr: INVOKEVIRTUAL): Boolean =
        (instr.name == "append") &&
            (instr.declaringClass == ObjectType ("java/lang/StringBuffer")) &&
            (instr.methodDescriptor == MethodDescriptor (ObjectType.Object :: Nil, ObjectType ("java/lang/StringBuffer")))

    private def isPrint(instr: INVOKEVIRTUAL): Boolean =
        ((instr.name == "print") || (instr.name == "println")) &&
            (instr.methodDescriptor == MethodDescriptor (ObjectType.Object :: Nil, VoidType))

    private def invokesToString(instruction: Instruction): Boolean =
    {
        if (!instruction.isInstanceOf[INVOKEVIRTUAL])
            return false

        val invokeVirtual = instruction.asInstanceOf[INVOKEVIRTUAL]
        isToString (invokeVirtual) || isAppendStringBuilder (invokeVirtual) || isAppendStringBuffer (invokeVirtual) || isPrint (invokeVirtual)
    }

    private def isArrayType(stack: Stack): Boolean = {
        if (stack.size < 1)
            return false
        val head = stack.get (0)
        head.getDeclaredType.isArrayType
    }

    def apply(database: BytecodeDatabase): Relation[InstructionInfo] = {
        val dataFlow = DataFlow (database)


        SELECT (instruction) FROM dataFlow WHERE
            (info => invokesToString (info.instruction.instruction)) AND
            (info => info.state.s.collection.exists (isArrayType))

    }


}
