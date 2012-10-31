package sae.analyses.findbugs.selected.oo

import sae.bytecode.structure.MethodDeclaration
import javax.management.relation.Relation
import sae.bytecode._
import sae.syntax.sql._
import de.tud.cs.st.bat.resolved.ObjectType

/**
 *
 * @author Ralf Mitschke
 *
 */
object CN_IMPLEMENTS_CLONE_BUT_NOT_CLONEABLE
        extends (BytecodeDatabase => Relation[MethodDeclaration])
{
    def apply(database: BytecodeDatabase): Relation[MethodDeclaration] = {
        import database._
        import sae.analyses.findbugs.base.oo.Definitions._

        val subTypesOfCloneable = SELECT(*) FROM inheritance WHERE (_.superType == cloneable)

        SELECT(*) FROM
                (methodDeclarations) WHERE
                (_.name == "clone") AND
                (_.parameters == Nil) AND
                (_.returnType == ObjectType.Object) AND
                (_.declaringClass.classType == cloneable) AND
                NOT(
                    EXISTS(
                        SELECT(*) FROM subTypesOfCloneable WHERE subType == declaringClass
                    )
                )
    }
}