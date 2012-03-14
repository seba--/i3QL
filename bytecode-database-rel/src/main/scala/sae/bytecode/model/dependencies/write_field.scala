package sae.bytecode.model.dependencies

import sae.bytecode.model.{MethodDeclaration, FieldReference}


/**
 *
 * Author: Ralf Mitschke
 * Created: 22.05.11 13:56
 *
 */

case class write_field(source: MethodDeclaration, target: FieldReference, isStatic: Boolean)
        extends Dependency[MethodDeclaration, FieldReference]
{

}