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
case class BATDeclaredClassInfo(minorVersion: Int,
                                majorVersion: Int,
                                accessFlags: Int,
                                classType: ObjectType)
{

    def isAnnotation = BATDeclaredClassInfo.isAnnotation (this)

    def isClass = BATDeclaredClassInfo.isClass (this)

    def isEnum = BATDeclaredClassInfo.isEnum (this)

    def isInterface = BATDeclaredClassInfo.isInterface (this)

    def isPublic = ACC_PUBLIC ∈ accessFlags

    def isDefault = !(ACC_PUBLIC ∈ accessFlags)

    def isFinal = ACC_FINAL ∈ accessFlags

    def isAbstract = ACC_ABSTRACT ∈ accessFlags

    def isSynthetic = ACC_SYNTHETIC ∈ accessFlags

}

object BATDeclaredClassInfo
{
    private val classCategoryMask: Int =
        ACC_INTERFACE.mask | ACC_ANNOTATION.mask | ACC_ENUM.mask

    private val annotationMask: Int =
        ACC_ANNOTATION.mask | ACC_INTERFACE.mask

    def isAnnotation(classDeclaration: BATDeclaredClassInfo) =
        (classDeclaration.accessFlags & classCategoryMask) == annotationMask

    def isClass(classDeclaration: BATDeclaredClassInfo) =
        (classDeclaration.accessFlags & classCategoryMask) == 0

    def isEnum(classDeclaration: BATDeclaredClassInfo) =
        (classDeclaration.accessFlags & classCategoryMask) == ACC_ENUM.mask

    def isInterface(classDeclaration: BATDeclaredClassInfo) =
        (classDeclaration.accessFlags & classCategoryMask) == ACC_INTERFACE.mask

}
