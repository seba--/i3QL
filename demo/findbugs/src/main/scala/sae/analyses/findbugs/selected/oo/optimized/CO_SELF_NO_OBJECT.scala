package sae.analyses.findbugs.selected.oo.optimized

import sae.bytecode._
import sae.Relation
import structure.ClassDeclaration
import sae.analyses.findbugs.base.oo.Definitions
import sae.syntax.sql._

/**
 *
 * @author Ralf Mitschke
 *
 */
object CO_SELF_NO_OBJECT
    extends (BytecodeDatabase => Relation[ClassDeclaration])
{
    def apply(database: BytecodeDatabase): Relation[ClassDeclaration] = {
        val definitions = Definitions (database)
        import definitions._

        // This is basically already the optimized version since we know that each subtype can be there only once
        // in general this would be an exists query
        /*
        SELECT ((md: MethodDeclaration, o: ObjectType) => md) FROM (implementersOfCompareToWithoutObjectParameter, subTypesOfComparable) WHERE
            (declaringType === identity[ObjectType] _)
            */

        SELECT (*) FROM (coSelfBaseOpt)
    }

}