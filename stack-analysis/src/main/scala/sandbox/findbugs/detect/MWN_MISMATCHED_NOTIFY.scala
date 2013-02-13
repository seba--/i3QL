package sandbox.findbugs.detect

import de.tud.cs.st.bat.resolved.{INVOKEVIRTUAL, ObjectType}
import sandbox.stackAnalysis.datastructure.{LocVariables, Stack, State}
import sandbox.findbugs.{BugType, BugLogger}
import sae.bytecode.structure.CodeInfo

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 17.12.12
 * Time: 11:07
 * To change this template use File | Settings | File Templates.
 */
object MWN_MISMATCHED_NOTIFY extends StackBugFinder {

  def notifyInstruction(pc: Int, codeInfo: CodeInfo, analysis: Array[State], logger: BugLogger) = {

    val instr = codeInfo.code.instructions(pc)

    if (instr.isInstanceOf[INVOKEVIRTUAL]) {
      val invInstr = instr.asInstanceOf[INVOKEVIRTUAL]

      if (invInstr.declaringClass.equals(ObjectType.Object) &&
        (invInstr.name.equals("notify") || invInstr.name.equals("notifyAll"))) {
        checkForBugs(pc, codeInfo, analysis, logger, checkMismatchedNotify)
      }
    }

  }

  //TODO: implement
  private def checkMismatchedNotify(pc: Int, codeInfo: CodeInfo, stack: Stack, loc: LocVariables): Option[BugType.Value] = {
    None
  }
}
