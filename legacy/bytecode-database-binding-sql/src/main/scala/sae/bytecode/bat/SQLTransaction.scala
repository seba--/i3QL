package sae.bytecode.bat

import sae.bytecode.structure._
import internal.{UnresolvedEnclosingMethod, UnresolvedInnerClassEntry}
import java.sql.{Statement, Connection}

/**
 *
 * @author Ralf Mitschke
 *
 */
class SQLTransaction(val connection: Connection)
    extends Transaction
{
    def begin() {
    }

    def commit() {
        connection.commit ()
    }

    def rollback() {
        connection.rollback ()
    }

    def add(classDeclaration: ClassDeclaration) {
        connection.createStatement ().execute ("INSERT HIGH_PRIORITY INTO bytecode.classes  (packageName, simpleName) VALUES ('" + classDeclaration.classType.packageName + "', '" + classDeclaration.classType.simpleName + "')", Statement.RETURN_GENERATED_KEYS)
        /*
        val stmt = connection.prepareStatement("INSERT INTO bytecode.classes  (packageName, simpleName) VALUES (?, ?)")
        stmt.setString(1, classDeclaration.classType.packageName)
        stmt.setString(2, classDeclaration.classType.simpleName)
        */
        //stmt.execute()
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