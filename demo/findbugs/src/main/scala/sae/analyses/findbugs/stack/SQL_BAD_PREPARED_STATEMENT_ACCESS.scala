package sae.analyses.findbugs.stack

import sae.bytecode.BytecodeDatabase
import sae.Relation
import sae.bytecode.instructions.{POP2, POP, InstructionInfo}
import sae.syntax.sql._
import structure.{Item, Stack}
import de.tud.cs.st.bat.resolved.{ObjectType, INVOKEINTERFACE, Instruction}


/**
 * @author Mirko
 * @author Ralf Mitschke
 */
object SQL_BAD_PREPARED_STATEMENT_ACCESS
    extends (BytecodeDatabase => Relation[InstructionInfo])
{


    private val SUFFIX_LIST: List[String] = "Array" :: "AsciiStream" :: "BigDecimal" :: "BinaryStream" ::
        "Blob" :: "Boolean" :: "Byte" :: "Bytes" :: "CharacterStream" :: "Clob" :: "Date" :: "Double" ::
        "Float" :: "Int" :: "Long" :: "Object" :: "Ref" :: "RowId" :: "Short" :: "String" :: "Time" :: "Timestamp" ::
        "UnicodeStream" :: "URL" :: Nil


    private def callsPreparedStatementSet(instruction: Instruction): Boolean = {
        if (!instruction.isInstanceOf[INVOKEINTERFACE])
            return false

        val invoke = instruction.asInstanceOf[INVOKEINTERFACE]
        invoke.declaringClass == ObjectType ("java/sql/PreparedStatement") &&
            invoke.name.size > 3 &&
            invoke.name.startsWith ("set") &&
            SUFFIX_LIST.contains (invoke.name.substring (3))
    }

    def apply(database: BytecodeDatabase): Relation[InstructionInfo] = {
        val dataFlow = DataFlow(database)


        SELECT (instruction) FROM dataFlow WHERE
            (info => callsPreparedStatementSet(info.instruction.instruction)) AND
            (info => info.state.s.collection.exists(s => isBadAccess(info.instruction.instruction, s)))

        //SELECT (*) FROM dataFlow WHERE  (_.instruction.declaringMethod.name == "<clinit>")
    }

    private def isBadAccess(instruction: Instruction, stack: Stack): Boolean = {
        val invInstr = instruction.asInstanceOf[INVOKEINTERFACE]
        val numParams: Int = invInstr.methodDescriptor.parameterTypes.size
        val indexParam: Item = stack.get (numParams - 1)

        indexParam.isCouldBeZero
    }

}
