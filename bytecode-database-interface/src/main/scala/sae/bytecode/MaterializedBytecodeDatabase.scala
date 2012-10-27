package sae.bytecode

import instructions.InstructionInfo
import java.io.InputStream
import sae.collections.SetResult
import sae.bytecode.structure._

/**
 *
 * This database stores all base relations in respective data structures.
 * relations are stored immediately, hence lot of memory can be consumed.
 */
class MaterializedBytecodeDatabase(val database: BytecodeDatabase)
    extends BytecodeDatabase
{
    val classDeclarations = new SetResult[ClassDeclaration](database.classDeclarations)

    val instructions = new SetResult[InstructionInfo](database.instructions)

    val methodDeclarations = new SetResult[MethodDeclaration](database.methodDeclarations)

    val fieldDeclarations = new SetResult[FieldDeclaration](database.fieldDeclarations)

    val classInheritance = new SetResult[InheritanceRelation](database.classInheritance)

    val interfaceInheritance = new SetResult[InheritanceRelation](database.interfaceInheritance)

    val codeAttributes = new SetResult[CodeAttribute](database.codeAttributes)

    val inheritance = new SetResult[InheritanceRelation](database.inheritance)

    val subTypes = new SetResult[InheritanceRelation](database.subTypes)

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