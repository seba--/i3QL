package sae.analyses.findbugs.base.relational

import de.tud.cs.st.bat.resolved.{IntegerType, ObjectType}
import sae.syntax.sql._
import sae.bytecode.BytecodeDatabase
import sae.Relation
import sae.bytecode.structure.minimal._
import sae.bytecode.structure.InheritanceRelation

/**
 *
 * @author Ralf Mitschke
 *
 */
case class Definitions(database: BytecodeDatabase)
{

    import database._


    lazy val serializable = ObjectType ("java/io/Serializable")

    lazy val subTypesOfSerializable: Relation[ObjectType] =
        SELECT ((_: InheritanceRelation).subType) FROM (subTypes) WHERE (_.superType == serializable)


    lazy val IllegalMonitorStateExceptionType = ObjectType ("java/lang/IllegalMonitorStateException")

    lazy val cloneable = ObjectType ("java/lang/Cloneable")

    lazy val subTypesOfCloneable: Relation[ObjectType] =
        SELECT ((_: InheritanceRelation).subType) FROM (subTypes) WHERE (_.superType == cloneable)

    lazy val implementersOfClone: Relation[MethodDeclaration] =
        SELECT (*) FROM methodDeclarationsMinimal WHERE
            (_.name == "clone") AND
            (_.parameterTypes == Nil) AND
            (_.returnType == ObjectType.Object)

    lazy val comparable = ObjectType ("java/lang/Comparable")

    lazy val subTypesOfComparable: Relation[ObjectType] =
        SELECT ((_: InheritanceRelation).subType) FROM (subTypes) WHERE (_.superType == comparable)

    lazy val implementersOfCompareToWithoutObjectParameter: Relation[MethodDeclaration] =
        SELECT (*) FROM methodDeclarationsMinimal WHERE
            (_.name == "compareTo") AND
            NOT ((_: MethodDeclaration).parameterTypes == Seq (ObjectType.Object)) AND
            (_.returnType == IntegerType)

    lazy val system = ObjectType ("java/lang/System")

    lazy val runtime = ObjectType ("java/lang/Runtime")

    lazy val privateFields: Relation[FieldDeclaration] =
        SELECT (*) FROM fieldDeclarationsMinimal WHERE (_.isPrivate)
}