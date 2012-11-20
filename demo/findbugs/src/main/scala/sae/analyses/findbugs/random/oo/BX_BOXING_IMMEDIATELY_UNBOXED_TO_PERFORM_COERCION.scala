package sae.analyses.findbugs.random.oo

import sae.Relation
import sae.syntax.sql._
import sae.bytecode._
import de.tud.cs.st.bat.resolved._
import de.tud.cs.st.bat.resolved.INVOKESPECIAL
import de.tud.cs.st.bat.resolved.INVOKEVIRTUAL
import structure.CodeInfo

/**
 *
 * Author: Ralf Mitschke
 * Date: 09.08.12
 * Time: 16:28
 *
 */
object BX_BOXING_IMMEDIATELY_UNBOXED_TO_PERFORM_COERCION
    extends (BytecodeDatabase => Relation[CodeInfo])
{
    // With the analyzed sequence check FindBugs only finds
    // new Integer(1).doubleValue()
    // and not
    // Integer.valueOf(1).doubleValue()
    def apply(database: BytecodeDatabase): Relation[CodeInfo] = {
        import database._

        SELECT (*) FROM (code) WHERE
            (_.declaringMethod.declaringClass.majorVersion >= 49) AND
            (
                (codeInfo: CodeInfo) => {
                    val instructions = codeInfo.code.instructions
                    var i = 0
                    var lastInvokeSpecial: (Int, INVOKESPECIAL) = null
                    var lastReceiverType: ReferenceType = null
                    var found = false
                    while (i < instructions.length) {
                        instructions (i) match {
                            case instr@INVOKESPECIAL (ObjectType (className), _, MethodDescriptor (Seq (_), _))
                                if className.startsWith ("java/lang") => lastInvokeSpecial = (i, instr)
                            case INVOKEVIRTUAL (secondReceiver, name, MethodDescriptor (Seq (), returnType))
                                if (i - 1) == lastInvokeSpecial._1 =>
                            {
                                val invokeSpecial = lastInvokeSpecial._2
                                if (invokeSpecial.declaringClass == secondReceiver &&
                                    invokeSpecial.methodDescriptor.parameterTypes (0) != returnType &&
                                    name.endsWith ("Value")
                                )
                                {
                                    found = true
                                    i = instructions.length // easy bail out
                                }
                            }
                            case _ =>
                        }
                        i += 1

                    }
                    found
                }
                )
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
