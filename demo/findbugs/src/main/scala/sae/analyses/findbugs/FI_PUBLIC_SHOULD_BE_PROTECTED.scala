package sae.analyses.findbugs

import sae.bytecode._
import sae.LazyView
import sae.syntax.sql._

/**
 *
 * Author: Ralf Mitschke
 * Date: 27.01.12
 * Time: 19:27
 *
 * FINDBUGS: FI: Finalizer should be protected, not isPublic (FI_PUBLIC_SHOULD_BE_PROTECTED)
 */
object FI_PUBLIC_SHOULD_BE_PROTECTED
    extends (BytecodeDatabase => LazyView[ClassDeclaration])
{

    def apply(database: BytecodeDatabase): LazyView[ClassDeclaration] = {
        import database._
        SELECT (declaringClass) FROM (methodDeclarations) WHERE
            (_.name == "finalize") AND
            (_.isPublic) AND
            (_.returnType == void) AND
            (_.parameterTypes == Nil)
    }

}