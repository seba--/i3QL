package sae.analyses.findbugs.random.oo

/**
 *
 * Author: Ralf Mitschke
 * Date: 06.08.12
 * Time: 17:12
 *
 */
object DL_SYNCHRONIZATION_ON_BOXED_PRIMITIVE
{

    /*
    // RM: List of signatures that synchronization should not happen on
    badSignatures.addAll(Arrays.asList(new String[] { "Ljava/lang/Boolean;", "Ljava/lang/Double;", "Ljava/lang/Float;",
                "Ljava/lang/Byte;", "Ljava/lang/Character;", "Ljava/lang/Short;", "Ljava/lang/Integer;", "Ljava/lang/Long;" }));

    // RM the check for new or already instantiated objects:
    private static boolean newlyConstructedObject(OpcodeStack.Item item) {
        XMethod method = item.getReturnValueOf();   //RM: if this value is the return value of a method, give the method invoked
        if (method == null)
            return false;
        return method.getName().equals("<init>");
    }

    //RM: Note: pendingBug is only written in this method
    public void sawOpcode(int seen) {
        switch (seen) {
        case MONITORENTER:
            OpcodeStack.Item top = stack.getStackItem(0);

            if (pendingBug != null) {
                accumulateBug();
            }
            monitorEnterPC = getPC();

            syncSignature = top.getSignature();
            isSyncOnBoolean = false;
            Object constant = top.getConstant();
            if (syncSignature.equals("Ljava/lang/String;") && constant instanceof String) {

                pendingBug = new BugInstance(this, "DL_SYNCHRONIZATION_ON_SHARED_CONSTANT", NORMAL_PRIORITY)
                        .addClassAndMethod(this);

                String value = (String) constant;
                if (identified.matcher(value).matches())
                    pendingBug.addString(value).describe(StringAnnotation.STRING_CONSTANT_ROLE);

            } else if (badSignatures.contains(syncSignature)) {
                isSyncOnBoolean = syncSignature.equals("Ljava/lang/Boolean;");
                XField field = top.getXField();
                FieldSummary fieldSummary = AnalysisContext.currentAnalysisContext().getFieldSummary();
                OpcodeStack.Item summary = fieldSummary.getSummary(field);
                int priority = NORMAL_PRIORITY;
                if (isSyncOnBoolean)
                    priority--;
                if (newlyConstructedObject(summary))
                    pendingBug = new BugInstance(this, "DL_SYNCHRONIZATION_ON_UNSHARED_BOXED_PRIMITIVE", NORMAL_PRIORITY)
                            .addClassAndMethod(this).addType(syncSignature).addOptionalField(field)
                            .addOptionalLocalVariable(this, top);
                else if (isSyncOnBoolean)
                    pendingBug = new BugInstance(this, "DL_SYNCHRONIZATION_ON_BOOLEAN", priority).addClassAndMethod(this)
                            .addOptionalField(field).addOptionalLocalVariable(this, top);
                else
                    pendingBug = new BugInstance(this, "DL_SYNCHRONIZATION_ON_BOXED_PRIMITIVE", priority).addClassAndMethod(this)
                            .addType(syncSignature).addOptionalField(field).addOptionalLocalVariable(this, top);
            }
            break;
        case MONITOREXIT:

            accumulateBug();

            break;

        }
    }
     */

}