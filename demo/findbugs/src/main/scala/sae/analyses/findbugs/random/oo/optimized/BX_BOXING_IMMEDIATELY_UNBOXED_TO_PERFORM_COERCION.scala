package sae.analyses.findbugs.random.oo.optimized

import sae.Relation
import sae.syntax.sql._
import sae.bytecode._
import sae.bytecode.instructions._
import sae.operators.impl.{EquiJoinView, TransactionalEquiJoinView}
import sae.analyses.findbugs.AnalysesOO

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

        val invokeSpecialSelection = compile (
            SELECT (*) FROM
                (invokeSpecial) WHERE
                (_.parameterTypes.size == 1) AND
                (!_.parameterTypes (0).isReferenceType) AND
                (_.declaringMethod.declaringClass.majorVersion >= 49) AND
                (_.receiverType.isObjectType) AND
                (_.receiverType.asInstanceOf[ClassType].className.startsWith ("java/lang"))
        )

        val invokeVirtualSelection = compile (
            SELECT (*) FROM
                (invokeVirtual) WHERE
                ((_: INVOKEVIRTUAL).parameterTypes == Nil) AND
                (_.name.endsWith ("Value"))
        )


        val join =
            if (AnalysesOO.transactional)
                new TransactionalEquiJoinView (
                    invokeSpecialSelection,
                    invokeVirtualSelection,
                    ((i: INVOKESPECIAL) => (i.declaringMethod, i.receiverType, i.sequenceIndex)),
                    ((i: INVOKEVIRTUAL) => (i.declaringMethod, i.receiverType, i.sequenceIndex - 1)),
                    (a: INVOKESPECIAL, b: INVOKEVIRTUAL) => (a, b)
                )
            else
                new EquiJoinView (
                    invokeSpecialSelection,
                    invokeVirtualSelection,
                    ((i: INVOKESPECIAL) => (i.declaringMethod, i.receiverType, i.sequenceIndex)),
                    ((i: INVOKEVIRTUAL) => (i.declaringMethod, i.receiverType, i.sequenceIndex - 1)),
                    (a: INVOKESPECIAL, b: INVOKEVIRTUAL) => (a, b)
                )

        SELECT ((e: (INVOKESPECIAL, INVOKEVIRTUAL)) => e._2) FROM (join) WHERE
            ((e: (INVOKESPECIAL, INVOKEVIRTUAL)) => e._1.parameterTypes (0) != e._2.returnType)

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
