package sae.findbugs.analyses

import sae.bytecode.{BytecodeDatabase, Database}
import sae.Relation
import sae.syntax.RelationalAlgebraSyntax._
import sae.bytecode.model.{FieldIdentifier, MethodDeclaration, FieldDeclaration}
import sae.bytecode.model.dependencies.{write_field, Dependency}

/**
 *
 * Author: Ralf Mitschke
 * Date: 27.01.12
 * Time: 11:15
 * FINDBUGS: UuF: Unused field (UUF_UNUSED_FIELD)
 */
object UUF_UNUSED_FIELD
{

    def apply(database: BytecodeDatabase): Relation[FieldDeclaration] = {

        val privateFields = σ((_: FieldDeclaration).isPrivate)(database.declared_fields)
        (
                (
                        privateFields,
                        identity(_: FieldIdentifier)
                        ) ⊳(
                        (_: Dependency[MethodDeclaration, FieldIdentifier]).target,
                        database.read_field
                        )
                )
    }

}