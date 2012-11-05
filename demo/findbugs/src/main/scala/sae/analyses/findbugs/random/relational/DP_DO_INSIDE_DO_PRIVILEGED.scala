package sae.analyses.findbugs.random.relational

import sae.Relation
import sae.syntax.sql._
import sae.bytecode._
import sae.bytecode.instructions._
import sae.bytecode.structure._

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
    val reflectionFieldClass = ClassType ("java/lang/reflect/Field")

    val reflectionMethodClass = ClassType ("java/lang/reflect/Method")

    val priviledgedActionClass = ClassType ("java/security/PrivilegedAction")

    val priviledgedExceptionActionClass = ClassType ("java/security/PrivilegedExceptionAction")


    // With the analyzed sequence check FindBugs only finds
    // new Integer(1).doubleValue()
    // and not
    // Integer.valueOf(1).doubleValue()
    def apply(database: BytecodeDatabase): Relation[INVOKEVIRTUAL] = {
        import database._
        val invokeVirtual /*: Relation[INVOKEVIRTUAL] */ = SELECT ((_: InstructionInfo).asInstanceOf[INVOKEVIRTUAL]) FROM instructions WHERE (_.isInstanceOf[INVOKEVIRTUAL])

        SELECT (*) FROM invokeVirtual WHERE
            (_.name == "setAccessible") AND
            (((_: INVOKEVIRTUAL).receiverType == reflectionFieldClass) OR (_.receiverType == reflectionMethodClass)) AND
            NOT (EXISTS (
                SELECT (*) FROM interfaceInheritance WHERE
                    (((_: InheritanceRelation).subType) === ((_: INVOKEVIRTUAL).declaringMethod.declaringClass)) AND
                    (((_: InheritanceRelation).superType == priviledgedActionClass) OR (_.superType == priviledgedExceptionActionClass))
            )
            )
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