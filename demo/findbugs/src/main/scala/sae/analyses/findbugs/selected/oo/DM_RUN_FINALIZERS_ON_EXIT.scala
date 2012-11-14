package sae.analyses.findbugs.selected.oo

import sae.bytecode.BytecodeDatabase
import sae.Relation
import sae.syntax.sql._
import sae.analyses.findbugs.base.oo.Definitions
import sae.bytecode.instructions.{INVOKESTATIC, InstructionInfo}
import de.tud.cs.st.bat.resolved.{BooleanType, VoidType}

/**
 *
 * @author Ralf Mitschke
 *
 */
object DM_RUN_FINALIZERS_ON_EXIT
        extends (BytecodeDatabase => Relation[InstructionInfo])
{

    def apply(database: BytecodeDatabase): Relation[InstructionInfo] = {
        val definitions = Definitions(database)
        import definitions._
        import database._

        SELECT(*) FROM invokeStatic WHERE (
                ((_: INVOKESTATIC).receiverType == system) OR
                        (_.receiverType == runtime)) AND
                (_.name == "runFinalizersOnExit") AND
                (_.parameterTypes == Seq(BooleanType)) AND
                (_.returnType == VoidType)
    }

}