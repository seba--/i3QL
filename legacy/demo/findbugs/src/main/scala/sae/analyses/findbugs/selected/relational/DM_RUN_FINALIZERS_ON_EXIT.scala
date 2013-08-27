package sae.analyses.findbugs.selected.relational

import sae.bytecode.BytecodeDatabase
import sae.Relation
import sae.syntax.sql._
import sae.analyses.findbugs.base.relational.Definitions
import sae.bytecode.instructions.minimal.INVOKESTATIC
import de.tud.cs.st.bat.resolved.{BooleanType, VoidType}

/**
 *
 * @author Ralf Mitschke
 *
 */
object DM_RUN_FINALIZERS_ON_EXIT
    extends (BytecodeDatabase => Relation[INVOKESTATIC])
{

    def apply(database: BytecodeDatabase): Relation[INVOKESTATIC] = {
        val definitions = Definitions (database)
        import database._
        import definitions._


        SELECT (*) FROM invokeStaticMinimal WHERE (
            ((_: INVOKESTATIC).receiverType == system) OR
                (_.receiverType == runtime)) AND
            (_.name == "runFinalizersOnExit") AND
            (_.parameterTypes == Seq (BooleanType)) AND
            (_.returnType == VoidType)
    }

}