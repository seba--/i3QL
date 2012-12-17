package sandbox.findbugs.detect

import sandbox.stackAnalysis.datastructure.{LocVariables, Stack, State}
import sandbox.findbugs.{BugType, BugLogger}
import de.tud.cs.st.bat.resolved._


/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 13.12.12
 * Time: 13:49
 * To change this template use File | Settings | File Templates.
 */
object ReturnValueIgnoredFinder extends StackBugFinder {

  private var previousInstructionWasNew = false


  def notifyInstruction(pc: Int, instructions: Array[Instruction], analysis: Array[State], logger: BugLogger) = {
    val instr = instructions(pc)


    if (instr.isInstanceOf[POP.type] || instr.isInstanceOf[POP2.type]) {
      checkForBugs(pc, instructions, analysis, logger, checkReturnValueIgnored)
    }

  }

  private def checkReturnValueIgnored(pc: Int, instructions: Array[Instruction], stack: Stack, lv: LocVariables): Option[BugType.Value] = {

    if (stack.size > 0 && (stack.get(0).isReturnValue || stack.get(0).isCreatedByNew))
      return Some(BugType.RV_RETURN_VALUE_IGNORED)

    return None
  }
}
