package sandbox.findbugs.detect

import de.tud.cs.st.bat.resolved._
import sandbox.stackAnalysis.datastructure.{LocalVariables, Stack, State}
import sandbox.findbugs.{BugType, BugLogger}
import sae.bytecode.structure.CodeInfo


/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 07.12.12
 * Time: 14:04
 * To change this template use File | Settings | File Templates.
 */
object SA_FIELD_SELF_COMPARISON extends BugFinder {


  def checkBugs(pc: Int, instr: Instruction): (Int, Instruction, Stack, LocalVariables) => Option[BugType.Value] = {


    //Comparison using IF_XXXX
    if (instructionIsValidBranch(instr)) {
      return checkSelfComparison

    }
    //Comparison using compareTo or equals
    else if (instr.isInstanceOf[INVOKEVIRTUAL]) {
      val invInstr = instr.asInstanceOf[INVOKEVIRTUAL]
      if ((invInstr.name.equals("equals") && invInstr.methodDescriptor.parameterTypes.size == 1 && invInstr.methodDescriptor.returnType.isInstanceOf[BooleanType])
        || (invInstr.name.equals("compareTo") && invInstr.methodDescriptor.parameterTypes.size == 1 && invInstr.methodDescriptor.returnType.isInstanceOf[IntegerType])) {

        return checkSelfComparison
      }
    }
    //Comparison using compareTo and interface invocation
    else if (instr.isInstanceOf[INVOKEINTERFACE]) {
      val invInstr = instr.asInstanceOf[INVOKEINTERFACE]
      if (invInstr.name.equals("compareTo") && invInstr.methodDescriptor.parameterTypes.size == 1 && invInstr.methodDescriptor.returnType.isInstanceOf[IntegerType]) {
        return checkSelfComparison
      }
    }

    return checkNone


  }

  private def checkSelfComparison(pc: Int, instr: Instruction, stack: Stack, loc: LocalVariables): Option[BugType.Value] = {


    if (stack.size < 2)
      return None

    val rhs = stack.get(0)
    val lhs = stack.get(1)

    if (rhs.getPC != -1 && lhs.getPC != -1) {
      if (rhs.isFromField && lhs.isFromField) {
        if (rhs.getFieldName.equals(lhs.getFieldName)) {
          return Some(BugType.SA_FIELD_SELF_COMPARISON)
        }
      }
    }
    return None
  }


  private def instructionIsValidBranch(instr: Instruction): Boolean = {
    return instr.isInstanceOf[ConditionalBranchInstruction] &&
      !instr.isInstanceOf[IFNE] &&
      !instr.isInstanceOf[IFEQ] &&
      !instr.isInstanceOf[IFGT] &&
      !instr.isInstanceOf[IFGE] &&
      !instr.isInstanceOf[IFLE] &&
      !instr.isInstanceOf[IFLT] &&
      !instr.isInstanceOf[IFNONNULL] &&
      !instr.isInstanceOf[IFNULL]
  }
}
