package sae.bytecode

import instructions._
import java.io.InputStream
import sae.collections.SetResult
import sae.Relation
import structure.CodeAttribute
import structure.CodeInfo
import structure.ClassDeclaration
import structure.FieldDeclaration
import structure.InheritanceRelation
import structure.MethodDeclaration

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

    lazy val classDeclarationsMinimal = new SetResult[structure.minimal.ClassDeclaration](database.classDeclarationsMinimal)

    lazy val methodDeclarationsMinimal = new SetResult[structure.minimal.MethodDeclaration](database.methodDeclarationsMinimal)

    lazy val fieldDeclarationsMinimal = new SetResult[structure.minimal.FieldDeclaration](database.fieldDeclarationsMinimal)

    lazy val classInheritance = new SetResult[InheritanceRelation](database.classInheritance)

    lazy val interfaceInheritance = new SetResult[InheritanceRelation](database.interfaceInheritance)

    lazy val code = new SetResult[CodeInfo](database.code)

    lazy val codeMinimal = new SetResult[structure.minimal.CodeInfo](database.codeMinimal)

    lazy val codeAttributes = new SetResult[CodeAttribute](database.codeAttributes)

    lazy val inheritance = new SetResult[InheritanceRelation](database.inheritance)

    lazy val subTypes = new SetResult[InheritanceRelation](database.subTypes)

    lazy val constructors: Relation[MethodDeclaration] = new SetResult[MethodDeclaration](database.constructors)

    lazy val constructorsMinimal = new SetResult(database.constructorsMinimal)


    lazy val instructions = new SetResult[InstructionInfo](database.instructions)

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

    lazy val instructionsMinimal = new SetResult[minimal.InstructionInfo](database.instructionsMinimal)

    lazy val invokeStaticMinimal = new SetResult[minimal.INVOKESTATIC](database.invokeStaticMinimal)

    lazy val invokeVirtualMinimal = new SetResult[minimal.INVOKEVIRTUAL](database.invokeVirtualMinimal)

    lazy val invokeInterfaceMinimal = new SetResult[minimal.INVOKEINTERFACE](database.invokeInterfaceMinimal)

    lazy val invokeSpecialMinimal = new SetResult[minimal.INVOKESPECIAL](database.invokeSpecialMinimal)

    lazy val readFieldMinimal = new SetResult[minimal.FieldReadInstruction](database.readFieldMinimal)

    lazy val getStaticMinimal = new SetResult[minimal.GETSTATIC](database.getStaticMinimal)

    lazy val getFieldMinimal = new SetResult[minimal.GETFIELD](database.getFieldMinimal)

    lazy val writeFieldMinimal = new SetResult[minimal.FieldWriteInstruction](database.writeFieldMinimal)

    lazy val putStaticMinimal = new SetResult[minimal.PUTSTATIC](database.putStaticMinimal)

    lazy val putFieldMinimal = new SetResult[minimal.PUTFIELD](database.putFieldMinimal)


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