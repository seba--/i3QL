package sae.findbugs.analyses

import sae.bytecode.BytecodeDatabase
import sae.Relation

/**
 *
 * Author: Ralf Mitschke
 * Date: 27.01.12
 * Time: 19:27
 *
 * FINDBUGS: FI: Finalizer should be protected, not public (FI_PUBLIC_SHOULD_BE_PROTECTED)
 */
object FI_PUBLIC_SHOULD_BE_PROTECTED
{

    /*
    def asOperators(database: BytecodeDatabase): Relation[ReferenceType] = {
        import sae.syntax.RelationalAlgebraSyntax._
        Π((_: MethodDeclaration).declaringRef)(
            σ(
                (m: MethodDeclaration) =>
                    (m.name == "finalize" && m.isPublic && m.returnType == VoidType() && m.parameters.isEmpty)
            )(database.declared_methods)
        )
    }
    */



    def apply(database: BytecodeDatabase): Relation[ReferenceType] = {
        import sae.syntax.sql._
        import database._
        //val a = SELECT(*) FROM declared_methods WHERE ((m: MethodDeclaration) => (m.name == "finalize" && m.isPublic && m.returnType == VoidType() && m.parameters.isEmpty))

        SELECT(declaringType) FROM declared_methods WHERE
                (_.name == "finalize") AND
                (_.isPublic) AND
                (_.returnType == void) AND
                (_.parameters.isEmpty)
    }
}