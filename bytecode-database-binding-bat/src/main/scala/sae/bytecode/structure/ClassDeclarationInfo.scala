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
case class ClassDeclarationInfo(classType: ObjectType, accessFlags: Int, hasDeprecatedAttribute: Boolean, hasSyntheticAttribute: Boolean)
{

    def isAnnotation = ClassDeclarationInfo.isAnnotation (this)

    def isClass = ClassDeclarationInfo.isClass (this)

    def isEnum = ClassDeclarationInfo.isEnum (this)

    def isInterface = ClassDeclarationInfo.isInterface (this)

    def isPublic = ClassDeclarationInfo.isPublic (this)

    def isFinal = ClassDeclarationInfo.isFinal (this)

    def isAbstract = ClassDeclarationInfo.isAbstract (this)

    def isSynthetic = ClassDeclarationInfo.isSynthetic (this)

    def isDeprecated = hasDeprecatedAttribute
}

object ClassDeclarationInfo
{
    private val classCategoryMask: Int =
        ACC_INTERFACE.mask | ACC_ANNOTATION.mask | ACC_ENUM.mask

    private val annotationMask: Int =
        ACC_ANNOTATION.mask | ACC_INTERFACE.mask

    def isAnnotation(classDeclaration: ClassDeclarationInfo) =
        (classDeclaration.accessFlags & classCategoryMask) == annotationMask

    def isClass(classDeclaration: ClassDeclarationInfo) =
        (classDeclaration.accessFlags & classCategoryMask) == 0

    def isEnum(classDeclaration: ClassDeclarationInfo) =
        (classDeclaration.accessFlags & classCategoryMask) == ACC_ENUM.mask

    def isInterface(classDeclaration: ClassDeclarationInfo) =
        (classDeclaration.accessFlags & classCategoryMask) == ACC_INTERFACE.mask

    def isPublic(classDeclaration: ClassDeclarationInfo) =
        ACC_PUBLIC ∈ classDeclaration.accessFlags

    def isDefault(classDeclaration: ClassDeclarationInfo) =
        !(ACC_PUBLIC ∈ classDeclaration.accessFlags)

    def isFinal(classDeclaration: ClassDeclarationInfo) =
        ACC_FINAL ∈ classDeclaration.accessFlags

    def isAbstract(classDeclaration: ClassDeclarationInfo) =
        ACC_ABSTRACT ∈ classDeclaration.accessFlags

    def isSynthetic(classDeclaration: ClassDeclarationInfo) =
        ACC_SYNTHETIC ∈ classDeclaration.accessFlags ||
            (classDeclaration.hasSyntheticAttribute)

}
