package sae.analyses.findbugs.stack

import sae.bytecode.BytecodeDatabase
import sae.Relation
import sae.bytecode.instructions.{POP2, POP, InstructionInfo}
import sae.syntax.sql._
import structure.Stack
import de.tud.cs.st.bat.resolved.{ObjectType, Type}


/**
 * @author Mirko
 * @author Ralf Mitschke
 */
object DL_SYNCHRONIZATION
    extends (BytecodeDatabase => Relation[InstructionInfo])
{

    private val BAD_SIGNATURES: List[Type] =
        ObjectType ("java/lang/Boolean") ::
            ObjectType ("java/lang/Byte") ::
            ObjectType ("java/lang/Character") ::
            ObjectType ("java/lang/Double") ::
            ObjectType ("java/lang/Float") ::
            ObjectType ("java/lang/Integer") ::
            ObjectType ("java/lang/Long") ::
            ObjectType ("java/lang/Short") ::
            Nil

    private def isSynchError(stack: Stack): Boolean = {
        if (stack.size == 0)
            return false
        val head = stack.get (0)
        BAD_SIGNATURES.exists (t => head.getDeclaredType.isOfType (t)) &&
            !head.isCreatedByNew &&
            !head.getDeclaredType.isOfType (ObjectType ("java/lang/Boolean"))
    }

    def apply(database: BytecodeDatabase): Relation[InstructionInfo] = {
        val dataFlow = DataFlow(database)


        SELECT (instruction) FROM dataFlow WHERE
            (info => info.state.s.collection.exists(isSynchError))

    }


}
