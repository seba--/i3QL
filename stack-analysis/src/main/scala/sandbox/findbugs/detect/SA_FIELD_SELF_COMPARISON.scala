package sandbox.findbugs.detect

import de.tud.cs.st.bat.resolved._
import sandbox.stackAnalysis.datastructure.{LocVariables, Stack, State}
import sandbox.findbugs.{BugType, BugLogger}
import sae.bytecode.structure.CodeInfo


/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 07.12.12
 * Time: 14:04
 * To change this template use File | Settings | File Templates.
 */
object SA_FIELD_SELF_COMPARISON extends StackBugFinder {


  def notifyInstruction(pc: Int, codeInfo: CodeInfo, analysis: Array[State], logger: BugLogger) = {

    val instr = codeInfo.code.instructions(pc)
    //Comparison using IF_XXXX
    if (instructionIsValidBranch(instr)) {

      checkForBugs(pc, codeInfo, analysis, logger, checkSelfComparison)

    }
    //Comparison using compareTo or equals
    else if (instr.isInstanceOf[INVOKEVIRTUAL]) {
      val invInstr = instr.asInstanceOf[INVOKEVIRTUAL]
      if ((invInstr.name.equals("equals") && invInstr.methodDescriptor.parameterTypes.size == 1 && invInstr.methodDescriptor.returnType.isInstanceOf[BooleanType])
        || (invInstr.name.equals("compareTo") && invInstr.methodDescriptor.parameterTypes.size == 1 && invInstr.methodDescriptor.returnType.isInstanceOf[IntegerType])) {

        checkForBugs(pc, codeInfo, analysis, logger, checkSelfComparison)

      }
    }
    //Comparison using compareTo and interface invocation
    else if (instr.isInstanceOf[INVOKEINTERFACE]) {
      val invInstr = instr.asInstanceOf[INVOKEINTERFACE]
      if (invInstr.name.equals("compareTo") && invInstr.methodDescriptor.parameterTypes.size == 1 && invInstr.methodDescriptor.returnType.isInstanceOf[IntegerType]) {

        checkForBugs(pc, codeInfo, analysis, logger, checkSelfComparison)
      }
    }

  }

  private def checkSelfComparison(pc: Int, codeInfo: CodeInfo, stack: Stack, loc: LocVariables): Option[BugType.Value] = {
    val instructions = codeInfo.code.instructions

    if (stack.size < 2)
      return None

    val rhs = stack.get(0)
    val lhs = stack.get(1)

    if (rhs.getPC != -1 && lhs.getPC != -1) {

      val rInstr = instructions(rhs.getPC)
      val lInstr = instructions(lhs.getPC)

      if ((rInstr.isInstanceOf[GETFIELD] && lInstr.isInstanceOf[GETFIELD])
        || (instructions(rhs.getPC).isInstanceOf[GETSTATIC] && instructions(lhs.getPC).isInstanceOf[GETSTATIC])) {
        if (rInstr.equals(lInstr)) {
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
