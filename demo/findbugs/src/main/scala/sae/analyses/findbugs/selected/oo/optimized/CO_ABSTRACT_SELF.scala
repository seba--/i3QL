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

        // This is basically already the optimized version since we know that each subtype can be there only once
        // in general this would be an exists query
        /*
        SELECT ((md:ObjectType, o:ObjectType) => md) FROM(classesImplementCompareToWithoutObjectParameter, subTypesOfComparable) WHERE
                (_.declaringClass.isAbstract) AND
                (thisClass === thisClass)
        */
        // TODO optimization
        SELECT (*) FROM (coSelfBaseOpt) WHERE (_.isAbstract)
    }
}