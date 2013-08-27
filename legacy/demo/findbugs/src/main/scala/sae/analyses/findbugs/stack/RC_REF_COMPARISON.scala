package sae.analyses.findbugs.stack

import sae.bytecode.BytecodeDatabase
import sae.Relation
import sae.bytecode.instructions.InstructionInfo
import sae.syntax.sql._
import de.tud.cs.st.bat.resolved._
import structure.{ItemType, Stack}
import de.tud.cs.st.bat.resolved.INVOKESPECIAL
import de.tud.cs.st.bat.resolved.INVOKEDYNAMIC
import de.tud.cs.st.bat.resolved.INVOKESTATIC
import de.tud.cs.st.bat.resolved.INVOKEINTERFACE
import de.tud.cs.st.bat.resolved.INVOKEVIRTUAL
import de.tud.cs.st.bat.resolved.IF_ACMPEQ
import de.tud.cs.st.bat.resolved.IF_ACMPNE


/**
 * @author Mirko
 * @author Ralf Mitschke
 */
object RC_REF_COMPARISON
    extends (BytecodeDatabase => Relation[InstructionInfo])
{


    private val suspiciousTypes: List[Type] =
    // ObjectType ("java/lang/Boolean") :: // marks a different bug pattern
        ObjectType ("java/lang/Byte") ::
            ObjectType ("java/lang/Character") ::
            ObjectType ("java/lang/Double") ::
            ObjectType ("java/lang/Float") ::
            ObjectType ("java/lang/Integer") ::
            ObjectType ("java/lang/Long") ::
            ObjectType ("java/lang/Short") ::
            Nil


    private def isCompareInstruction(instruction: Instruction): Boolean =
        instruction.isInstanceOf[IF_ACMPEQ] || instruction.isInstanceOf[IF_ACMPNE]


    //Returns MethodName, DeclaringClass, MethodDescriptor, isStatic
    private def getMethodDescriptor(instr: Instruction): (String, ReferenceType, MethodDescriptor, Boolean) = {

        instr match {
            case INVOKEDYNAMIC (s, m) => {
                (s, null, m, false)
            }
            case INVOKEINTERFACE (classReference, name, method) => {
                (name, classReference, method, false)
            }
            case INVOKESPECIAL (classReference, name, method) => {
                (name, classReference, method, false)
            }
            case INVOKESTATIC (classReference, name, method) => {
                (name, classReference, method, true)
            }
            case INVOKEVIRTUAL (classReference, name, method) => {
                (name, classReference, method, false)
            }
            case x => throw new IllegalArgumentException (x + ": The invoke instruction is unknown.")
        }

    }

    private def isCompareMethodCall(instruction: Instruction): Boolean = {
        if (!instruction.isInstanceOf[MethodInvocationInstruction])
            return false
        val (methodName, declaringClass, methodDesc, isStatic) = getMethodDescriptor (instruction)

        (methodName == "assertSame" && methodDesc == MethodDescriptor ((ObjectType.Object :: ObjectType.Object :: Nil), VoidType)) ||
            (!isStatic && methodName == "equals" && methodDesc == MethodDescriptor ((ObjectType.Object :: Nil), BooleanType)) ||
            (isStatic && methodName == "assertEquals" && methodDesc == MethodDescriptor ((ObjectType.Object :: ObjectType.Object :: Nil), VoidType)) && declaringClass != ObjectType ("org/testng/Assert") ||
            (isStatic && methodName == "equal" && methodDesc == MethodDescriptor ((ObjectType.Object :: ObjectType.Object :: Nil), VoidType) && declaringClass != ObjectType ("com/google/common/base/Objects"))
    }


    private def makesComparison(instruction: Instruction): Boolean =
    {
        isCompareInstruction (instruction) || isCompareMethodCall (instruction)
    }


    def apply(database: BytecodeDatabase): Relation[InstructionInfo] = {
        val dataFlow = DataFlow (database)


        SELECT (instruction) FROM dataFlow WHERE
            (info => makesComparison (info.instruction.instruction)) AND
            (info => info.state.s.collection.exists (isSuspiciousRefComparison))

        //SELECT (*) FROM dataFlow WHERE  (_.instruction.declaringMethod.name == "<clinit>")
    }

    private def isSuspiciousRefComparison(stack: Stack): Boolean =
    {
        if (stack.size < 2)
            return false

        val rhs = stack (0)
        val lhs = stack (1)

        //Do nothing if comparison with null.
        if (rhs.isCouldBeNull || lhs.isCouldBeNull) {
            return false
        }

        if (rhs.getDeclaredType.isReferenceType && lhs.getDeclaredType.isReferenceType &&
            isSuspicious (rhs.getDeclaredType) || isSuspicious (lhs.getDeclaredType)
        )
        {
            return true
        }
        false
    }

    private def isSuspicious(itemType: ItemType): Boolean = {
        if (itemType.isInstanceOf[ItemType.SomeRef]) {
            val someRef = itemType.asInstanceOf[ItemType.SomeRef]
            return suspiciousTypes.contains (someRef.refType)
        }
        false
    }

    /*

   // RM: check the comparison; needs typeDataflow
   private void checkRefComparison(Location location, JavaClass jclass, Method method, MethodGen methodGen,
           RefComparisonTypeFrameModelingVisitor visitor, TypeDataflow typeDataflow,
           List<WarningWithProperties> stringComparisonList, List<WarningWithProperties> refComparisonList)
           throws DataflowAnalysisException {

       InstructionHandle handle = location.getHandle();

       TypeFrame frame = typeDataflow.getFactAtLocation(location);
       if (frame.getStackDepth() < 2) {
           throw new DataflowAnalysisException("Stack underflow", methodGen, handle);
       }

       int numSlots = frame.getNumSlots();
       Type lhsType = frame.getValue(numSlots - 2);
       Type rhsType = frame.getValue(numSlots - 1);

       if (lhsType instanceof NullType || rhsType instanceof NullType) {
           return;
       }
       if (lhsType instanceof ReferenceType && rhsType instanceof ReferenceType) {
           IncompatibleTypes newResult = IncompatibleTypes.getPriorityForAssumingCompatible(lhsType, rhsType, true);
           if (newResult != IncompatibleTypes.SEEMS_OK && newResult != IncompatibleTypes.UNCHECKED) {
               String sourceFile = jclass.getSourceFileName();

               boolean isAssertSame = handle.getInstruction() instanceof INVOKESTATIC;
               if (isAssertSame)
                   bugAccumulator.accumulateBug(
                           new BugInstance(this, "TESTING", newResult.getPriority())
                           .addClassAndMethod(methodGen, sourceFile)
                           .addString("Calling assertSame with two distinct objects")
                           .addFoundAndExpectedType(rhsType, lhsType)
                                   .addSomeSourceForTopTwoStackValues(classContext, method, location),
                           SourceLineAnnotation.fromVisitedInstruction(classContext, methodGen, sourceFile, handle));
                   else
               bugAccumulator.accumulateBug(
                       new BugInstance(this, "EC_UNRELATED_TYPES_USING_POINTER_EQUALITY", newResult.getPriority())
                               .addClassAndMethod(methodGen, sourceFile).addFoundAndExpectedType(rhsType, lhsType)
                               .addSomeSourceForTopTwoStackValues(classContext, method, location),
                       SourceLineAnnotation.fromVisitedInstruction(classContext, methodGen, sourceFile, handle));
               return;
           }
           if (lhsType.equals(Type.OBJECT) && rhsType.equals(Type.OBJECT))
               return;
           String lhs = SignatureConverter.convert(lhsType.getSignature());
           String rhs = SignatureConverter.convert(rhsType.getSignature());

           if (lhs.equals("java.lang.String") || rhs.equals("java.lang.String")) {
               handleStringComparison(jclass, method, methodGen, visitor, stringComparisonList, location, lhsType, rhsType);
           } else if (suspiciousSet.contains(lhs)) {
               handleSuspiciousRefComparison(jclass, method, methodGen, refComparisonList, location, lhs,
                       (ReferenceType) lhsType, (ReferenceType) rhsType);
           } else if (suspiciousSet.contains(rhs)) {
               handleSuspiciousRefComparison(jclass, method, methodGen, refComparisonList, location, rhs,
                       (ReferenceType) lhsType, (ReferenceType) rhsType);
           }
       }
   }

   // RM: Report the actual bug
   private void handleSuspiciousRefComparison(JavaClass jclass, Method method, MethodGen methodGen,
           List<WarningWithProperties> refComparisonList, Location location, String lhs, ReferenceType lhsType,
           ReferenceType rhsType) {
       XField xf = null;
       if (lhsType instanceof FinalConstant)
           xf = ((FinalConstant) lhsType).getXField();
       else if (rhsType instanceof FinalConstant)
           xf = ((FinalConstant) rhsType).getXField();
       String sourceFile = jclass.getSourceFileName();
       String bugPattern = "RC_REF_COMPARISON";
       int priority = Priorities.HIGH_PRIORITY;
       if (lhs.equals("java.lang.Boolean")) {
           bugPattern = "RC_REF_COMPARISON_BAD_PRACTICE_BOOLEAN";
           priority = Priorities.NORMAL_PRIORITY;
       } else if (xf != null && xf.isStatic() && xf.isFinal()) {
           bugPattern = "RC_REF_COMPARISON_BAD_PRACTICE";
           if (xf.isPublic() || !methodGen.isPublic())
               priority = Priorities.NORMAL_PRIORITY;
       }
       BugInstance instance = new BugInstance(this, bugPattern, priority).addClassAndMethod(methodGen, sourceFile)
               .addType("L" + lhs.replace('.', '/') + ";").describe(TypeAnnotation.FOUND_ROLE);
       if (xf != null)
           instance.addField(xf).describe(FieldAnnotation.LOADED_FROM_ROLE);
       else
           instance.addSomeSourceForTopTwoStackValues(classContext, method, location);
       SourceLineAnnotation sourceLineAnnotation = SourceLineAnnotation.fromVisitedInstruction(classContext, methodGen,
               sourceFile, location.getHandle());
       if (sourceLineAnnotation != null)
           refComparisonList.add(new WarningWithProperties(instance, new WarningPropertySet<WarningProperty>(),
                   sourceLineAnnotation, location));
   }

    */
}
