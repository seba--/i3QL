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
package sae.bytecode.bat

import sae.deltas.Update
import sae.bytecode.structure.{CodeInfo, FieldDeclaration, MethodDeclaration, ClassDeclaration}
import sae.bytecode.structure.internal.{UnresolvedInnerClassEntry, UnresolvedEnclosingMethod}
import collection.mutable

/**
 *
 * @author Ralf Mitschke
 *
 */

class HashSetTransaction
        extends Transaction
{
    var classDeclarationAdditions: mutable.HashSet[ClassDeclaration] = mutable.HashSet.empty

    var methodDeclarationAdditions: mutable.HashSet[MethodDeclaration] = mutable.HashSet.empty

    var fieldDeclarationAdditions: mutable.HashSet[FieldDeclaration] = mutable.HashSet.empty

    var codeAdditions: mutable.HashSet[CodeInfo] = mutable.HashSet.empty

    var classDeclarationDeletions: mutable.HashSet[ClassDeclaration] = mutable.HashSet.empty

    var methodDeclarationDeletions: mutable.HashSet[MethodDeclaration] = mutable.HashSet.empty

    var fieldDeclarationDeletions: mutable.HashSet[FieldDeclaration] = mutable.HashSet.empty

    var codeDeletions: mutable.HashSet[CodeInfo] = mutable.HashSet.empty

    var classDeclarationUpdates: mutable.HashSet[Update[ClassDeclaration]] = mutable.HashSet.empty

    var methodDeclarationUpdates: mutable.HashSet[Update[MethodDeclaration]] = mutable.HashSet.empty

    var fieldDeclarationUpdates: mutable.HashSet[Update[FieldDeclaration]] = mutable.HashSet.empty

    var codeUpdates: mutable.HashSet[Update[CodeInfo]] = mutable.HashSet.empty

    def invalidate() {
        classDeclarationAdditions = null
        methodDeclarationAdditions = null
        fieldDeclarationAdditions = null
        codeAdditions = null
        classDeclarationDeletions = null
        methodDeclarationDeletions = null
        fieldDeclarationDeletions = null
        codeDeletions = null
        classDeclarationUpdates = null
        methodDeclarationUpdates = null
        fieldDeclarationUpdates = null
        codeUpdates = null
    }

    private def removeEqualAddDeletes[E](otherSet: mutable.HashSet[E], element: E): Boolean = {
        if (otherSet.contains(element))
            otherSet.remove(element)
        else
            false
    }

    /*
        def classDeclarationsAsUpdates() {
            classDeclarationAdditions.foreach(added => {
                val other =
                    classDeclarationDeletions.find(deleted =>
                        added.classType == deleted.classType
                    )
                if (other.isDefined) {
                    classDeclarationUpdates.add(Update(other.get, added, 1, Nil))
                    classDeclarationDeletions.remove(other.get)
                }
            }
            )
            classDeclarationUpdates.foreach(update =>
                classDeclarationAdditions.remove(update.newV)
            )
        }

        def methodDeclarationsAsUpdates() {
            methodDeclarationAdditions.foreach(added => {
                val other =
                    methodDeclarationDeletions.find(deleted =>
                        added == deleted
                    )
                if (other.isDefined) {
                    methodDeclarationUpdates.add(Update(other.get, added, 1, Nil))
                    methodDeclarationDeletions.remove(other.get)
                }
            }
            )
            methodDeclarationUpdates.foreach(update =>
                methodDeclarationAdditions.remove(update.newV)
            )
        }

        def fieldDeclarationsAsUpdates() {
            fieldDeclarationAdditions.foreach(added => {
                val other =
                    fieldDeclarationDeletions.find(deleted =>
                        added == deleted
                    )
                if (other.isDefined) {
                    fieldDeclarationUpdates.add(Update(other.get, added, 1, Nil))
                    fieldDeclarationDeletions.remove(other.get)
                }
            }
            )
            fieldDeclarationUpdates.foreach(update =>
                fieldDeclarationAdditions.remove(update.newV)
            )
        }
    */

    def codesAsUpdates() {
        val (smaller, bigger) =
            if (codeAdditions.size < codeDeletions.size)
                (codeAdditions, codeDeletions)
            else
                (codeDeletions, codeAdditions)

        smaller.foreach(added => {
            val other =
                bigger.find(deleted =>
                    added.declaringMethod == deleted.declaringMethod
                )
            if (other.isDefined) {
                codeUpdates.add(Update(other.get, added, 1, Nil))
                bigger.remove(other.get)
            }
        }
        )
        codeUpdates.foreach(update =>
            smaller.remove(update.newV)
        )
        codeUpdates = codeUpdates.filter(update =>
            update.oldV.code.maxLocals != update.newV.code.maxLocals ||
                    update.oldV.code.maxStack != update.newV.code.maxStack ||
                    !update.oldV.code.instructions.sameElements(update.newV.code.instructions) ||
                    update.oldV.exceptionTable != update.newV.exceptionTable
        )
    }

    def add(classDeclaration: ClassDeclaration) {
        if (!removeEqualAddDeletes(classDeclarationDeletions, classDeclaration))
            classDeclarationAdditions.add(classDeclaration)
    }

    def remove(classDeclaration: ClassDeclaration) {
        if (!removeEqualAddDeletes(classDeclarationAdditions, classDeclaration))
            classDeclarationDeletions.add(classDeclaration)
    }

    def add(methodDeclaration: MethodDeclaration) {
        if (!removeEqualAddDeletes(methodDeclarationDeletions, methodDeclaration))
            methodDeclarationAdditions.add(methodDeclaration)
    }

    def remove(methodDeclaration: MethodDeclaration) {
        if (!removeEqualAddDeletes(methodDeclarationAdditions, methodDeclaration))
            methodDeclarationDeletions.add(methodDeclaration)
    }

    def add(fieldDeclaration: FieldDeclaration) {
        if (!removeEqualAddDeletes(fieldDeclarationDeletions, fieldDeclaration))
            fieldDeclarationAdditions.add(fieldDeclaration)
    }

    def remove(fieldDeclaration: FieldDeclaration) {
        if (!removeEqualAddDeletes(fieldDeclarationAdditions, fieldDeclaration))
            fieldDeclarationDeletions.add(fieldDeclaration)
    }

    def add(codeInfo: CodeInfo) {
        if (!removeEqualAddDeletes(codeDeletions, codeInfo)) {
            val hash = codeInfo.hashCode()
            codeAdditions.add(codeInfo)
        }
    }

    def remove(codeInfo: CodeInfo) {
        if (!removeEqualAddDeletes(codeAdditions, codeInfo)) {
            val hash = codeInfo.hashCode()
            codeDeletions.add(codeInfo)
        }
    }

    def add(unresolvedInnerClass: UnresolvedInnerClassEntry) {

    }

    def remove(unresolvedInnerClass: UnresolvedInnerClassEntry) {}

    def add(unresolvedEnclosedMethod: UnresolvedEnclosingMethod) {}

    def remove(unresolvedEnclosedMethod: UnresolvedEnclosingMethod) {}
}
