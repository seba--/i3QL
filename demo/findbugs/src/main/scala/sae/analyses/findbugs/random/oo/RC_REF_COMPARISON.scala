package sae.analyses.findbugs.random.oo

/**
 *
 * Author: Ralf Mitschke
 * Date: 06.08.12
 * Time: 15:12
 *
 */
object RC_REF_COMPARISON
{

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
            IncompatibleTypes result = IncompatibleTypes.getPriorityForAssumingCompatible(lhsType, rhsType, true);
            if (result != IncompatibleTypes.SEEMS_OK && result != IncompatibleTypes.UNCHECKED) {
                String sourceFile = jclass.getSourceFileName();

                boolean isAssertSame = handle.getInstruction() instanceof INVOKESTATIC;
                if (isAssertSame)
                    bugAccumulator.accumulateBug(
                            new BugInstance(this, "TESTING", result.getPriority())
                            .addClassAndMethod(methodGen, sourceFile)
                            .addString("Calling assertSame with two distinct objects")
                            .addFoundAndExpectedType(rhsType, lhsType)
                                    .addSomeSourceForTopTwoStackValues(classContext, method, location),
                            SourceLineAnnotation.fromVisitedInstruction(classContext, methodGen, sourceFile, handle));
                    else
                bugAccumulator.accumulateBug(
                        new BugInstance(this, "EC_UNRELATED_TYPES_USING_POINTER_EQUALITY", result.getPriority())
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