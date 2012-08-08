package sae.findbugs.analyses

/**
 *
 * Author: Ralf Mitschke
 * Date: 06.08.12
 * Time: 15:53
 *
 */
object DMI_LONG_BITS_TO_DOUBLE_INVOKED_ON_INT
{

    /*
        if (prevOpcode == I2L && seen == INVOKESTATIC && getClassConstantOperand().equals("java/lang/Double")
                && getNameConstantOperand().equals("longBitsToDouble")) {
            accumulator.accumulateBug(new BugInstance(this, "DMI_LONG_BITS_TO_DOUBLE_INVOKED_ON_INT", HIGH_PRIORITY)
                    .addClassAndMethod(this).addCalledMethod(this), this);
        }
     */

}