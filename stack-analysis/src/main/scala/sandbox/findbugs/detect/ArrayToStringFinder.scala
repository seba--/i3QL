package sandbox.findbugs.detect

import de.tud.cs.st.bat.resolved._
import sandbox.stackAnalysis.datastructure.{LocVariables, Stack, State}
import de.tud.cs.st.bat.resolved.INVOKEVIRTUAL
import sandbox.findbugs.{BugType, BugLogger}

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 09.12.12
 * Time: 17:16
 * To change this template use File | Settings | File Templates.
 */
object ArrayToStringFinder extends StackBugFinder {

  def notifyInstruction(pc: Int, instructions: Array[Instruction], analysis: Array[State], logger: BugLogger) = {
    val instr = instructions(pc)

    if (instr.isInstanceOf[INVOKEVIRTUAL]) {
      val invInstr = instr.asInstanceOf[INVOKEVIRTUAL]

      lazy val isToString = (invInstr.name equals "toString") && (invInstr.methodDescriptor.equals(MethodDescriptor(Nil, ObjectType.String)))
      lazy val isAppendStringBuilder = (invInstr.name equals "append") &&
        (invInstr.declaringClass.equals(ObjectType("java/lang/StringBuilder"))) &&
        (invInstr.methodDescriptor.equals(MethodDescriptor(ObjectType.Object :: Nil, ObjectType("java/lang/StringBuilder"))))
      lazy val isAppendStringBuffer = (invInstr.name equals "append") &&
        (invInstr.declaringClass.equals(ObjectType("java/lang/StringBuffer"))) &&
        (invInstr.methodDescriptor.equals(MethodDescriptor(ObjectType.Object :: Nil, ObjectType("java/lang/StringBuffer"))))
      lazy val isPrint = ((invInstr.name equals "print") || (invInstr.name equals "println")) &&
        (invInstr.methodDescriptor.equals(MethodDescriptor(ObjectType.Object :: Nil, VoidType)))

      if (isToString || isAppendStringBuilder || isAppendStringBuffer || isPrint)
        checkForBugs(pc, instructions, analysis, logger, checkArrayToString)
    }
  }

  private def checkArrayToString(pc: Int, instructions: Array[Instruction], stack: Stack, loc: LocVariables): Option[BugType.Value] = {
    if (stack.size < 1)
      return None

    val op = stack.get(0)
    if (op.getDeclaredType.isArrayType) {
      return Some(BugType.DMI_INVOKING_TOSTRING_ON_ARRAY)
    }
    return None
  }

}
