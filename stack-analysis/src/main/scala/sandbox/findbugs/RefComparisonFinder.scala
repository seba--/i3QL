package sandbox.findbugs

import sandbox.stackAnalysis.Configuration
import de.tud.cs.st.bat.resolved._

import sandbox.stackAnalysis.TypeOption.NullType
import sandbox.stackAnalysis.Stack

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 23.11.12
 * Time: 14:03
 * To change this template use File | Settings | File Templates.
 */
class RefComparisonFinder extends BugFinder[Configuration] {

  def notifyInstruction(pc: Int, instructions: Array[Instruction], analysis: Array[Configuration], logger: BugLogger) = {
    val instr = instructions(pc)

    //Comparison of two objects
    if (instr.isInstanceOf[IF_ACMPEQ] || instr.isInstanceOf[IF_ACMPNE]) {
      for (stack <- analysis(pc).s.collection) {
        checkRefComparison(pc, stack, logger)
      }
    } else if (instr.isInstanceOf[MethodInvocationInstruction]) {

      val methodInfo = getMethodDescriptor(instr.asInstanceOf[MethodInvocationInstruction])
      val methodName = methodInfo._1
      val declaringClass = methodInfo._2
      val methodDesc = methodInfo._3
      val isStatic = methodInfo._4

      if (methodName.equals("assertSame") &&
        methodDesc.equals(MethodDescriptor((ObjectType.Object :: ObjectType.Object :: Nil), VoidType))) {
        for (stack <- analysis(pc).s.collection) {
          checkRefComparison(pc, stack, logger)
        }
      } else if ( declaringClass != null && (
        !isStatic && methodName.equals("equals") && methodDesc.equals(MethodDescriptor((ObjectType.Object :: Nil), BooleanType))
        || isStatic &&  methodName.equals("assertEquals") && methodDesc.equals(MethodDescriptor((ObjectType.Object :: ObjectType.Object :: Nil), VoidType))
        && !declaringClass.equals(ObjectType("org/testng/Assert"))
        || isStatic && methodName.equals("equal") && methodDesc.equals(MethodDescriptor((ObjectType.Object :: ObjectType.Object :: Nil), VoidType))
        && !declaringClass.equals(ObjectType("com/google.common/base/Objects")))) {

        for (stack <- analysis(pc).s.collection) {
          checkEqualsComparison(pc, stack, logger)
        }

      }


    }

  }

  //TODO: implement
  private def checkEqualsComparison(pc: Int, stack: Stack, logger: BugLogger) = {

  }

  private def checkRefComparison(pc: Int, stack: Stack, logger: BugLogger) = {

    val rhs = stack(0)
    val lhs = stack(1)
    //Do nothing if comparison with null.
    if (rhs.isInstanceOf[NullType] || lhs.isInstanceOf[NullType]) {
      //return
    } else if (rhs.isReference && lhs.isReference) {
      //TODO:add case when the types of rhs and lhs are not compatible
      if (rhs.isType(ObjectType.Object) && lhs.isType(ObjectType.Object)) {
        //return
      } else if (rhs.isType(ObjectType.String) && lhs.isType(ObjectType.String)) {
        //handleStringComparison
        /*val rhsType = rhs.toType
        val lhsType = lhs.toType*/

        //TODO:Compare strings (needs information about static strings)
        // - two static strings => do not report
        // - dynamic string and anything => high
        // - static string and unknown => medium
        // - all other cases => low

        //check if one string is passed as a parameter
        if (rhs.getPC == -1 || lhs.getPC == -1)
          logger.log(pc, BugType.ES_COMPARING_PARAMETER_STRING_WITH_EQ)
        else
          logger.log(pc, BugType.ES_COMPARING_STRINGS_WITH_EQ)

      } else if (rhs.isType(RefComparisonFinder.suspiciousTypes) || lhs.isType(RefComparisonFinder.suspiciousTypes)) {
        //handleSuspiciousTypeComparison
        //TODO:add case where one side is a constant (final static)
        if (rhs.isType(ObjectType("java/lang/Boolean")) && lhs.isType(ObjectType("java/lang/Boolean"))) {
          logger.log(pc, BugType.RC_REF_COMPARISON_BAD_PRACTICE_BOOLEAN)
          /*}  else if (rhs.isConstant || lhs.isConstant) {
        logger.log(pc,BugType.RC_REF_COMPARISON_BAD_PRACTICE)  */
        } else {
          logger.log(pc, BugType.RC_REF_COMPARISON)
        }

      }

    }


  }

  //Returns MethodName, DeclaringClass, MethodDescriptor, isStatic
  private def getMethodDescriptor(instr: MethodInvocationInstruction): (String, ReferenceType, MethodDescriptor, Boolean) = {

    instr match {
      case INVOKEDYNAMIC(s, m) => {
        return (s, null, m, false)
      }
      case INVOKEINTERFACE(classReference, name, method) => {
        return (name, classReference, method, false)
      }
      case INVOKESPECIAL(classReference, name, method) => {
        return (name, classReference, method, false)
      }
      case INVOKESTATIC(classReference, name, method) => {
        return (name, classReference, method, true)
      }
      case INVOKEVIRTUAL(classReference, name, method) => {
        return (name, classReference, method, false)
      }
      case x => throw new IllegalArgumentException(x + ": The invoke instruction is unknown.")
    }

  }

}

object RefComparisonFinder {
  def suspiciousTypes: List[Type] =
    ObjectType("java/lang/Boolean") ::
      ObjectType("java/lang/Byte") ::
      ObjectType("java/lang/Character") ::
      ObjectType("java/lang/Double") ::
      ObjectType("java/lang/Float") ::
      ObjectType("java/lang/Integer") ::
      ObjectType("java/lang/Long") ::
      ObjectType("java/lang/Short") ::
      Nil
}
