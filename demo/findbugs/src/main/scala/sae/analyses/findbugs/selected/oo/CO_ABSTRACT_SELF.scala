package sae.analyses.findbugs.selected.oo

import sae.bytecode._
import sae.Relation
import sae.syntax.sql._
import structure.MethodDeclaration
import sae.analyses.findbugs.base.oo.Definitions
import de.tud.cs.st.bat.resolved.ObjectType

/**
 *
 * @author Ralf Mitschke
 *
 */
object CO_ABSTRACT_SELF
        extends (BytecodeDatabase => Relation[MethodDeclaration])
{
    def apply(database: BytecodeDatabase): Relation[MethodDeclaration] = {
        val definitions = Definitions(database)
        import definitions._

        // This is basically already the optimized version since we know that each subtype can be there only once
        // in general this would be an exists query
        SELECT(*) FROM(implementersOfCompareToWithoutObjectParameter, subTypesOfComparable) WHERE
                (_.declaringClass.isAbstract)
                (declaringType === identity[ObjectType] _)

        // TODO optimization
        // SELECT (*) FROM (CO_SELF_NO_OBJECT(database)) WHERE (_.declaringClass.isAbstract)
    }
}