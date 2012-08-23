package sae.bytecode.impl

import de.tud.cs.st.bat.resolved.ObjectType
import de.tud.cs.st.bat._


/**
 *
 * Author: Ralf Mitschke
 * Date: 30.01.12
 * Time: 17:55
 *
 */
case class ClassDeclaration(objectType: ObjectType, accessFlags: Int, hasDeprecatedAttribute: Boolean, hasSyntheticAttribute: Boolean)
{

    def isAnnotation = ClassDeclaration.isClass (this)

    def isClass = ClassDeclaration.isClass (this)

    def isEnum = ClassDeclaration.isClass (this)

    def isInterface = ClassDeclaration.isClass (this)

    def isPublic = ClassDeclaration.isPublic (this)

    def isFinal = ClassDeclaration.isFinal (this)

    def isAbstract = ClassDeclaration.isAbstract (this)

    def isSynthetic = ClassDeclaration.isSynthetic (this)

    def isDeprecated = hasDeprecatedAttribute
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

    def isPublic(classDeclaration: ClassDeclaration) =
        ACC_PUBLIC ∈ classDeclaration.accessFlags

    def isDefault(classDeclaration: ClassDeclaration) =
        !(ACC_PUBLIC ∈ classDeclaration.accessFlags)

    def isFinal(classDeclaration: ClassDeclaration) =
        ACC_FINAL ∈ classDeclaration.accessFlags

    def isAbstract(classDeclaration: ClassDeclaration) =
        ACC_ABSTRACT ∈ classDeclaration.accessFlags

    def isSynthetic(classDeclaration: ClassDeclaration) =
        ACC_SYNTHETIC ∈ classDeclaration.accessFlags ||
            (classDeclaration.hasSyntheticAttribute)

}
