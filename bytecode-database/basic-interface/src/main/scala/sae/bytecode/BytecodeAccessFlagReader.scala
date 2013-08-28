/* License (BSD Style License):
 *  Copyright (c) 2009, 2011
 *  Software Technology Group
 *  Department of Computer Science
 *  Technische Universität Darmstadt
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  - Neither the name of the Software Technology Group or Technische
 *    Universität Darmstadt nor the names of its contributors may be used to
 *    endorse or promote products derived from this software without specific
 *    prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 */
package sae.bytecode

/**
 *
 * @author Ralf Mitschke
 */
trait BytecodeAccessFlagReader
    extends BytecodeStructure
{

    import BytecodeAccessFlags._

    private def contains (access_flags: Int, mask: Int): Boolean = (access_flags & mask) != 0

    def isAnnotation (classDeclaration: ClassDeclaration) =
        (classDeclaration.accessFlags & ACC_CLASS_EXT) == ACC_ANNOTATION_EXT

    def isClass (classDeclaration: ClassDeclaration) =
        (classDeclaration.accessFlags & ACC_CLASS_EXT) == 0

    def isEnum (classDeclaration: ClassDeclaration) =
        (classDeclaration.accessFlags & ACC_CLASS_EXT) == ACC_ENUM

    def isInterface (classDeclaration: ClassDeclaration) =
        (classDeclaration.accessFlags & ACC_CLASS_EXT) == ACC_INTERFACE

    def isPublic (classDeclaration: ClassDeclaration): Boolean =
        contains (classDeclaration.accessFlags, ACC_PUBLIC)

    def isDefault (classDeclaration: ClassDeclaration): Boolean =
        !contains (classDeclaration.accessFlags, ACC_PUBLIC)

    def isFinal (classDeclaration: ClassDeclaration): Boolean =
        contains (classDeclaration.accessFlags, ACC_FINAL)

    def isAbstract (classDeclaration: ClassDeclaration): Boolean =
        contains (classDeclaration.accessFlags, ACC_ABSTRACT)

    def isSynthetic (classDeclaration: ClassDeclaration): Boolean =
        contains (classDeclaration.accessFlags, ACC_SYNTHETIC)

    def isPublic (declaredClassMember: DeclaredClassMember): Boolean =
        contains (declaredClassMember.accessFlags, ACC_PUBLIC)

    def isProtected (declaredClassMember: DeclaredClassMember): Boolean =
        contains (declaredClassMember.accessFlags, ACC_PROTECTED)

    def isPrivate (declaredClassMember: DeclaredClassMember): Boolean =
        contains (declaredClassMember.accessFlags, ACC_PRIVATE)

    def isStatic (declaredClassMember: DeclaredClassMember): Boolean =
        contains (declaredClassMember.accessFlags, ACC_STATIC)

    def isFinal (declaredClassMember: DeclaredClassMember): Boolean =
        contains (declaredClassMember.accessFlags, ACC_FINAL)

    def isSynchronized (methodDeclaration: MethodDeclaration): Boolean =
        contains (methodDeclaration.accessFlags, ACC_SYNCHRONIZED)

    def isBridge (methodDeclaration: MethodDeclaration): Boolean =
        contains (methodDeclaration.accessFlags, ACC_BRIDGE)

    def isVarArgs (methodDeclaration: MethodDeclaration): Boolean =
        contains (methodDeclaration.accessFlags, ACC_VARARGS)

    def isNative (methodDeclaration: MethodDeclaration): Boolean =
        contains (methodDeclaration.accessFlags, ACC_NATIVE)

    def isAbstract (methodDeclaration: MethodDeclaration): Boolean =
        contains (methodDeclaration.accessFlags, ACC_ABSTRACT)

    def isStrict (methodDeclaration: MethodDeclaration): Boolean =
        contains (methodDeclaration.accessFlags, ACC_STRICT)

    def isSynthetic (methodDeclaration: MethodDeclaration): Boolean =
        contains (methodDeclaration.accessFlags, ACC_SYNTHETIC)

    def isTransient (fieldDeclaration: FieldDeclaration): Boolean =
        contains (fieldDeclaration.accessFlags, ACC_TRANSIENT)

    def isVolatile (fieldDeclaration: FieldDeclaration): Boolean =
        contains (fieldDeclaration.accessFlags, ACC_VOLATILE)

    def isEnum (fieldDeclaration: FieldDeclaration): Boolean =
        contains (fieldDeclaration.accessFlags, ACC_ENUM)

    def isSynthetic (fieldDeclaration: FieldDeclaration): Boolean =
        contains (fieldDeclaration.accessFlags, ACC_SYNTHETIC)
}
