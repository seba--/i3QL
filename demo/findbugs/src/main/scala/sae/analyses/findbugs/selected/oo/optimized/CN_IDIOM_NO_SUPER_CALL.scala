package sae.analyses.findbugs.selected.oo.optimized

import sae.bytecode.BytecodeDatabase
import sae.Relation
import sae.bytecode.structure.{CodeInfo, MethodDeclaration}
import sae.syntax.sql._
import de.tud.cs.st.bat.resolved.{MethodDescriptor, INVOKESPECIAL, ObjectType}

/**
 *
 * @author Ralf Mitschke
 *
 *         TODO consider optimization together with CN_IDIOM
 *         TODO consider optimization together with CN_IMPLEMENTS_CLONE_BUT_NOT_CLONEABLE
 *
 *         TODO optimize THIS
 */
object CN_IDIOM_NO_SUPER_CALL
    extends (BytecodeDatabase => Relation[MethodDeclaration])
{

    def apply(database: BytecodeDatabase): Relation[MethodDeclaration] = {
        import database._
        SELECT ((_: CodeInfo).declaringMethod) FROM code WHERE
            NOT ((_: CodeInfo).declaringMethod.declaringClass.isInterface) AND
            NOT ((_: CodeInfo).declaringMethod.declaringClass.isAnnotation) AND
            (_.declaringMethod.name == "clone") AND
            (_.declaringMethod.parameterTypes == Nil) AND
            (_.declaringMethod.returnType == ObjectType.Object) AND
            NOT ((_: CodeInfo).declaringMethod.isAbstract) AND
            (_.declaringMethod.declaringClass.superClass.isDefined) AND
            NOT ((ci: CodeInfo) => {
                val superClass = ci.declaringMethod.declaringClass.superClass.get
                ci.code.instructions.exists ({
                    case INVOKESPECIAL (
                    `superClass`,
                    "clone",
                    MethodDescriptor (Nil, ObjectType.Object)
                    ) ⇒ true
                    case _ ⇒ false
                })
            })

    }

}