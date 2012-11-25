package sae.analyses.findbugs.base.relational

import de.tud.cs.st.bat.resolved.{IntegerType, ObjectType}
import sae.syntax.sql._
import sae.bytecode._
import instructions.minimal.FieldReadInstruction
import sae.bytecode.structure.minimal._
import sae.Relation
import structure.InheritanceRelation

/**
 *
 * @author Ralf Mitschke
 *
 */
case class Definitions(database: BytecodeDatabase)
{

    import database._


    val serializable = ObjectType ("java/io/Serializable")

    val subTypesOfSerializable: Relation[ObjectType] =
        SELECT ((_: InheritanceRelation).subType) FROM (subTypes) WHERE (_.superType == serializable)


    val IllegalMonitorStateExceptionType = ObjectType ("java/lang/IllegalMonitorStateException")

    val cloneable = ObjectType ("java/lang/Cloneable")

    val subTypesOfCloneable: Relation[ObjectType] =
        SELECT ((_: InheritanceRelation).subType) FROM (subTypes) WHERE (_.superType == cloneable)

    val implementersOfClone: Relation[MethodDeclaration] =
        SELECT (*) FROM methodDeclarationsMinimal WHERE
            (_.name == "clone") AND
            (_.parameterTypes == Nil) AND
            (_.returnType == ObjectType.Object)

    val comparable = ObjectType ("java/lang/Comparable")

    val subTypesOfComparable: Relation[ObjectType] =
        SELECT ((_: InheritanceRelation).subType) FROM (subTypes) WHERE (_.superType == comparable)

    val implementersOfCompareToWithoutObjectParameter: Relation[MethodDeclaration] =
        SELECT (*) FROM methodDeclarationsMinimal WHERE
            (_.name == "compareTo") AND
            NOT ((_: MethodDeclaration).parameterTypes == Seq (ObjectType.Object)) AND
            (_.returnType == IntegerType)

    val system = ObjectType ("java/lang/System")

    val runtime = ObjectType ("java/lang/Runtime")

    val privateFields: Relation[FieldDeclaration] =
        SELECT (*) FROM fieldDeclarationsMinimal WHERE (_.isPrivate)

    lazy val fieldReadsFromExternalPackage: Relation[FieldReadInstruction] =
        SELECT (*) FROM readField WHERE (instruction =>
            instruction.declaringMethod.declaringClassType.packageName !=
                instruction.receiverType.packageName)

    val hashTableType = ObjectType ("java/util/Hashtable")

    def isHashTable: FieldDeclaration => Boolean = field => field.fieldType == hashTableType

    def isArray: FieldDeclaration => Boolean = field => field.fieldType.isArrayType

    lazy val ms_base : Relation[FieldDeclaration] =
    SELECT (*) FROM (fieldDeclarations) WHERE
        (!_.declaringClass.isInterface) AND
        (_.isFinal) AND
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