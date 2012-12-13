package sae.analyses.findbugs.selected.oo.optimized

import sae.bytecode._
import sae.Relation
import sae.syntax.sql._
import structure.ClassDeclaration
import sae.analyses.findbugs.base.oo.Definitions

/**
 *
 * @author Ralf Mitschke
 *
 */
object CO_ABSTRACT_SELF
        extends (BytecodeDatabase => Relation[ClassDeclaration])
{
    def apply(database: BytecodeDatabase): Relation[ClassDeclaration] = {
        val definitions = Definitions(database)
        import definitions._
        SELECT (*) FROM (coSelfBaseOpt) WHERE (_.isAbstract)
    }
}