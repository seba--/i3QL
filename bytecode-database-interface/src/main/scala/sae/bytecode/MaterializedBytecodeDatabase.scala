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
    extends BytecodeBaseRelations
    with BytecodeDatabaseManipulation
{
    lazy val declared_classes = new SetResult[ClassDeclaration](database.declared_classes)

    lazy val declared_methods = new SetResult[MethodDeclaration](database.declared_methods)

    lazy val declared_fields = new SetResult[FieldDeclaration](database.declared_fields)

    lazy val instructions = new BagResult[InstructionInfo](database.instructions)

    // TODO move this is not a base relation
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