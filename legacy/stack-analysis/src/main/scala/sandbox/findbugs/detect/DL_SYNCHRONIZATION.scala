package sandbox.findbugs.detect

import sandbox.stackAnalysis.datastructure.{LocalVariables, Stack, State}
import sandbox.findbugs.BugType
import de.tud.cs.st.bat.resolved._
import sae.bytecode.structure.CodeInfo

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 14.12.12
 * Time: 12:58
 * To change this template use File | Settings | File Templates.
 */
object DL_SYNCHRONIZATION extends Detector {

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


  def getDetectorFunction(instr: Instruction): (Int, Instruction, Stack, LocalVariables) => Option[BugType.Value] = {

    if (instr.isInstanceOf[MONITORENTER.type]) {
      return checkSynchronize
    }

    return checkNone

  }

  private def checkSynchronize(pc: Int, instr: Instruction, stack: Stack, lv: LocalVariables): Option[BugType.Value] = {
    if (stack.size == 0)
      return None

    val stackHead = stack.get(0)
    if (stackHead.getItemType.isOfType(ObjectType("java/lang/String"))) {
      Some(BugType.DL_SYNCHRONIZATION_ON_SHARED_CONSTANT)
    } else if (BAD_SIGNATURES.exists(stackHead.getItemType.isOfType)) {
      val isSyncOnBoolean = stackHead.getItemType.isOfType(ObjectType("java/lang/Boolean"))
      if (stackHead.isCreatedByNew) {
        Some(BugType.DL_SYNCHRONIZATION_ON_UNSHARED_BOXED_PRIMITIVE)
      } else if (isSyncOnBoolean) {
        Some(BugType.DL_SYNCHRONIZATION_ON_BOOLEAN)
      } else {
        Some(BugType.DL_SYNCHRONIZATION_ON_BOXED_PRIMITIVE)
      }
    } else {

      None
    }

  }
}
