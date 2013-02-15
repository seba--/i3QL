package sandbox.findbugs.detect

import de.tud.cs.st.bat.resolved.{Instruction, ObjectType, INVOKEINTERFACE}
import sandbox.stackAnalysis.datastructure.{Item, LocalVariables, Stack, State}
import sandbox.findbugs.{BugType, BugLogger}
import sae.bytecode.structure.CodeInfo

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 10.12.12
 * Time: 11:04
 * To change this template use File | Settings | File Templates.
 */
object SQL_BAD_PREPARED_STATEMENT_ACCESS extends BugFinder {

  private def SUFFIX_LIST: List[String] = "Array" :: "AsciiStream" :: "BigDecimal" :: "BinaryStream" ::
    "Blob" :: "Boolean" :: "Byte" :: "Bytes" :: "CharacterStream" :: "Clob" :: "Date" :: "Double" ::
    "Float" :: "Int" :: "Long" :: "Object" :: "Ref" :: "RowId" :: "Short" :: "String" :: "Time" :: "Timestamp" ::
    "UnicodeStream" :: "URL" :: Nil

  def checkBugs(pc: Int, instr: Instruction): (Int, Instruction, Stack, LocalVariables) => Option[BugType.Value] = {

    if (instr.isInstanceOf[INVOKEINTERFACE]) {
      val invInstr = instr.asInstanceOf[INVOKEINTERFACE]

      if (invInstr.declaringClass.equals(ObjectType("java/sql/PreparedStatement")) &&
        invInstr.name.startsWith("set") && SUFFIX_LIST.contains(invInstr.name.substring(3))) {
        return checkBadAccess
      }
    }

    return checkNone


  }

  private def checkBadAccess(pc: Int, instr: Instruction, stack: Stack, loc: LocalVariables): Option[BugType.Value] = {
    val invInstr = instr.asInstanceOf[INVOKEINTERFACE]
    val numParams: Int = invInstr.methodDescriptor.parameterTypes.size
    val indexParam: Item = stack.get(numParams - 1)

    if (indexParam.isCouldBeZero)
      return Some(BugType.SQL_BAD_PREPARED_STATEMENT_ACCESS)
    else
      return None
  }

}
