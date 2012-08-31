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
case class BATClassDeclaration(classType: ObjectType, accessFlags: Int, hasDeprecatedAttribute: Boolean, hasSyntheticAttribute: Boolean)
{

    def isAnnotation = BATClassDeclaration.isAnnotation (this)

    def isClass = BATClassDeclaration.isClass (this)

    def isEnum = BATClassDeclaration.isEnum (this)

    def isInterface = BATClassDeclaration.isInterface (this)

    def isPublic = BATClassDeclaration.isPublic (this)

    def isFinal = BATClassDeclaration.isFinal (this)

    def isAbstract = BATClassDeclaration.isAbstract (this)

    def isSynthetic = BATClassDeclaration.isSynthetic (this)

    def isDeprecated = hasDeprecatedAttribute
}

object BATClassDeclaration
{
    private val classCategoryMask: Int =
        ACC_INTERFACE.mask | ACC_ANNOTATION.mask | ACC_ENUM.mask

    private val annotationMask: Int =
        ACC_ANNOTATION.mask | ACC_INTERFACE.mask

    def isAnnotation(classDeclaration: BATClassDeclaration) =
        (classDeclaration.accessFlags & classCategoryMask) == annotationMask

    def isClass(classDeclaration: BATClassDeclaration) =
        (classDeclaration.accessFlags & classCategoryMask) == 0

    def isEnum(classDeclaration: BATClassDeclaration) =
        (classDeclaration.accessFlags & classCategoryMask) == ACC_ENUM.mask

    def isInterface(classDeclaration: BATClassDeclaration) =
        (classDeclaration.accessFlags & classCategoryMask) == ACC_INTERFACE.mask

    def isPublic(classDeclaration: BATClassDeclaration) =
        ACC_PUBLIC ∈ classDeclaration.accessFlags

    def isDefault(classDeclaration: BATClassDeclaration) =
        !(ACC_PUBLIC ∈ classDeclaration.accessFlags)

    def isFinal(classDeclaration: BATClassDeclaration) =
        ACC_FINAL ∈ classDeclaration.accessFlags

    def isAbstract(classDeclaration: BATClassDeclaration) =
        ACC_ABSTRACT ∈ classDeclaration.accessFlags

    def isSynthetic(classDeclaration: BATClassDeclaration) =
        ACC_SYNTHETIC ∈ classDeclaration.accessFlags ||
            (classDeclaration.hasSyntheticAttribute)

}
