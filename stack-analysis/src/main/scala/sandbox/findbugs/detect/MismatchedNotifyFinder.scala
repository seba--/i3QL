package sandbox.findbugs.detect

import de.tud.cs.st.bat.resolved.{INVOKEVIRTUAL, ObjectType, Instruction}
import sandbox.stackAnalysis.datastructure.{LocVariables, Stack, State}
import sandbox.findbugs.{BugType, BugLogger}

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 17.12.12
 * Time: 11:07
 * To change this template use File | Settings | File Templates.
 */
object MismatchedNotifyFinder extends StackBugFinder {

  def notifyInstruction(pc: Int, instructions: Array[Instruction], analysis: Array[State], logger: BugLogger) = {
    val instr = instructions(pc)

    if (instr.isInstanceOf[INVOKEVIRTUAL]) {
      val invInstr = instr.asInstanceOf[INVOKEVIRTUAL]

      if (invInstr.declaringClass.equals(ObjectType.Object) &&
        (invInstr.name.equals("notify") || invInstr.name.equals("notifyAll"))) {
        checkForBugs(pc, instructions, analysis, logger, checkMismatchedNotify)
      }
    }

  }

  //TODO: implement
  private def checkMismatchedNotify(pc: Int, instructions: Array[Instruction], stack: Stack, loc: LocVariables): Option[BugType.Value] = {
    None
  }
}
