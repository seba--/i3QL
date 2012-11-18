package sae.analyses.findbugs.selected.relational

import sae.bytecode.structure.minimal._
import sae.syntax.sql._
import de.tud.cs.st.bat.resolved.ObjectType
import sae.Relation
import sae.analyses.findbugs.base.relational.Definitions
import sae.bytecode.BytecodeDatabase

/**
 *
 * @author Ralf Mitschke
 *
 *         TODO consider optimization together with CN_IDIOM
 *         TODO consider optimization together with CN_IDIOM_NO_SUPER_CALL
 */
object CN_IMPLEMENTS_CLONE_BUT_NOT_CLONEABLE
    extends (BytecodeDatabase => Relation[MethodDeclaration])
{
    def apply(database: BytecodeDatabase): Relation[MethodDeclaration] = {
        val definitions = Definitions (database)
        import definitions._

        SELECT (*) FROM
            (implementersOfClone) WHERE
            NOT (
                EXISTS (
                    SELECT (*) FROM (subTypesOfCloneable) WHERE (identity[ObjectType] _ === declaringType)
                )
            )
    }
}