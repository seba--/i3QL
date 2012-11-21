package sae.analyses.findbugs.selected.relational

import sae.bytecode.BytecodeDatabase
import sae.Relation
import sae.syntax.sql._
import sae.bytecode.instructions.minimal.InvokeInstruction
import de.tud.cs.st.bat.resolved.VoidType
import sae.analyses.findbugs.base.relational.Definitions


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
        val definitions = Definitions (database)
        import database._
        import definitions._


        SELECT (*) FROM invokeStaticMinimal.asInstanceOf[Relation[InvokeInstruction]] WHERE
            NOT ((_: InvokeInstruction).declaringMethod.declaringType.className.startsWith ("java/lang")) AND
            NOT ((instr: InvokeInstruction) => gcReg.findFirstIn (instr.declaringMethod.name).isDefined) AND
            (_.receiverType == system) AND
            (_.name == "gc") AND
            (_.parameterTypes == Nil) AND
            (_.returnType == VoidType) UNION_ALL (
            SELECT (*) FROM invokeVirtualMinimal.asInstanceOf[Relation[InvokeInstruction]] WHERE
                NOT ((_: InvokeInstruction).declaringMethod.declaringType.className.startsWith ("java/lang")) AND
                NOT ((instr: InvokeInstruction) => gcReg.findFirstIn (instr.declaringMethod.name).isDefined) AND
                (_.receiverType == runtime) AND
                (_.name == "gc") AND
                (_.parameterTypes == Nil) AND
                (_.returnType == VoidType)
            )

    }

}