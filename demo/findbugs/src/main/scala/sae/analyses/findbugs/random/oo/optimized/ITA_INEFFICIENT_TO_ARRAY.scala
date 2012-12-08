package sae.analyses.findbugs.random.oo.optimized

import sae.Relation
import sae.syntax.sql._
import sae.bytecode._
import instructions._
import de.tud.cs.st.bat.resolved.ArrayType
import instructions.ANEWARRAY
import instructions.ICONST_0
import structure.MethodDeclaration
import sae.operators.impl.{EquiJoinView, TransactionalEquiJoinView}
import sae.analyses.findbugs.AnalysesOO


/**
 *
 * Author: Ralf Mitschke
 * Date: 06.08.12
 * Time: 15:22
 *
 */
object ITA_INEFFICIENT_TO_ARRAY
    extends (BytecodeDatabase => Relation[InvokeInstruction])
{


    val objectArrayType = ArrayType (ClassType ("java/lang/Object"))

    val collectionInterface = ClassType ("java/util/Collection")

    val listInterface = ClassType ("java/util/List")

    def nextSequenceIndex: InstructionInfo => Int = _.sequenceIndex + 1

    def previousSequenceIndex: InstructionInfo => Int = _.sequenceIndex - 1

    def instructionIndex: InstructionInfo => (MethodDeclaration, Int) = instr => (instr.declaringMethod, instr.sequenceIndex)

    def nextInstructionIndex: InstructionInfo => (MethodDeclaration, Int) = instr => (instr.declaringMethod, instr.sequenceIndex + 1)

    def prevInstructionIndex: InstructionInfo => (MethodDeclaration, Int) = instr => (instr.declaringMethod, instr.sequenceIndex - 1)

    def apply(database: BytecodeDatabase): Relation[InvokeInstruction] = {
        import database._

        val iconst0: Relation[ICONST_0] = SELECT ((_: InstructionInfo).asInstanceOf[ICONST_0]) FROM instructions WHERE (_.isInstanceOf[ICONST_0])

        val anewarray: Relation[ANEWARRAY] = SELECT ((_: InstructionInfo).asInstanceOf[ANEWARRAY]) FROM instructions WHERE (_.isInstanceOf[ANEWARRAY])

        val newArray0 =
            if (AnalysesOO.transactional)
                new TransactionalEquiJoinView (
                    iconst0,
                    anewarray,
                    nextInstructionIndex,
                    instructionIndex,
                    (i: ICONST_0, a: ANEWARRAY) => a
                )

            else
                new EquiJoinView (
                    iconst0,
                    anewarray,
                    nextInstructionIndex,
                    instructionIndex,
                    (i: ICONST_0, a: ANEWARRAY) => a
                )



        val selectInvokes = compile (
            SELECT (*) FROM (invokeInterface.asInstanceOf[Relation[InvokeInstruction]]) WHERE
                (_.name == "toArray") AND
                (_.returnType == objectArrayType) AND
                (_.parameterTypes == Seq (objectArrayType)) UNION_ALL (
                SELECT (*) FROM (invokeVirtual.asInstanceOf[Relation[InvokeInstruction]]) WHERE
                    (_.name == "toArray") AND
                    (_.returnType == objectArrayType) AND
                    (_.parameterTypes == Seq (objectArrayType))
                )
        )

        val invokes: Relation[InvokeInstruction] =
            if (AnalysesOO.transactional)
                new TransactionalEquiJoinView (
                    selectInvokes,
                    newArray0,
                    prevInstructionIndex,
                    instructionIndex,
                    (i: InvokeInstruction, a: ANEWARRAY) => i
                )
            else
                new EquiJoinView (
                    selectInvokes,
                    newArray0,
                    prevInstructionIndex,
                    instructionIndex,
                    (i: InvokeInstruction, a: ANEWARRAY) => i
                )

        SELECT (*) FROM (invokes) WHERE EXISTS (
            SELECT (*) FROM subTypes WHERE
                (_.superType == collectionInterface) AND
                (subType === receiverType)
        )
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