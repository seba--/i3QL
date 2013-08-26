package sae.bytecode.bat

import sae.bytecode.structure._
import internal.{UnresolvedEnclosingMethod, UnresolvedInnerClassEntry}

/**
 *
 * @author Ralf Mitschke
 *
 */
abstract class Transaction
{

    def add(classDeclaration: ClassDeclaration)

    def remove(classDeclaration: ClassDeclaration)

    def add(methodDeclaration: MethodDeclaration)

    def remove(methodDeclaration: MethodDeclaration)

    def add(fieldDeclaration: FieldDeclaration)

    def remove(fieldDeclaration: FieldDeclaration)

    def add(codeInfo: CodeInfo)

    def remove(codeInfo: CodeInfo)

    def add(unresolvedInnerClass: UnresolvedInnerClassEntry)

    def remove(unresolvedInnerClass: UnresolvedInnerClassEntry)

    def add(unresolvedEnclosedMethod: UnresolvedEnclosingMethod)

    def remove(unresolvedEnclosedMethod: UnresolvedEnclosingMethod)


}