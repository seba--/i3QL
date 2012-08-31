package sae.bytecode.structure

import de.tud.cs.st.bat._

case class BATDeclaredFieldInfo(accessFlags: Int,
                                name: String,
                                fieldType: de.tud.cs.st.bat.resolved.FieldType,
                                hasDeprecatedAttribute: Boolean,
                                hasSyntheticAttribute: Boolean)
{
    def isPublic = BATDeclaredFieldInfo.isPublic (this)

    def isProtected = BATDeclaredFieldInfo.isProtected (this)

    def isPrivate = BATDeclaredFieldInfo.isPrivate (this)

    def isFinal = BATDeclaredFieldInfo.isFinal (this)

    def isStatic = BATDeclaredFieldInfo.isStatic (this)

    def isTransient = BATDeclaredFieldInfo.isTransient (this)

    def isVolatile = BATDeclaredFieldInfo.isVolatile (this)

    def isEnum = BATDeclaredFieldInfo.isEnum (this)

    def isDeprecated = hasDeprecatedAttribute

    def isSynthetic = BATDeclaredFieldInfo.isSynthetic (this)

}

// TODO Inline this. It is an object due to some fields required for computation, which we wanted to be static, now not necessary
object BATDeclaredFieldInfo
{


    def isPublic(fieldDeclaration: BATDeclaredFieldInfo) =
        ACC_PUBLIC ∈ fieldDeclaration.accessFlags


    def isProtected(fieldDeclaration: BATDeclaredFieldInfo) =
        ACC_PROTECTED ∈ fieldDeclaration.accessFlags


    def isPrivate(fieldDeclaration: BATDeclaredFieldInfo) =
        ACC_PRIVATE ∈ fieldDeclaration.accessFlags

    def isStatic(fieldDeclaration: BATDeclaredFieldInfo) =
        ACC_STATIC ∈ fieldDeclaration.accessFlags

    def isFinal(fieldDeclaration: BATDeclaredFieldInfo) =
        ACC_FINAL ∈ fieldDeclaration.accessFlags

    def isTransient(fieldDeclaration: BATDeclaredFieldInfo) =
        ACC_TRANSIENT ∈ fieldDeclaration.accessFlags

    def isVolatile(fieldDeclaration: BATDeclaredFieldInfo) =
        ACC_VOLATILE ∈ fieldDeclaration.accessFlags

    def isEnum(fieldDeclaration: BATDeclaredFieldInfo) =
        ACC_ENUM ∈ fieldDeclaration.accessFlags

    def isSynthetic(fieldDeclaration: BATDeclaredFieldInfo) =
        ACC_SYNTHETIC ∈ fieldDeclaration.accessFlags ||
            (fieldDeclaration.hasSyntheticAttribute)

}