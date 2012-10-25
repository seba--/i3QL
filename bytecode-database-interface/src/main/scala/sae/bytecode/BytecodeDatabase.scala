package sae.bytecode


/**
 *
 * Author: Ralf Mitschke
 * Author: Malte V
 * Created: 22.06.11 15:22
 *
 */

trait BytecodeDatabase
    extends BytecodeBaseRelations
    with BytecodeDerivedRelations
    with BytecodeDatabaseManipulation
    with BytecodeCFG
{
    lazy val relations = Seq (
        classDeclarations,
        fieldDeclarations,
        methodDeclarations,
        classInheritance,
        interfaceInheritance,
        instructions
    ).view

}