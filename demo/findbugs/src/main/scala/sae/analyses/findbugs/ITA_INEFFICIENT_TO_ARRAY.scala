package sae.analyses.findbugs

import sae.bytecode._
import sae.Relation
import sae.syntax.sql._
import sae.bytecode.instructions._
import sae.bytecode.structure._
import de.tud.cs.st.bat.resolved.ArrayType


/**
 *
 * Author: Ralf Mitschke
 * Date: 06.08.12
 * Time: 15:22
 *
 */
object ITA_INEFFICIENT_TO_ARRAY
//extends (BytecodeDatabase => Relation[InstructionInfo])
{


    val objectArrayType = ArrayType (ClassType ("java/lang/Object"))

    val collectionInterface = ClassType ("java/util/Collection")

    val listInterface = ClassType ("java/util/List")

    def apply(database: BytecodeDatabase): Relation[InstructionInfo] = {
        import database._

        val invokeInterface /*: Relation[INVOKEINTERFACE] */ = SELECT ((_: InstructionInfo).asInstanceOf[INVOKEINTERFACE]) FROM instructions WHERE (_.isInstanceOf[INVOKEINTERFACE])

        val invokeVirtual /*: Relation[INVOKEVIRTUAL] */ = SELECT ((_: InstructionInfo).asInstanceOf[INVOKEVIRTUAL]) FROM instructions WHERE (_.isInstanceOf[INVOKEVIRTUAL])

        val iconst0 /*: Relation[ICONST_0] */ = SELECT ((_: InstructionInfo).asInstanceOf[ICONST_0]) FROM instructions WHERE (_.isInstanceOf[ICONST_0])

        val anewarray /*: Relation[ANEWARRAY] */ = SELECT ((_: InstructionInfo).asInstanceOf[ANEWARRAY]) FROM instructions WHERE (_.isInstanceOf[ANEWARRAY])

        val newArray0 =
            SELECT ((i: ICONST_0, a: ANEWARRAY) => a) FROM (iconst0, anewarray) WHERE
                (declaringMethod === declaringMethod) AND
                (sequenceIndex === ((_: ANEWARRAY).sequenceIndex - 1))

/*
        SELECT ((i: INVOKEINTERFACE, a: ANEWARRAY) => i) FROM (invokeInterface, newArray0) WHERE
            (_.name == "toArray") AND
            (_.returnType == objectArrayType) AND
            (_.parameterTypes == List (objectArrayType)) AND
            (declaringMethod === declaringMethod) AND
            (sequenceIndex === ((_: ANEWARRAY).sequenceIndex + 1)) AND
            EXISTS (
                SELECT (*) FROM inheritance WHERE (_.superType == collectionInterface) AND
                    (((_: InheritanceRelation).subType) === ((_: INVOKEINTERFACE).receiverType))
            ) UNION
            SELECT ((i: INVOKEVIRTUAL, a: ANEWARRAY) => i) FROM (invokeVirtual, newArray0) WHERE
            (_.name == "toArray") AND
            (_.returnType == objectArrayType) AND
            (_.parameterTypes == List (objectArrayType)) AND
            (declaringMethod === declaringMethod) AND
            (sequenceIndex === ((_: ANEWARRAY).sequenceIndex + 1)) AND
            EXISTS (SELECT (*) FROM inheritance WHERE (_.superType == collectionInterface) AND ((_: InheritanceRelation).subType) === ((_: INVOKEINTERFACE).receiverType))
*/
        null
    }


    /**
     * ########  Code from FindBugs #########
     */
    /*

    // RM: setting of constant operand!
    else if (constantRefOperand instanceof ConstantCP) {
                        ConstantCP cp = (ConstantCP) constantRefOperand;
                        ConstantClass clazz = (ConstantClass) getConstantPool().getConstant(cp.getClassIndex());
                        classConstantOperand = getStringFromIndex(clazz.getNameIndex());
                        referencedClass = DescriptorFactory.createClassDescriptor(classConstantOperand);
                        referencedXClass = null;
                        ConstantNameAndType sig = (ConstantNameAndType) getConstantPool().getConstant(
                                cp.getNameAndTypeIndex());
                        nameConstantOperand = getStringFromIndex(sig.getNameIndex());
                        sigConstantOperand = getStringFromIndex(sig.getSignatureIndex());
                        refConstantOperand = null;
                    }

    public String getSigConstantOperand() {
        if (sigConstantOperand == NOT_AVAILABLE)
            throw new IllegalStateException("getSigConstantOperand called but value not available");
        return sigConstantOperand;
    }
    // RM: Report bug on heuristic based on seen invoke instructions
    public void sawOpcode(int seen) {
        if (DEBUG)
            System.out.println("State: " + state + "  Opcode: " + OPCODE_NAMES[seen]);

        switch (state) {
        case SEEN_NOTHING:
            if (seen == ICONST_0)
                state = SEEN_ICONST_0;
            break;

        case SEEN_ICONST_0:
            if (seen == ANEWARRAY) {
                state = SEEN_ANEWARRAY;
            } else
                state = SEEN_NOTHING;
            break;

        case SEEN_ANEWARRAY:
            if (((seen == INVOKEVIRTUAL) || (seen == INVOKEINTERFACE)) && (getNameConstantOperand().equals("toArray"))
                    && (getSigConstantOperand().equals("([Ljava/lang/Object;)[Ljava/lang/Object;"))) {
                try {
                    String clsName = getDottedClassConstantOperand();
                    JavaClass cls = Repository.lookupClass(clsName);
                    if (cls.implementationOf(collectionClass))
                        bugAccumulator.accumulateBug(
                                new BugInstance(this, "ITA_INEFFICIENT_TO_ARRAY", LOW_PRIORITY).addClassAndMethod(this), this);

                } catch (ClassNotFoundException cnfe) {
                    bugReporter.reportMissingClass(cnfe);
                }
            }
            state = SEEN_NOTHING;
            break;

        default:
            state = SEEN_NOTHING;
            break;
        }
    }
     */
}