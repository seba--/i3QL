package sae.analyses.findbugs.random.relational

import sae.Relation
import sae.syntax.sql._
import sae.bytecode._
import sae.bytecode.instructions._
import sae.bytecode.structure._

/**
 *
 * Author: Ralf Mitschke
 * Date: 06.08.12
 * Time: 16:01
 *
 */
object MS_SHOULD_BE_FINAL
    extends (BytecodeDatabase => Relation[FieldDeclaration])
{

    val hashTableType = ClassType ("java/util/Hashtable")

    def isHashTable: FieldDeclaration => Boolean = field => field.fieldType == hashTableType

    def isArray: FieldDeclaration => Boolean = field => field.fieldType.isArrayType


    def apply(database: BytecodeDatabase): Relation[FieldDeclaration] = {
        import database._

        val fieldReadsFromExternalPackage: Relation[FieldReadInstruction] =
            SELECT (*) FROM readField WHERE (instruction =>
                instruction.declaringMethod.declaringClassType.packageName !=
                    instruction.receiverType.packageName)

        SELECT (*) FROM (fieldDeclarations) WHERE
            (!_.declaringClass.isInterface) AND
            (!_.isFinal) AND
            (_.isStatic) AND
            (!_.isSynthetic) AND
            (!_.isVolatile) AND
            //NOT ((_: FieldDeclaration).isSynthetic) AND
            //NOT ((_: FieldDeclaration).isVolatile) AND
            //(((_: FieldDeclaration).isProtected) OR (_.isPublic)) AND
            (f => f.isProtected || f.isPublic) AND
            //(isArray OR isHashTable) AND
            (f => isArray (f) || isHashTable (f)) AND
            NOT (
                EXISTS (
                    SELECT (*) FROM fieldReadsFromExternalPackage WHERE
                        (((_: FieldReadInstruction).receiverType) === ((_: FieldDeclaration).declaringType)) AND
                        (((_: FieldReadInstruction).name) === ((_: FieldDeclaration).name)) AND
                        (((_: FieldReadInstruction).fieldType) === ((_: FieldDeclaration).fieldType))
                )
            )

    }

    //SELECT (*) FROM (fieldReadInstructions) WHERE

    /*
        def analyze(project: Project) = {
            val classFiles: Traversable[ClassFile] = project.classFiles
            // list of tuples in the form (packageName, FieldEntry)
            val readFieldsFromPackage = BaseAnalyses.readFields (classFiles)
                .map (entry => (entry._1._1.thisClass.packageName, entry._2))
            for (classFile ← classFiles if (!classFile.isInterfaceDeclaration);
                 val declaringClass = classFile.thisClass;
                 val packageName = declaringClass.packageName;
                 field@Field (_, name, fieldType, _) ← classFile.fields
                 if (field.isFinal &&
                     field.isStatic &&
                     !field.isSynthetic &&
                     !field.isVolatile &&
                     (field.isPublic || field.isProtected) &&
                     (isArray (field.fieldType) || isHashTable (field.fieldType)) &&
                     !readFieldsFromPackage.exists (entry => entry._2 == (declaringClass, name, fieldType) && entry._1 != packageName)
                     )
            ) yield
            {
                ("MS_PKGPROTECT", classFile.thisClass.toJava + "." + field.name + " : " + field.fieldType.toJava)
            }
        }
    */

    /**
     * ########  Code from FindBugs #########
     */
    /*
    //RM: fill the ousidePackage
        case GETSTATIC:
        case PUTSTATIC:

            XField xField = getXFieldOperand();
            if (xField == null) {
                break;
            }
            if (!interesting(xField)) {
                break;
            }

            boolean samePackage = packageName.equals(extractPackage(getClassConstantOperand()));
            if (!samePackage) {
                outsidePackage.add(xField);
            }

    //RM: the is Interesting Field used before filling outside package
    private boolean interesting(XField f) {
        if (!f.isPublic() && !f.isProtected()) {
            return false;
        }
        if (!f.isStatic() || f.isSynthetic() || f.isVolatile()) {
            return false;
        }
        boolean isHashtable = f.getSignature().equals("Ljava/util/Hashtable;");
        boolean isArray = f.getSignature().charAt(0) == '[';
        if (f.isFinal() && !(isArray || isHashtable)) {
            return false;
        }
        return true;
    }

     // RM: iterate over all fields and report a lot of errors based on heuristics
     for (XField f : seen) {

        boolean isFinal = f.isFinal();
        String className = f.getClassName();
        String fieldSig = f.getSignature();
        String fieldName = f.getName();
        boolean couldBeFinal = !isFinal && !notFinal.contains(f);
        boolean isPublic = f.isPublic();
        boolean couldBePackage = !outsidePackage.contains(f);
        boolean movedOutofInterface = false;

        try {
            XClass xClass = Global.getAnalysisCache().getClassAnalysis(XClass.class, f.getClassDescriptor());
            movedOutofInterface = couldBePackage && xClass.isInterface();
        } catch (CheckedAnalysisException e) {
            assert true;
        }
        boolean isHashtable = fieldSig.equals("Ljava/util/Hashtable;");
        boolean isArray = fieldSig.charAt(0) == '[' && unsafeValue.contains(f);
        boolean isReadAnywhere = readAnywhere.contains(f);

        if (isFinal && !isHashtable && !isArray) {
            continue;
        } else if (movedOutofInterface) {
            bugType = "MS_OOI_PKGPROTECT";
        } else if (couldBePackage && couldBeFinal && (isHashtable || isArray)) {
            bugType = "MS_FINAL_PKGPROTECT";
        } else if (couldBeFinal && !isHashtable && !isArray) {
            bugType = "MS_SHOULD_BE_FINAL";
            if (needsRefactoringToBeFinal.contains(f))
                bugType = "MS_SHOULD_BE_REFACTORED_TO_BE_FINAL";
            if (fieldName.equals(fieldName.toUpperCase()) || fieldSig.charAt(0) == 'L') {
                priority = HIGH_PRIORITY;
            }
        } else if (couldBePackage) {
            bugType = "MS_PKGPROTECT";
        ...

        BugInstance bug = new BugInstance(this, bugType, priority).addClass(className).addField(f);
        SourceLineAnnotation firstPC = firstFieldUse.get(f);
        if (firstPC != null) {
            bug.addSourceLine(firstPC);
        }
        bugReporter.reportBug(bug);
     */

}