package sae.analyses.findbugs.random.oo

import sae.Relation
import sae.syntax.sql._
import sae.bytecode._
import sae.bytecode.instructions._
import de.tud.cs.st.bat.resolved.ObjectType

/**
 *
 * Author: Ralf Mitschke
 * Date: 10.08.12
 * Time: 10:10
 *
 */
object DP_DO_INSIDE_DO_PRIVILEGED
    extends (BytecodeDatabase => Relation[INVOKEVIRTUAL])
{
    val reflectionField = ObjectType ("java/lang/reflect/Field")

    val reflectionMethod = ObjectType ("java/lang/reflect/Method")

    val priviledgedAction = ObjectType ("java/security/PrivilegedAction")

    val priviledgedExceptionAction = ObjectType ("java/security/PrivilegedExceptionAction")


    // With the analyzed sequence check FindBugs only finds
    // new Integer(1).doubleValue()
    // and not
    // Integer.valueOf(1).doubleValue()
    def apply(database: BytecodeDatabase): Relation[INVOKEVIRTUAL] = {
        import database._


        SELECT (*) FROM invokeVirtual WHERE
            ((i: INVOKEVIRTUAL) => (i.receiverType == reflectionField || i.receiverType == reflectionMethod)) AND
            (_.name == "setAccessible") AND
            (_.declaringMethod.declaringClass.interfaces.exists (
                interface => interface == priviledgedAction || interface == priviledgedExceptionAction
            ))
    }


    /**
     * ######### Findbugs code ############
     */

    /*
    @Override
    public void visit(JavaClass obj) {

        isDoPrivileged = Subtypes2.instanceOf(getDottedClassName(), "java.security.PrivilegedAction")
                || Subtypes2.instanceOf(getDottedClassName(), "java.security.PrivilegedExceptionAction");
    }

    @Override
    public void visit(Code obj) {
        if (isDoPrivileged && getMethodName().equals("run"))
            return;
        if (getMethod().isPrivate())
            return;
        if (DumbMethods.isTestMethod(getMethod()))
            return;
        super.visit(obj);
        bugAccumulator.reportAccumulatedBugs();
    }

    @Override
    public void sawOpcode(int seen) {
        if (seen == INVOKEVIRTUAL && getNameConstantOperand().equals("setAccessible")) {
            @DottedClassName
            String className = getDottedClassConstantOperand();
            if (className.equals("java.lang.reflect.Field") || className.equals("java.lang.reflect.Method"))
                bugAccumulator.accumulateBug(
                        new BugInstance(this, "DP_DO_INSIDE_DO_PRIVILEGED", LOW_PRIORITY).addClassAndMethod(this)
                                .addCalledMethod(this), this);

        }
     */
}