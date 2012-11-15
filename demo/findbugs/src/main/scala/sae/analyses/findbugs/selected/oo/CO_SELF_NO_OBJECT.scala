package sae.analyses.findbugs.selected.oo

import sae.bytecode._
import sae.Relation
import sae.bytecode.structure.MethodDeclaration
import sae.analyses.findbugs.base.oo.Definitions
import sae.syntax.sql._
import de.tud.cs.st.bat.resolved.ObjectType

/**
 *
 * @author Ralf Mitschke
 *
 */
object CO_SELF_NO_OBJECT extends (BytecodeDatabase => Relation[MethodDeclaration])
{
    def apply(database: BytecodeDatabase): Relation[MethodDeclaration] = {
        val definitions = Definitions (database)
        import definitions._

        // This is basically already the optimized version since we know that each subtype can be there only once
        // in general this would be an exists query
        SELECT ((md: MethodDeclaration, o: ObjectType) => md) FROM (implementersOfCompareToWithoutObjectParameter, subTypesOfComparable) WHERE
            (declaringType === identity[ObjectType] _)

    }

}