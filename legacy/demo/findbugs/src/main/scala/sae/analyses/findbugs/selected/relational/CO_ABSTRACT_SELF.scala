package sae.analyses.findbugs.selected.relational

import sae.Relation
import sae.bytecode.structure.minimal._
import sae.analyses.findbugs.base.relational.Definitions
import sae.syntax.sql._
import de.tud.cs.st.bat.resolved.ObjectType
import sae.bytecode.BytecodeDatabase
/**
 *
 * @author Ralf Mitschke
 *
 */
object CO_ABSTRACT_SELF
    extends (BytecodeDatabase => Relation[MethodDeclaration])
{
    def apply(database: BytecodeDatabase): Relation[MethodDeclaration] = {
        import database._
        val definitions = Definitions (database)
        import definitions._


        // This is basically already the optimized version since we know that each subtype can be there only once
        // in general this would be an exists query

        /*
        val implementersOfCompareToWithoutObjectParameterWithAbstractClass: Relation[MethodDeclaration] =
            SELECT ((cd: ClassDeclaration, md: MethodDeclaration) => md) FROM (classDeclarationsMinimal, implementersOfCompareToWithoutObjectParameter) WHERE
                (_.isAbstract) AND
                (classType === declaringType)


        SELECT ((md: MethodDeclaration, o: ObjectType) => md) FROM (implementersOfCompareToWithoutObjectParameterWithAbstractClass, subTypesOfComparable) WHERE
            (declaringType === identity[ObjectType] _)
          */

        val co_self: Relation[MethodDeclaration] =
            SELECT ((md: MethodDeclaration, o: ObjectType) => md) FROM (implementersOfCompareToWithoutObjectParameter, subTypesOfComparable) WHERE
                (declaringType === identity[ObjectType] _)

        SELECT ((cd: ClassDeclaration, md: MethodDeclaration) => md) FROM (classDeclarationsMinimal, co_self) WHERE
            (_.isAbstract) AND
            (classType === declaringType)

        // TODO optimization
        // SELECT (*) FROM (CO_SELF_NO_OBJECT(database)) WHERE (_.declaringClass.isAbstract)
    }
}