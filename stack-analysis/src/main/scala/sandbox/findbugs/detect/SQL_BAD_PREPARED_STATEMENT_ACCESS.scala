package sandbox.findbugs.detect

import de.tud.cs.st.bat.resolved.{ObjectType, INVOKEINTERFACE}
import sandbox.stackAnalysis.datastructure.{Item, LocVariables, Stack, State}
import sandbox.findbugs.{BugType, BugLogger}
import sae.bytecode.structure.CodeInfo

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 10.12.12
 * Time: 11:04
 * To change this template use File | Settings | File Templates.
 */
object SQL_BAD_PREPARED_STATEMENT_ACCESS extends StackBugFinder {

  private def SUFFIX_LIST: List[String] = "Array" :: "AsciiStream" :: "BigDecimal" :: "BinaryStream" ::
    "Blob" :: "Boolean" :: "Byte" :: "Bytes" :: "CharacterStream" :: "Clob" :: "Date" :: "Double" ::
    "Float" :: "Int" :: "Long" :: "Object" :: "Ref" :: "RowId" :: "Short" :: "String" :: "Time" :: "Timestamp" ::
    "UnicodeStream" :: "URL" :: Nil

  def notifyInstruction(pc: Int, codeInfo: CodeInfo, analysis: Array[State], logger: BugLogger) = {

    val instr = codeInfo.code.instructions(pc)

    if (instr.isInstanceOf[INVOKEINTERFACE]) {
      val invInstr = instr.asInstanceOf[INVOKEINTERFACE]

      if (invInstr.declaringClass.equals(ObjectType("java/sql/PreparedStatement")) &&
        invInstr.name.startsWith("set") && SUFFIX_LIST.contains(invInstr.name.substring(3))) {
        checkForBugs(pc, codeInfo, analysis, logger, checkBadAccess)
      }
    }

  }

  private def checkBadAccess(pc: Int, codeInfo: CodeInfo, stack: Stack, loc: LocVariables): Option[BugType.Value] = {
    val invInstr = codeInfo.code.instructions(pc).asInstanceOf[INVOKEINTERFACE]
    val numParams: Int = invInstr.methodDescriptor.parameterTypes.size
    val indexParam: Item = stack.get(numParams - 1)

    if (indexParam.isCouldBeZero)
      return Some(BugType.SQL_BAD_PREPARED_STATEMENT_ACCESS)
    else
      return None
  }

}
