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


    /*

   // Fixed list of suffixes for get/set/update methods
   private static final Set<String> dbFieldTypesSet = new HashSet<String>() {
       static final long serialVersionUID = -3510636899394546735L;
       {
           add("Array");
           add("AsciiStream");
           add("BigDecimal");
           add("BinaryStream");
           add("Blob");
           add("Boolean");
           add("Byte");
           add("Bytes");
           add("CharacterStream");
           add("Clob");
           add("Date");
           add("Double");
           add("Float");
           add("Int");
           add("Long");
           add("Object");
           add("Ref");
           add("RowId");
           add("Short");
           add("String");
           add("Time");
           add("Timestamp");
           add("UnicodeStream");
           add("URL");
       }
   };

   //RM: Bug reported on seeing invoke_interface
   public void sawOpcode(int seen) {

       if (seen == INVOKEINTERFACE) {
           String methodName = getNameConstantOperand();
           String clsConstant = getClassConstantOperand();
           if ((clsConstant.equals("java/sql/ResultSet") && ((methodName.startsWith("get") && dbFieldTypesSet
                   .contains(methodName.substring(3))) || (methodName.startsWith("update") && dbFieldTypesSet
                   .contains(methodName.substring(6)))))
                   || ((clsConstant.equals("java/sql/PreparedStatement") && ((methodName.startsWith("set") && dbFieldTypesSet
                           .contains(methodName.substring(3))))))) {
               String signature = getSigConstantOperand();
               int numParms = PreorderVisitor.getNumberArguments(signature);
               if (stack.getStackDepth() >= numParms) {
                   OpcodeStack.Item item = stack.getStackItem(numParms - 1);

                   if ("I".equals(item.getSignature()) && item.couldBeZero()) {
                       bugReporter.reportBug(new BugInstance(this,
                               clsConstant.equals("java/sql/PreparedStatement") ? "SQL_BAD_PREPARED_STATEMENT_ACCESS"
                                       : "SQL_BAD_RESULTSET_ACCESS", item.mustBeZero() ? HIGH_PRIORITY : NORMAL_PRIORITY)
                               .addClassAndMethod(this).addSourceLine(this));
                   }
               }
           }
       }

   }
    */

}
