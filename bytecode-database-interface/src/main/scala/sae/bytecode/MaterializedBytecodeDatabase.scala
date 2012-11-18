package sae.bytecode

import instructions._
import instructions.INVOKEINTERFACE
import instructions.INVOKESTATIC
import instructions.INVOKEVIRTUAL
import java.io.InputStream
import sae.collections.SetResult
import sae.bytecode.structure._
import sae.Relation
import structure.CodeAttribute
import structure.CodeInfo
import structure.FieldDeclaration
import structure.InheritanceRelation
import structure.MethodDeclaration
import de.tud.cs.st.bat.resolved.ObjectType

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

    lazy val methodDeclarations = new SetResult[MethodDeclaration](database.methodDeclarations)

    lazy val fieldDeclarations = new SetResult[FieldDeclaration](database.fieldDeclarations)

    lazy val typeDeclarations = new SetResult[ObjectType](database.typeDeclarations)

    lazy val methodDeclarationsMinimal = new SetResult[minimal.MethodDeclaration](database.methodDeclarationsMinimal)

    lazy val fieldDeclarationsMinimal = new SetResult[minimal.FieldDeclaration](database.fieldDeclarationsMinimal)

    lazy val classInheritance = new SetResult[InheritanceRelation](database.classInheritance)

    lazy val interfaceInheritance = new SetResult[InheritanceRelation](database.interfaceInheritance)

    lazy val code = new SetResult[CodeInfo](database.code)

    lazy val instructions = new SetResult[InstructionInfo](database.instructions)

    lazy val codeAttributes = new SetResult[CodeAttribute](database.codeAttributes)

    lazy val inheritance = new SetResult[InheritanceRelation](database.inheritance)

    lazy val subTypes = new SetResult[InheritanceRelation](database.subTypes)

    lazy val constructors: Relation[MethodDeclaration] = new SetResult[MethodDeclaration](database.constructors)

    lazy val invokeStatic: Relation[INVOKESTATIC] = new SetResult[INVOKESTATIC](database.invokeStatic)

    lazy val invokeVirtual: Relation[INVOKEVIRTUAL] = new SetResult[INVOKEVIRTUAL](database.invokeVirtual)

    lazy val invokeInterface: Relation[INVOKEINTERFACE] = new SetResult[INVOKEINTERFACE](database.invokeInterface)

    lazy val invokeSpecial: Relation[INVOKESPECIAL] = new SetResult[INVOKESPECIAL](database.invokeSpecial)

    lazy val readField = new SetResult[FieldReadInstruction](database.readField)

    lazy val getStatic = new SetResult[GETSTATIC](database.getStatic)

    lazy val getField = new SetResult[GETFIELD](database.getField)

    lazy val writeField = new SetResult[FieldWriteInstruction](database.writeField)

    lazy val putStatic = new SetResult[PUTSTATIC](database.putStatic)

    lazy val putField = new SetResult[PUTFIELD](database.putField)

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