package sae.analyses.findbugs.base.oo

import de.tud.cs.st.bat.resolved.{ObjectType, IntegerType}
import sae.syntax.sql._
import sae.Relation
import sae.bytecode._
import sae.bytecode.structure._
import sae.bytecode.instructions.FieldReadInstruction

/**
 *
 * @author Ralf Mitschke
 *
 */
case class Definitions(database: BytecodeDatabase)
{

    import database._


    lazy val serializable = ObjectType("java/io/Serializable")

    lazy val subTypesOfSerializable: Relation[ObjectType] =
        SELECT((_: InheritanceRelation).subType) FROM (subTypes) WHERE (_.superType == serializable)


    lazy val IllegalMonitorStateExceptionType = ObjectType("java/lang/IllegalMonitorStateException")

    lazy val cloneable = ObjectType("java/lang/Cloneable")

    lazy val subTypesOfCloneable: Relation[ObjectType] =
        SELECT((_: InheritanceRelation).subType) FROM (subTypes) WHERE (_.superType == cloneable)

    lazy val implementersOfClone: Relation[MethodDeclaration] =
        compile(
            SELECT(*) FROM methodDeclarations WHERE
                    (_.name == "clone") AND
                    (_.parameterTypes == Nil) AND
                    (_.returnType == ObjectType.Object)
        )

    val implementersOfCloneAsType: Relation[ObjectType] = compile(
        SELECT((_: MethodDeclaration).declaringClassType) FROM (implementersOfClone)
    )


    lazy val comparable = ObjectType("java/lang/Comparable")

    lazy val subTypesOfComparable: Relation[ObjectType] =
        SELECT((_: InheritanceRelation).subType) FROM (subTypes) WHERE (_.superType == comparable)

    lazy val implementersOfCompareToWithoutObjectParameter: Relation[MethodDeclaration] =
        SELECT(*) FROM methodDeclarations WHERE
                (_.name == "compareTo") AND
                NOT((_: MethodDeclaration).parameterTypes == Seq(ObjectType.Object)) AND
                (_.returnType == IntegerType)

    lazy val system = ObjectType("java/lang/System")

    lazy val runtime = ObjectType("java/lang/Runtime")

    lazy val privateFields: Relation[FieldDeclaration] =
        SELECT(*) FROM fieldDeclarations WHERE (_.isPrivate)

    lazy val hashTableType = ObjectType("java/util/Hashtable")

    def isHashTable: FieldDeclaration => Boolean = field => field.fieldType == hashTableType

    def isArray: FieldDeclaration => Boolean = field => field.fieldType.isArrayType

    lazy val fieldReadsFromExternalPackage: Relation[FieldReadInstruction] =
        SELECT(*) FROM readField WHERE (instruction =>
            instruction.declaringMethod.declaringClassType.packageName !=
                    instruction.receiverType.packageName)

    lazy val notInterfaces = compile(
        SELECT(*) FROM classDeclarations WHERE (!_.isInterface)
    )

    lazy val ms_fields: Relation[FieldDeclaration] =
        SELECT(*) FROM (fieldDeclarations) WHERE
                (_.isStatic) AND
                (!_.isSynthetic) AND
                (!_.isVolatile) AND
                (f => f.isProtected || f.isPublic) AND
                (!_.declaringClass.isInterface)


    lazy val ms_base: Relation[FieldDeclaration] =
        SELECT(*) FROM ms_fields WHERE NOT(
            EXISTS(
                SELECT(*) FROM fieldReadsFromExternalPackage WHERE
                        (((_: FieldReadInstruction).receiverType) === ((_: FieldDeclaration).declaringType)) AND
                        (((_: FieldReadInstruction).name) === ((_: FieldDeclaration).name)) AND
                        (((_: FieldReadInstruction).fieldType) === ((_: FieldDeclaration).fieldType))
            )
        )
}