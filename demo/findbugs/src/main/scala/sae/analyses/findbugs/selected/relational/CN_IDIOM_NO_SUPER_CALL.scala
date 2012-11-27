package sae.analyses.findbugs.selected.relational

import sae.bytecode.BytecodeDatabase
import sae.Relation
import sae.bytecode.structure.MethodDeclaration
import sae.syntax.sql._
import de.tud.cs.st.bat.resolved.ObjectType

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
        import sae.bytecode._
        SELECT(*) FROM methodDeclarations WHERE
                //NOT((_: MethodDeclaration).declaringClass.isInterface) AND
                //NOT((_: MethodDeclaration).declaringClass.isAnnotation) AND
                NOT((_: MethodDeclaration).isAbstract) AND
                (_.name == "clone") AND
                (_.parameterTypes == Nil) AND
                (_.returnType == ObjectType.Object) AND
                (_.declaringClass.superClass.isDefined) AND
                NOT(
                    EXISTS(
                        SELECT(*) FROM invokeSpecial WHERE
                                (_.name == "clone") AND
                                (_.parameterTypes == Nil) AND
                                (_.returnType == ObjectType.Object) AND
                                (receiverType === ((_: MethodDeclaration).declaringClass.superClass.get)) AND
                                (declaringMethod === identity[MethodDeclaration] _)
                    )
                )

    }

}