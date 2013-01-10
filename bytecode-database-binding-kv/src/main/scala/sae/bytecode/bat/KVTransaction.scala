package sae.bytecode.bat

import sae.bytecode.structure._
import internal.{UnresolvedEnclosingMethod, UnresolvedInnerClassEntry}
import com.sleepycat.je.{DatabaseEntry, Database}

/**
 *
 * @author Ralf Mitschke
 *
 */
class KVTransaction(database: Database)
    extends Transaction
{
    def begin() {
    }

    def commit() {
    }

    def rollback() {
    }

    def add(classDeclaration: ClassDeclaration) {
        val key = new DatabaseEntry (classDeclaration.classType.className.getBytes ("UTF-8"))
        // Now build the DatabaseEntry using a TupleBinding
        // Now store it
        database.put (null, key, key)
    }

    def remove(classDeclaration: ClassDeclaration) {
        val key = new DatabaseEntry (classDeclaration.classType.className.getBytes ("UTF-8"))
        database.delete (null, key)
    }

    def add(methodDeclaration: MethodDeclaration) {
        val key = new DatabaseEntry (methodDeclaration.toString.getBytes ("UTF-8"))
        database.put (null, key, key)
    }

    def remove(methodDeclaration: MethodDeclaration) {}

    def add(fieldDeclaration: FieldDeclaration) {
        val key = new DatabaseEntry (fieldDeclaration.toString.getBytes ("UTF-8"))
        database.put (null, key, key)
    }

    def remove(fieldDeclaration: FieldDeclaration) {}

    def add(codeInfo: CodeInfo) {

        var i = 0
        while (i < codeInfo.code.instructions.size) {
            val instruction = codeInfo.code.instructions (i)
            if (instruction != null) {
                val key = new DatabaseEntry ((codeInfo.declaringMethod.toString + i).getBytes ("UTF-8"))

                val data = new DatabaseEntry (instruction.toString.getBytes ("UTF-8"))
                database.put (null, key, data)
            }
            i += 1
        }

    }

    def remove(codeInfo: CodeInfo) {}

    def add(unresolvedInnerClass: UnresolvedInnerClassEntry) {}

    def remove(unresolvedInnerClass: UnresolvedInnerClassEntry) {}

    def add(unresolvedEnclosedMethod: UnresolvedEnclosingMethod) {}

    def remove(unresolvedEnclosedMethod: UnresolvedEnclosingMethod) {}
}