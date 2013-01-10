package sae.analyses.findbugs.random.oo.optimized

import sae.Relation
import sae.syntax.sql._
import sae.bytecode._
import sae.bytecode.instructions._
import de.tud.cs.st.bat.resolved.{LongType, DoubleType}

import sae.operators.impl.{EquiJoinView, TransactionalEquiJoinView}
import sae.analyses.findbugs.base.oo.Definitions

/**
 *
 * Author: Ralf Mitschke
 * Date: 06.08.12
 * Time: 15:53
 *
 */
object DMI_LONG_BITS_TO_DOUBLE_INVOKED_ON_INT
    extends (BytecodeDatabase => Relation[INVOKESTATIC])
{

    val doubleClass = ClassType ("java/lang/Double")

    def apply(database: BytecodeDatabase): Relation[INVOKESTATIC] = {
        import database._

        val intToLong: Relation[I2L] = SELECT ((_: InstructionInfo).asInstanceOf[I2L]) FROM instructions WHERE (_.isInstanceOf[I2L])

        val longBitsToDouble = compile (
            SELECT (*) FROM invokeStatic WHERE
                (_.receiverType == doubleClass) AND
                (_.name == "longBitsToDouble") AND
                (_.returnType == DoubleType) AND
                (_.parameterTypes == List (LongType))
        )

        if (Definitions.transactional)
            new TransactionalEquiJoinView (
                intToLong,
                longBitsToDouble,
                ((i: I2L) => (i.declaringMethod, i.sequenceIndex)),
                ((i: INVOKESTATIC) => (i.declaringMethod, i.sequenceIndex - 1)),
                (a: I2L, b: INVOKESTATIC) => b
            )
        else
            new EquiJoinView (
                intToLong,
                longBitsToDouble,
                ((i: I2L) => (i.declaringMethod, i.sequenceIndex)),
                ((i: INVOKESTATIC) => (i.declaringMethod, i.sequenceIndex - 1)),
                (a: I2L, b: INVOKESTATIC) => b
            )

    }

    /*
       if (prevOpcode == I2L && seen == INVOKESTATIC && getClassConstantOperand().equals("java/lang/Double")
               && getNameConstantOperand().equals("longBitsToDouble")) {
           accumulator.accumulateBug(new BugInstance(this, "DMI_LONG_BITS_TO_DOUBLE_INVOKED_ON_INT", HIGH_PRIORITY)
                   .addClassAndMethod(this).addCalledMethod(this), this);
       }
    */

}