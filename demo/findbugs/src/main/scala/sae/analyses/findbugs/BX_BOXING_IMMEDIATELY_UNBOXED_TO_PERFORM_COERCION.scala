package sae.analyses.findbugs

import sae.Relation
import sae.syntax.sql._
import sae.bytecode._
import sae.bytecode.instructions._
import de.tud.cs.st.bat.resolved.FieldType

/**
 *
 * Author: Ralf Mitschke
 * Date: 09.08.12
 * Time: 16:28
 *
 */
object BX_BOXING_IMMEDIATELY_UNBOXED_TO_PERFORM_COERCION
    extends (BytecodeDatabase => Relation[INVOKEVIRTUAL])
{
    // With the analyzed sequence check FindBugs only finds
    // new Integer(1).doubleValue()
    // and not
    // Integer.valueOf(1).doubleValue()
    def apply(database: BytecodeDatabase): Relation[INVOKEVIRTUAL] = {
        import database._

        val invokeSpecial: Relation[INVOKESPECIAL] = SELECT ((_: InstructionInfo).asInstanceOf[INVOKESPECIAL]) FROM instructions WHERE (_.isInstanceOf[INVOKESPECIAL])
        val invokeVirtual: Relation[INVOKEVIRTUAL] = SELECT ((_: InstructionInfo).asInstanceOf[INVOKEVIRTUAL]) FROM instructions WHERE (_.isInstanceOf[INVOKEVIRTUAL])

        val firstParamType: INVOKESPECIAL => FieldType = _.parameterTypes (0)

        SELECT ((a: INVOKESPECIAL, b: INVOKEVIRTUAL) => b) FROM
            (invokeSpecial, invokeVirtual) WHERE
            (declaringMethod === declaringMethod) AND
            (receiverType === receiverType) AND
            (sequenceIndex === ((second: INVOKEVIRTUAL) => second.sequenceIndex - 1)) AND
            NOT (firstParamType === returnType) AND
            (_.declaringMethod.declaringClass.majorVersion >= 49) AND
            (_.receiverType.isObjectType) AND
            (_.receiverType.asInstanceOf[ClassType].className.startsWith ("java/lang")) AND
            ((_: INVOKEVIRTUAL).parameterTypes == Nil) AND
            (_.name.endsWith ("Value"))
    }

    /**
     * ###### FindBugs Code
     */
    /*
    @Override
    public void visit(JavaClass obj) {
        isTigerOrHigher = obj.getMajor() >= MAJOR_1_5;

   @Override
    public void sawOpcode(int seen) {
        if (stack.isTop()) {
            ...
            return;
        }


        if (isTigerOrHigher) {
            if (previousMethodInvocation != null && prevOpCode == INVOKESPECIAL && seen == INVOKEVIRTUAL) {
                String classNameForPreviousMethod = previousMethodInvocation.getClassName();
                String classNameForThisMethod = getClassConstantOperand();
                if (classNameForPreviousMethod.startsWith("java.lang.")
                        && classNameForPreviousMethod.equals(classNameForThisMethod.replace('/', '.'))
                        && getNameConstantOperand().endsWith("Value") && getSigConstantOperand().length() == 3) {
                    if (getSigConstantOperand().charAt(2) == previousMethodInvocation.getSignature().charAt(1))
                        bugAccumulator.accumulateBug(
                                new BugInstance(this, "BX_BOXING_IMMEDIATELY_UNBOXED", NORMAL_PRIORITY).addClassAndMethod(this),
                                this);

                    else
                        bugAccumulator.accumulateBug(new BugInstance(this, "BX_BOXING_IMMEDIATELY_UNBOXED_TO_PERFORM_COERCION",
                                NORMAL_PRIORITY).addClassAndMethod(this), this);

                    ternaryConversionState = 1;
                } else
                    ternaryConversionState = 0;
     */

}
