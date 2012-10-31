package sae.analyses.findbugs.selected.oo

import sae.bytecode.BytecodeDatabase
import sae.Relation
import sae.bytecode.structure.{CodeInfo, MethodDeclaration}
import sae.syntax.sql._
import de.tud.cs.st.bat.resolved.{MethodDescriptor, INVOKESPECIAL, ObjectType}

/**
 *
 * @author Ralf Mitschke
 *
 */
class CN_IDIOM_NO_SUPER_CALL
        extends (BytecodeDatabase => Relation[MethodDeclaration])
{

    def apply(database: BytecodeDatabase): Relation[MethodDeclaration] = {
        import database._
        SELECT(_: CodeInfo).declaringMethod FROM
                code WHERE
                NOT(_.declaringMethod.declaringClass.isInterface) AND
                NOT(_.declaringMethod.declaringClass.isAnnotation) AND
                (_.declaringMethod.name == "clone") AND
                (_.declaringMethod.parameters == Nil) AND
                (_.declaringMethod.returnType == ObjectType.Object) AND
                NOT(_.code.instructions.exists({
                    case INVOKESPECIAL(`declaringMethod.declaringClass.superClass`,
                    "clone",
                    MethodDescriptor(Nil, ObjectType.Object)
                    ) ⇒ true
                    case _ ⇒ false
                }))

    }

}