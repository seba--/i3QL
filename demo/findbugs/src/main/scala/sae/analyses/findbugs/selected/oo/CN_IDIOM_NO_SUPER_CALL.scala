package sae.analyses.findbugs.selected.oo

import sae.bytecode._
import sae.Relation
import sae.bytecode.structure.MethodDeclaration
import sae.syntax.sql._
import de.tud.cs.st.bat.resolved.ObjectType
import sae.analyses.findbugs.base.oo.Definitions

/**
 *
 * @author Ralf Mitschke
 *
 *         TODO consider optimization together with CN_IDIOM
 *         TODO consider optimization together with CN_IMPLEMENTS_CLONE_BUT_NOT_CLONEABLE
 */
object CN_IDIOM_NO_SUPER_CALL
        extends (BytecodeDatabase => Relation[MethodDeclaration])
{

    def apply(database: BytecodeDatabase): Relation[MethodDeclaration] = {
        import database._
        val definitions = Definitions(database)
        import definitions._
        SELECT(*) FROM implementersOfClone WHERE
                (!_.declaringClass.isInterface) AND
                (!_.declaringClass.isAnnotation) AND
                (!_.isAbstract) AND
                (_.declaringClass.superClass.isDefined) AND
                NOT(
                    EXISTS(
                        SELECT(*) FROM invokeSpecial WHERE
                                (_.name == "clone") AND
                                (_.parameterTypes == Nil) AND
                                (_.returnType == ObjectType.Object) AND
                                (declaringMethod === thisMethod) AND
                                (receiverType === declaringTypeSuperType)
                    )
                )
    }

}