package sae.bytecode.structure

import de.tud.cs.st.bat.resolved.{ObjectType, Code}
/**
 * 
 * Author: Ralf Mitschke
 * Date: 30.10.12
 * Time: 14:23
 *
 */
case class CodeInfo(declaringMethod : MethodDeclaration, code : Code, exceptionTable: Seq[ObjectType]) {

}