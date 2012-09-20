package sae.findbugs.analyses

import sae.bytecode.{BytecodeDatabase, Database}
import sae.Relation
import sae.bytecode.model.{FieldDeclaration, ClassDeclaration}
import sae.syntax.RelationalAlgebraSyntax._
import de.tud.cs.st.bat.ObjectType

/**
 *
 * Author: Ralf Mitschke
 * Date: 27.01.12
 * Time: 11:08
 *
 * CI: Class is final but declares protected field (CI_CONFUSED_INHERITANCE) // http://code.google.com/p/findbugs/source/browse/branches/2.0_gui_rework/findbugs/src/java/edu/umd/cs/findbugs/detect/ConfusedInheritance.java
 */
object CI_CONFUSED_INHERITANCE
{

    def apply(database: BytecodeDatabase): Relation[(ClassDeclaration,FieldDeclaration)] = {
        val finalClasses = σ((_: ClassDeclaration).isFinal)(database.declared_classes)
        val protectedFields = σ((_: FieldDeclaration).isProtected)(database.declared_fields)
        (
                (
                        finalClasses,
                        (_: ClassDeclaration).objectType
                        ) ⋈(
                        (_: FieldDeclaration).declaringClass,
                        protectedFields
                        )
                ) {
            (cd: ClassDeclaration, fd: FieldDeclaration) => (cd, fd)
        }
    }

}