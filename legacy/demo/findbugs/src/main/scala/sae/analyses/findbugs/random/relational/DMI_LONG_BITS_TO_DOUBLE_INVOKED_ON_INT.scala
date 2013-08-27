package sae.analyses.findbugs.random.relational

import sae.Relation
import sae.syntax.sql._
import sae.bytecode._
import sae.bytecode.instructions._
import de.tud.cs.st.bat.resolved.{LongType, DoubleType}

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

        val intToLong : Relation[I2L] = SELECT ((_: InstructionInfo).asInstanceOf[I2L]) FROM instructions WHERE (_.isInstanceOf[I2L])

        SELECT ((a: I2L, b: INVOKESTATIC) => b) FROM
            (intToLong, invokeStatic) WHERE
            (declaringMethod === declaringMethod) AND
            (sequenceIndex === ((second: INVOKESTATIC) => second.sequenceIndex - 1)) AND
            ((_: INVOKESTATIC).receiverType == doubleClass) AND
            (_.name == "longBitsToDouble") AND
            (_.returnType == DoubleType) AND
            (_.parameterTypes == List(LongType))
    }


    /*
        if (prevOpcode == I2L && seen == INVOKESTATIC && getClassConstantOperand().equals("java/lang/Double")
                && getNameConstantOperand().equals("longBitsToDouble")) {
            accumulator.accumulateBug(new BugInstance(this, "DMI_LONG_BITS_TO_DOUBLE_INVOKED_ON_INT", HIGH_PRIORITY)
                    .addClassAndMethod(this).addCalledMethod(this), this);
        }
     */

}