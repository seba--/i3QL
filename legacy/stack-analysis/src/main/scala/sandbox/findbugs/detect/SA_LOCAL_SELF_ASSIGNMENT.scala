package sandbox.findbugs.detect

import de.tud.cs.st.bat.resolved._
import de.tud.cs.st.bat.resolved.ISTORE
import de.tud.cs.st.bat.resolved.ASTORE
import de.tud.cs.st.bat.resolved.LSTORE
import de.tud.cs.st.bat.resolved.FSTORE
import de.tud.cs.st.bat.resolved.DSTORE
import sandbox.stackAnalysis.datastructure.{LocalVariables, Stack, State}
import sandbox.findbugs.BugType
import sae.bytecode.structure.CodeInfo

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 02.12.12
 * Time: 17:19
 * To change this template use File | Settings | File Templates.
 */
object SA_LOCAL_SELF_ASSIGNMENT extends Detector {

  def getDetectorFunction(instr: Instruction): (Int, Instruction, Stack, LocalVariables) => Option[BugType.Value] = {

    if (instr.isInstanceOf[StoreLocalVariableInstruction]) {
      return checkStoreInstruction
    }
    return checkNone
  }

  private def checkStoreInstruction(pc: Int, instr: Instruction, stack: Stack, lv: LocalVariables): Option[BugType.Value] = {
    val lvIndex = getIndexOfLocalVariable(instr.asInstanceOf[StoreLocalVariableInstruction])
    //TODO: Remove this test when exceptions are implemented.
    if (stack.size == 0) {

    } else if (saveEquals(lv(lvIndex), stack(0))) {
      /* byCodeInfo.code.localVariableTable match {
      case None => return Some(BugType.SA_LOCAL_SELF_ASSIGNMENT)

      case Some(varTable) => {

        //TODO: Check if loc variable name is also a field name.
        if (stack(0).isFromField && stack(0).getFieldName == varTable(lvIndex).name)
          return Some(BugType.SA_LOCAL_SELF_ASSIGNMENT_INSTEAD_OF_FIELD)
        else
          return Some(BugType.SA_LOCAL_SELF_ASSIGNMENT)
      }
    }  */

      return Some(BugType.SA_LOCAL_SELF_ASSIGNMENT)
    }
    return None
  }


  private def getIndexOfLocalVariable(instr: StoreLocalVariableInstruction): Int = {
    instr match {
      case ISTORE(x) => x

      case LSTORE(x) => x

      case FSTORE(x) => x

      case DSTORE(x) => x

      case ASTORE(x) => x

      case ISTORE_0 | LSTORE_0 | FSTORE_0 | DSTORE_0 | ASTORE_0 => 0
      case ISTORE_1 | LSTORE_1 | FSTORE_1 | DSTORE_1 | ASTORE_1 => 1
      case ISTORE_2 | LSTORE_2 | FSTORE_2 | DSTORE_2 | ASTORE_2 => 2
      case ISTORE_3 | LSTORE_3 | FSTORE_3 | DSTORE_3 | ASTORE_3 => 3

      case x => throw new IllegalArgumentException(x + ": The store instruction is unknown.")
    }
  }

  private def saveEquals(a: Any, b: Any): Boolean = {
    if (a == null)
      return b == null
    else
      a.equals(b)
  }


}
