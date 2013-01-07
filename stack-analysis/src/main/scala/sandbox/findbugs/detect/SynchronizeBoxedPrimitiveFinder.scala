package sandbox.findbugs.detect

import sandbox.stackAnalysis.datastructure.{LocVariables, Stack, State}
import sandbox.findbugs.{BugType, BugLogger}
import de.tud.cs.st.bat.resolved._
import sae.bytecode.structure.CodeInfo

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 14.12.12
 * Time: 12:58
 * To change this template use File | Settings | File Templates.
 */
object SynchronizeBoxedPrimitiveFinder extends StackBugFinder {

  private val BAD_SIGNATURES: List[Type] =
    ObjectType("java/lang/Boolean") ::
      ObjectType("java/lang/Byte") ::
      ObjectType("java/lang/Character") ::
      ObjectType("java/lang/Double") ::
      ObjectType("java/lang/Float") ::
      ObjectType("java/lang/Integer") ::
      ObjectType("java/lang/Long") ::
      ObjectType("java/lang/Short") ::
      Nil


  def notifyInstruction(pc: Int, codeInfo: CodeInfo, analysis: Array[State], logger: BugLogger) = {
    val instr = codeInfo.code.instructions(pc)

    if (instr.isInstanceOf[MONITORENTER.type]) {
      checkForBugs(pc, codeInfo, analysis, logger, checkSynchronize)
    }

  }

  private def checkSynchronize(pc: Int, codeInfo: CodeInfo, stack: Stack, lv: LocVariables): Option[BugType.Value] = {
    val stackHead = stack.get(0)

    if (stackHead.getDeclaredType.isOfType(ObjectType("java/lang/String"))) {
      return Some(BugType.DL_SYNCHRONIZATION_ON_SHARED_CONSTANT)
    } else if (BAD_SIGNATURES.exists(stackHead.getDeclaredType.isOfType)) {
      val isSyncOnBoolean = stackHead.getDeclaredType.isOfType(ObjectType("java/lang/Boolean"))
      if (stackHead.isCreatedByNew) {
        return Some(BugType.DL_SYNCHRONIZATION_ON_UNSHARED_BOXED_PRIMITIVE)
      } else if (isSyncOnBoolean) {
        return Some(BugType.DL_SYNCHRONIZATION_ON_BOOLEAN)
      } else {
        return Some(BugType.DL_SYNCHRONIZATION_ON_BOXED_PRIMITIVE)
      }
    }

    return None
  }
}
