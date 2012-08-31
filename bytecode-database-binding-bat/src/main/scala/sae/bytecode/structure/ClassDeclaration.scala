package sae.bytecode.structure

import de.tud.cs.st.bat.resolved.ObjectType
import de.tud.cs.st.bat._


/**
 *
 * Author: Ralf Mitschke
 * Date: 30.01.12
 * Time: 17:55
 *
 */
case class ClassDeclaration(minorVersion: Int,
                                majorVersion: Int,
                                accessFlags: Int,
                                classType: ObjectType)
{

    def isAnnotation = ClassDeclaration.isAnnotation (this)

    def isClass = ClassDeclaration.isClass (this)

    def isEnum = ClassDeclaration.isEnum (this)

    def isInterface = ClassDeclaration.isInterface (this)

    def isPublic = ACC_PUBLIC ∈ accessFlags

    def isDefault = !(ACC_PUBLIC ∈ accessFlags)

    def isFinal = ACC_FINAL ∈ accessFlags

    def isAbstract = ACC_ABSTRACT ∈ accessFlags

    def isSynthetic = ACC_SYNTHETIC ∈ accessFlags

}

object ClassDeclaration
{
    private val classCategoryMask: Int =
        ACC_INTERFACE.mask | ACC_ANNOTATION.mask | ACC_ENUM.mask

    private val annotationMask: Int =
        ACC_ANNOTATION.mask | ACC_INTERFACE.mask

    def isAnnotation(classDeclaration: ClassDeclaration) =
        (classDeclaration.accessFlags & classCategoryMask) == annotationMask

    def isClass(classDeclaration: ClassDeclaration) =
        (classDeclaration.accessFlags & classCategoryMask) == 0

    def isEnum(classDeclaration: ClassDeclaration) =
        (classDeclaration.accessFlags & classCategoryMask) == ACC_ENUM.mask

    def isInterface(classDeclaration: ClassDeclaration) =
        (classDeclaration.accessFlags & classCategoryMask) == ACC_INTERFACE.mask

}
