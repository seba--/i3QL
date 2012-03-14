package sae.findbugs.analyses

import sae.bytecode.Database
import sae.LazyView
import sae.bytecode.model.MethodDeclaration
import de.tud.cs.st.bat.{ReferenceType, VoidType}
import sae.syntax.RelationalAlgebraSyntax._

/**
 *
 * Author: Ralf Mitschke
 * Date: 27.01.12
 * Time: 19:27
 *
 *  FINDBUGS: FI: Finalizer should be protected, not public (FI_PUBLIC_SHOULD_BE_PROTECTED)
 */
object FI_PUBLIC_SHOULD_BE_PROTECTED
{

    def apply(database: Database): LazyView[ReferenceType] = {
        Π((_: MethodDeclaration).declaringRef)(
            σ(
                (m: MethodDeclaration) =>
                    (m.name == "finalize" && m.isPublic && m.returnType == VoidType() && m.parameters.isEmpty)
            )(database.declared_methods)
        )
    }

}