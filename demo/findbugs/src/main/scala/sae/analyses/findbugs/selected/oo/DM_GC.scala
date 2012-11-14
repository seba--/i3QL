package sae.analyses.findbugs.selected.oo

import sae.bytecode.BytecodeDatabase
import sae.Relation
import sae.syntax.sql._
import sae.bytecode.instructions.InvokeInstruction
import de.tud.cs.st.bat.resolved.VoidType
import sae.analyses.findbugs.base.oo.Definitions


/**
 *
 * @author Ralf Mitschke
 *
 */
object DM_GC
        extends (BytecodeDatabase => Relation[InvokeInstruction])
{

    val gcReg = "(^gc)|(gc$)".r

    def apply(database: BytecodeDatabase): Relation[InvokeInstruction] = {
        val definitions = Definitions(database)
        import database._
        import definitions._

        SELECT(*) FROM invokeStatic WHERE
                NOT((_: InvokeInstruction).declaringMethod.declaringClassType.className.startsWith("java/lang")) AND
                NOT((instr: InvokeInstruction) => gcReg.findFirstIn(instr.declaringMethod.name)) AND
                (_.receiverType == system) AND
                (_.name == "gc") AND
                (_.parameterTypes == Nil) AND
                (_.returnType == VoidType) UNION_ALL (
                SELECT(*) FROM invokeVirtual WHERE
                        NOT((_: InvokeInstruction).declaringMethod.declaringClassType.className
                                .startsWith("java/lang")) AND
                        (_.receiverType == runtime)
                                (_.name == "gc") AND
                        (_.parameterTypes == Nil) AND
                        (_.returnType == VoidType)
                )
    }

}