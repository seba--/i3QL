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
{

    def apply(database: BytecodeDatabase): LazyView[ReferenceType] = {
        import database._
        SELECT (declaringType) FROM declared_methods WHERE
            (_.name == "finalize") AND
            (_.isPublic) AND
            returnType === void AND
            parameters === Nil
    }
}