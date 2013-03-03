package sae.analyses.findbugs.stack

import sae.bytecode.BytecodeDatabase
import sae.Relation
import sae.bytecode.instructions.InstructionInfo
import sae.syntax.sql._
import de.tud.cs.st.bat.resolved._
import structure.{ItemType, Stack}
import de.tud.cs.st.bat.resolved.INVOKESPECIAL
import de.tud.cs.st.bat.resolved.INVOKEDYNAMIC
import de.tud.cs.st.bat.resolved.INVOKESTATIC
import de.tud.cs.st.bat.resolved.INVOKEINTERFACE
import de.tud.cs.st.bat.resolved.INVOKEVIRTUAL
import de.tud.cs.st.bat.resolved.IF_ACMPEQ
import de.tud.cs.st.bat.resolved.IF_ACMPNE


/**
 * @author Mirko
 * @author Ralf Mitschke
 */
object RC_REF_COMPARISON
    extends (BytecodeDatabase => Relation[InstructionInfo])
{


    private val suspiciousTypes: List[Type] =
    // ObjectType ("java/lang/Boolean") :: // marks a different bug pattern
        ObjectType ("java/lang/Byte") ::
            ObjectType ("java/lang/Character") ::
            ObjectType ("java/lang/Double") ::
            ObjectType ("java/lang/Float") ::
            ObjectType ("java/lang/Integer") ::
            ObjectType ("java/lang/Long") ::
            ObjectType ("java/lang/Short") ::
            Nil


    private def isCompareInstruction(instruction: Instruction): Boolean =
        instruction.isInstanceOf[IF_ACMPEQ] || instruction.isInstanceOf[IF_ACMPNE]


    //Returns MethodName, DeclaringClass, MethodDescriptor, isStatic
    private def getMethodDescriptor(instr: Instruction): (String, ReferenceType, MethodDescriptor, Boolean) = {

        instr match {
            case INVOKEDYNAMIC (s, m) => {
                (s, null, m, false)
            }
            case INVOKEINTERFACE (classReference, name, method) => {
                (name, classReference, method, false)
            }
            case INVOKESPECIAL (classReference, name, method) => {
                (name, classReference, method, false)
            }
            case INVOKESTATIC (classReference, name, method) => {
                (name, classReference, method, true)
            }
            case INVOKEVIRTUAL (classReference, name, method) => {
                (name, classReference, method, false)
            }
            case x => throw new IllegalArgumentException (x + ": The invoke instruction is unknown.")
        }

    }

    private def isCompareMethodCall(instruction: Instruction): Boolean = {
        if (!instruction.isInstanceOf[MethodInvocationInstruction])
            return false
        val (methodName, declaringClass, methodDesc, isStatic) = getMethodDescriptor (instruction)

        (methodName == "assertSame" && methodDesc == MethodDescriptor ((ObjectType.Object :: ObjectType.Object :: Nil), VoidType)) ||
            (!isStatic && methodName == "equals" && methodDesc == MethodDescriptor ((ObjectType.Object :: Nil), BooleanType)) ||
            (isStatic && methodName == "assertEquals" && methodDesc == MethodDescriptor ((ObjectType.Object :: ObjectType.Object :: Nil), VoidType)) && declaringClass != ObjectType ("org/testng/Assert") ||
            (isStatic && methodName == "equal" && methodDesc == MethodDescriptor ((ObjectType.Object :: ObjectType.Object :: Nil), VoidType) && declaringClass != ObjectType ("com/google/common/base/Objects"))
    }


    private def makesComparison(instruction: Instruction): Boolean =
    {
        isCompareInstruction (instruction) || isCompareMethodCall (instruction)
    }


    def apply(database: BytecodeDatabase): Relation[InstructionInfo] = {
        val dataFlow = DataFlow (database)


        SELECT (instruction) FROM dataFlow WHERE
            (info => makesComparison (info.instruction.instruction)) AND
            (info => info.state.s.collection.exists (isSuspiciousRefComparison))

        //SELECT (*) FROM dataFlow WHERE  (_.instruction.declaringMethod.name == "<clinit>")
    }

    private def isSuspiciousRefComparison(stack: Stack): Boolean =
    {
        if (stack.size < 2)
            return false

        val rhs = stack (0)
        val lhs = stack (1)

        //Do nothing if comparison with null.
        if (rhs.isCouldBeNull || lhs.isCouldBeNull) {
            return false
        }

        if (rhs.getDeclaredType.isReferenceType && lhs.getDeclaredType.isReferenceType &&
            isSuspicious (rhs.getDeclaredType) || isSuspicious (lhs.getDeclaredType)
        )
        {
            return true
        }
        false
    }

    private def isSuspicious(itemType: ItemType): Boolean = {
        if (itemType.isInstanceOf[ItemType.SomeRef]) {
            val someRef = itemType.asInstanceOf[ItemType.SomeRef]
            return suspiciousTypes.contains (someRef.refType)
        }
        false
    }


}
