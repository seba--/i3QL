package sae.bytecode

import java.io.InputStream
import sae.collections.{BagResult, SetResult}

/**
 *
 * Author: Ralf Mitschke
 * Author: Malte V
 * Created: 22.06.11 15:22
 *
 */

class MaterializedBytecodeDatabase(val database: BytecodeDatabase)
    extends BytecodeDatabase
{
    lazy val classDeclarations = new SetResult[ClassDeclaration](database.classDeclarations)

    lazy val instructions = new BagResult[InstructionInfo](database.instructions)

    lazy val methodDeclarations = new SetResult[MethodDeclaration](database.methodDeclarations)

    lazy val fieldDeclarations = new SetResult[FieldDeclaration](database.fieldDeclarations)

    lazy val classInheritance = new SetResult[InheritanceRelation](database.classInheritance)

    lazy val interfaceInheritance = new SetResult[InheritanceRelation](database.interfaceInheritance)

    def fieldReadInstructions = null

    def addClassFile(stream: InputStream) {
        database.addClassFile (stream)
    }

    def removeClassFile(stream: InputStream) {
        database.removeClassFile (stream)
    }

    def addArchive(stream: InputStream) {
        database.addArchive (stream)
    }

    def removeArchive(stream: InputStream) {
        database.removeArchive (stream)
    }


}