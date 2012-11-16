package sae.analyses.findbugs.base.oo

import de.tud.cs.st.bat.resolved.{ObjectType, IntegerType}
import sae.syntax.sql._
import sae.bytecode.BytecodeDatabase
import sae.Relation
import sae.bytecode.structure.{FieldDeclaration, InheritanceRelation, MethodDeclaration}

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
        SELECT ((_: InheritanceRelation).subType) FROM (inheritance) WHERE (_.superType == serializable)


    val IllegalMonitorStateExceptionType = ObjectType ("java/lang/IllegalMonitorStateException")

    val cloneable = ObjectType ("java/lang/Cloneable")

    val subTypesOfCloneable: Relation[ObjectType] =
        SELECT ((_: InheritanceRelation).subType) FROM (inheritance) WHERE (_.superType == cloneable)

    val implementersOfClone: Relation[MethodDeclaration] =
        SELECT (*) FROM methodDeclarations WHERE
            (_.name == "clone") AND
            (_.parameterTypes == Nil) AND
            (_.returnType == ObjectType.Object)

    val comparable = ObjectType ("java/lang/Comparable")

    val subTypesOfComparable: Relation[ObjectType] =
        SELECT ((_: InheritanceRelation).subType) FROM (inheritance) WHERE (_.superType == comparable)

    val implementersOfCompareToWithoutObjectParameter: Relation[MethodDeclaration] =
        SELECT (*) FROM methodDeclarations WHERE
            (_.name == "compareTo") AND
            NOT ((_: MethodDeclaration).parameterTypes == Seq (ObjectType.Object)) AND
            (_.returnType == IntegerType)

    val system = ObjectType ("java/lang/System")

    val runtime = ObjectType ("java/lang/Runtime")

    val privateFields: Relation[FieldDeclaration] =
        SELECT (*) FROM fieldDeclarations WHERE (_.isPrivate)
}