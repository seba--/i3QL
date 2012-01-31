package sae.bytecode.model

import de.tud.cs.st.bat.ObjectType


/**
 *
 * Author: Ralf Mitschke
 * Date: 30.01.12
 * Time: 17:55
 *
 */
case class ClassDeclaration(objectType: ObjectType, accessFlags: Int, isDeprecated: Boolean, isSynthetic: Boolean)
{

    lazy val isAnnotation = ClassDeclaration.isClass(this)

    lazy val isClass = ClassDeclaration.isClass(this)

    lazy val isEnum = ClassDeclaration.isClass(this)

    lazy val isInterface = ClassDeclaration.isClass(this)

    lazy val isPublic = ClassDeclaration.isPublic(this)

    lazy val isFinal = ClassDeclaration.isFinal(this)

    lazy val isAbstract = ClassDeclaration.isAbstract(this)
}

object ClassDeclaration
{

    import de.tud.cs.st.bat.constants.ACC_ANNOTATION
    import de.tud.cs.st.bat.constants.ACC_INTERFACE
    import de.tud.cs.st.bat.constants.ACC_ENUM
    import de.tud.cs.st.bat.constants.ACC_PUBLIC
    import de.tud.cs.st.bat.constants.ACC_FINAL
    import de.tud.cs.st.bat.constants.ACC_ABSTRACT


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

}
