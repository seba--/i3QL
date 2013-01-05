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
        val aKey = classDeclaration.classType.className
        val theKey = new DatabaseEntry (aKey.getBytes ("UTF-8"))
        // Now build the DatabaseEntry using a TupleBinding
        /*
        val myLong = new Long(123456789l);
        val  theData = new DatabaseEntry();
        val myBinding = TupleBinding.getPrimitiveBinding(Long.class);
        myBinding.objectToEntry(myLong, theData);
        */
        // Now store it
        database.put (null, theKey, theKey)

    }

    def remove(classDeclaration: ClassDeclaration) {}

    def add(methodDeclaration: MethodDeclaration) {}

    def remove(methodDeclaration: MethodDeclaration) {}

    def add(fieldDeclaration: FieldDeclaration) {}

    def remove(fieldDeclaration: FieldDeclaration) {}

    def add(codeInfo: CodeInfo) {}

    def remove(codeInfo: CodeInfo) {}

    def add(unresolvedInnerClass: UnresolvedInnerClassEntry) {}

    def remove(unresolvedInnerClass: UnresolvedInnerClassEntry) {}

    def add(unresolvedEnclosedMethod: UnresolvedEnclosingMethod) {}

    def remove(unresolvedEnclosedMethod: UnresolvedEnclosingMethod) {}
}