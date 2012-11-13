package sae.analyses.findbugs.base.relational

import de.tud.cs.st.bat.resolved.ObjectType
import sae.syntax.sql._
import sae.bytecode.BytecodeDatabase
import sae.Relation
import sae.bytecode.structure.{InheritanceRelation, MethodDeclaration}

/**
 *
 * @author Ralf Mitschke
 *
 */
case class Definitions(database: BytecodeDatabase)
{

    import database._

    val cloneable = ObjectType ("java/lang/Cloneable")

    val subTypesOfCloneable: Relation[ObjectType] =
        SELECT ((_: InheritanceRelation).subType) FROM (inheritance) WHERE (_.superType == cloneable)

    val implementersOfClone: Relation[MethodDeclaration] =
        SELECT (*) FROM methodDeclarations WHERE
            (_.name == "clone") AND
            (_.parameterTypes == Nil) AND
            (_.returnType == ObjectType.Object)
}