package sae.analyses.findbugs.selected.relational

import sae.Relation
import sae.bytecode.structure.{FieldDeclaration, ClassDeclaration}
import sae.bytecode._
import sae.syntax.sql._

/**
 *
 * Author: Ralf Mitschke
 * Date: 27.01.12
 * Time: 11:08
 *
 * CI: Class is final but declares protected field (CI_CONFUSED_INHERITANCE) // http://code.google.com/p/findbugs/source/browse/branches/2.0_gui_rework/findbugs/src/java/edu/umd/cs/findbugs/detect/ConfusedInheritance.java
 */
object CI_CONFUSED_INHERITANCE
    extends (BytecodeDatabase => Relation[(ClassDeclaration, FieldDeclaration)])
{

    def apply(database: BytecodeDatabase): Relation[(ClassDeclaration, FieldDeclaration)] =
    {
        import database._
        SELECT ((cd: ClassDeclaration, fd: FieldDeclaration) => (cd, fd)) FROM
            (classDeclarations, fieldDeclarations) WHERE
            (classType === ((_: FieldDeclaration).declaringClassType)) AND
            (_.isFinal) AND
            ((_: FieldDeclaration).isProtected)
    }

}