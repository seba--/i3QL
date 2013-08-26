package sae.analyses.findbugs.stack

import sae.bytecode.BytecodeDatabase
import sae.Relation
import sae.bytecode.instructions.InstructionInfo
import sae.syntax.sql._
import structure._
import de.tud.cs.st.bat.resolved._
import de.tud.cs.st.bat.resolved.ISTORE
import de.tud.cs.st.bat.resolved.ASTORE
import de.tud.cs.st.bat.resolved.LSTORE
import structure.Stack
import de.tud.cs.st.bat.resolved.DSTORE
import de.tud.cs.st.bat.resolved.FSTORE


/**
 * @author Mirko
 * @author Ralf Mitschke
 */
object SA_LOCAL_SELF_ASSIGNMENT
    extends (BytecodeDatabase => Relation[InstructionInfo])
{

    private def isStoreInstruction(instruction: Instruction): Boolean =
        instruction.isInstanceOf[StoreLocalVariableInstruction]

    def apply(database: BytecodeDatabase): Relation[InstructionInfo] = {
        val dataFlow = DataFlow (database)

        /*
        val stateWithCodeAttr =
        new TransactionalEquiJoinView(
            SELECT (*) FROM dataFlow WHERE
                (info => isStoreInstruction(info.instruction.instruction)),
                database.code,
            (_:InstructionInfo).declaringMethod,
            (_:CodeAttribute).declaringMethod,
            (s:StateInfo,c:CodeAttribute) => (s,c)
                )

        SELECT( (s:StateInfo,c:CodeAttribute)  => s.instruction ) FROM    stateWithCodeAttr WHERE
            ((info:StateInfo,c:CodeAttribute) => info.state.s.collection.exists(s => isSelfStoreInstruction(s, info.state.l, c.)))
        */

        // TODO make local vars available
        SELECT (instruction) FROM dataFlow WHERE
            (info => isStoreInstruction (info.instruction.instruction)) AND
            (info => info.state.s.collection.exists (s => isSelfStoreInstruction (info.instruction.instruction, s, info.state.l, None)))
    }


    private def isSelfStoreInstruction(instr: Instruction, stack: Stack, lv: LocVariables, localVariableTable: Option[LocalVariables]): Boolean = {
        val lvIndex = getIndexOfLocalVariable (instr.asInstanceOf[StoreLocalVariableInstruction])

        //TODO: Remove this test when exceptions are implemented.
        if (stack.size == 0) {

        }
        else if (saveEquals (lv (lvIndex), stack (0))) {
            localVariableTable match {
                case None => return true

                case Some (varTable) => {
                    //TODO: Check if loc variable name is also a field name.
                    if (stack (0).isFromField && stack (0).getFieldName == varTable (lvIndex).name)
                        return false
                    else
                        return true
                }
            }
        }
        false
    }


    private def getIndexOfLocalVariable(instr: StoreLocalVariableInstruction): Int = {
        instr match {
            case ISTORE (x) => x

            case LSTORE (x) => x

            case FSTORE (x) => x

            case DSTORE (x) => x

            case ASTORE (x) => x

            case ISTORE_0 | LSTORE_0 | FSTORE_0 | DSTORE_0 | ASTORE_0 => 0
            case ISTORE_1 | LSTORE_1 | FSTORE_1 | DSTORE_1 | ASTORE_1 => 1
            case ISTORE_2 | LSTORE_2 | FSTORE_2 | DSTORE_2 | ASTORE_2 => 2
            case ISTORE_3 | LSTORE_3 | FSTORE_3 | DSTORE_3 | ASTORE_3 => 3

            case x => throw new IllegalArgumentException (x + ": The store instruction is unknown.")
        }
    }

    private def saveEquals(a: Any, b: Any): Boolean = {
        if (a == null)
            b == null
        else
            a.equals (b)
    }
}
