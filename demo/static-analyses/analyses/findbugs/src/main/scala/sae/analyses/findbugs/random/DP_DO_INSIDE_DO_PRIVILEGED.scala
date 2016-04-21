package sae.analyses.findbugs.random

import sae.bytecode.BytecodeDatabase
import idb.Relation
import idb.syntax.iql._
import idb.syntax.iql.IR._


/**
 * @author Ralf Mitschke, Mirko Köhler
 */
object DP_DO_INSIDE_DO_PRIVILEGED extends (BytecodeDatabase => Relation[BytecodeDatabase#MethodInvocationInstruction]) {



  // With the analyzed sequence check FindBugs only finds
  // new Integer(1).doubleValue()
  // and not
  // Integer.valueOf(1).doubleValue()
  def apply(database: BytecodeDatabase): Relation[BytecodeDatabase#MethodInvocationInstruction] = {
    import database._

    SELECT (*) FROM methodInvocationInstructions WHERE
      ((i: Rep[MethodInvocationInstruction]) => {
		((i.methodInfo.receiverType == ObjectType ("java/lang/reflect/Field")) OR (i.methodInfo.receiverType == ("java/lang/reflect/Method"))) AND
        (i.methodInfo.name == "setAccessible") AND
        NOT (i.declaringMethod.declaringClass.interfaces.contains(ObjectType ("java/security/PrivilegedAction"))) AND
		NOT (i.declaringMethod.declaringClass.interfaces.contains(ObjectType ("java/security/PrivilegedExceptionAction")))
	  })
  }
}
     /*
def apply(database: BytecodeDatabase): Relation[INVOKEVIRTUAL] = {
import database._


SELECT (*) FROM invokeVirtual WHERE
((i: INVOKEVIRTUAL) => (i.receiverType == reflectionField || i.receiverType == reflectionMethod)) AND
(_.name == "setAccessible") AND
(!_.declaringMethod.declaringClass.interfaces.exists (
interface => interface == priviledgedAction || interface == priviledgedExceptionAction
))
}

*/

