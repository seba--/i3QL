package sae.analyses.findbugs.selected.oo.optimized

import sae.bytecode._
import sae.syntax.sql._
import de.tud.cs.st.bat.resolved.ObjectType
import sae.Relation
import sae.analyses.findbugs.base.oo.Definitions
import sae.operators.impl.NotExistsInSameDomainView


/**
 *
 * @author Ralf Mitschke
 *
 *         TODO consider optimization together with CN_IDIOM
 *         TODO consider optimization together with CN_IDIOM_NO_SUPER_CALL
 */
object CN_IMPLEMENTS_CLONE_BUT_NOT_CLONEABLE
    extends (BytecodeDatabase => Relation[ObjectType])
{
    def apply(database: BytecodeDatabase): Relation[ObjectType] = {
        val definitions = Definitions (database)
        import definitions._

        if (Definitions.existsOptimization)
            new NotExistsInSameDomainView[ObjectType](implementersOfCloneAsType.asMaterialized, subTypesOfCloneable
                .asMaterialized)
        else
            SELECT (*) FROM
                (implementersOfCloneAsType) WHERE
                NOT (
                    EXISTS (
                        SELECT (*) FROM (subTypesOfCloneable) WHERE (thisClass === thisClass)
                    )
                )

    }
}