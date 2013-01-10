package sae.analyses.metrics.base

import sae.bytecode._
import collection.mutable
import sae.syntax.sql._
import structure.MethodDeclaration
import DependencyFactory._
import sae.Relation
import de.tud.cs.st.bat.resolved.ObjectType

/**
 *
 * @author Ralf Mitschke
 *
 */

object Definitions
{

    val definitionsTable = mutable.HashMap.empty[BytecodeDatabase, Definitions]

    var shared: Boolean = false

    def apply(database: BytecodeDatabase): Definitions = {
        if (!shared)
            return new Definitions (database)

        definitionsTable.getOrElseUpdate (database, new Definitions (database))
    }
}


class Definitions(val database: BytecodeDatabase)
{

    def sourcePackage : Dependency => String = _.source.packageName

    def targetPackage : Dependency => String = _.target.packageName

    def source : Dependency => ObjectType = _.source

    def target : Dependency => ObjectType = _.target


    lazy val classTypeDependencies: Relation[Dependency] =
        compile (
            SELECT (extendsDependency) FROM database.classInheritance UNION_ALL (
                SELECT (implementsDependency) FROM database.interfaceInheritance
                ) UNION_ALL (
                SELECT (invokeInterfaceDependency) FROM database.invokeInterface WHERE (_.receiverType.isObjectType)
                ) UNION_ALL (
                SELECT (invokeSpecialDependency) FROM database.invokeSpecial WHERE (_.receiverType.isObjectType)
                ) UNION_ALL (
                SELECT (invokeVirtualDependency) FROM database.invokeVirtual WHERE (_.receiverType.isObjectType)
                ) UNION_ALL (
                SELECT (invokeStaticDependency) FROM database.invokeStatic WHERE (_.receiverType.isObjectType)
                ) UNION_ALL (
                SELECT (readFieldDependency) FROM database.readField
                ) UNION_ALL (
                SELECT (writeFieldDependency) FROM database.writeField
                ) UNION_ALL (
                SELECT (newObjectDependency) FROM database.newObject
                ) UNION_ALL (
                SELECT (checkCastDependency) FROM database.checkCast WHERE (_.instruction.referenceType.isObjectType)
                ) UNION_ALL (
                SELECT (exceptionDeclarationDependency) FROM database.exceptionDeclarations
                ) UNION_ALL (
                SELECT (fieldDeclarationDependency) FROM database.fieldDeclarations WHERE (_.fieldType.isObjectType)
                ) UNION_ALL (
                SELECT (parameterTypeDependency) FROM (
                    database.methodDeclarations,
                    (((_: MethodDeclaration).parameterTypes.filter (_.isObjectType)) IN database.methodDeclarations)
                    )
                ) UNION_ALL (
                SELECT (returnTypeDependency) FROM (database.methodDeclarations) WHERE
                    (_.returnType.isObjectType)
                )
        )


    lazy val crossPackageDependencies = compile (
        SELECT (*) FROM classTypeDependencies WHERE (_.isCrossPackage) AND (_.target != ObjectType.Object)
    )
}