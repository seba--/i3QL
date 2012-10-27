package sae.bytecode

import instructions.InstructionInfo
import java.io.InputStream
import sae.collections.SetResult
import sae.bytecode.structure._

/**
 *
 * This database stores all base relations in respective data structures.
 * However, the database is lazily initialized, hence only relations are stored
 * from the point on where they are required by clients.
 * All other relations are not materialized.
 */
class LazyMaterializedBytecodeDatabase(val database: BytecodeDatabase)
    extends BytecodeDatabase
{
    lazy val classDeclarations = new SetResult[ClassDeclaration](database.classDeclarations)

    lazy val instructions = new SetResult[InstructionInfo](database.instructions)

    lazy val methodDeclarations = new SetResult[MethodDeclaration](database.methodDeclarations)

    lazy val fieldDeclarations = new SetResult[FieldDeclaration](database.fieldDeclarations)

    lazy val classInheritance = new SetResult[InheritanceRelation](database.classInheritance)

    lazy val interfaceInheritance = new SetResult[InheritanceRelation](database.interfaceInheritance)

    lazy val codeAttributes = new SetResult[CodeAttribute](database.codeAttributes)

    lazy val inheritance = new SetResult[InheritanceRelation](database.inheritance)

    lazy val subTypes = new SetResult[InheritanceRelation](database.subTypes)

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