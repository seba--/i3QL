package sae.bytecode.structure

import de.tud.cs.st.bat._

case class FieldDeclarationInfo(accessFlags: Int,
                     name: String,
                     fieldType: de.tud.cs.st.bat.resolved.FieldType,
                     hasDeprecatedAttribute: Boolean,
                     hasSyntheticAttribute: Boolean)
{
    def isPublic = FieldDeclarationInfo.isPublic (this)

    def isProtected = FieldDeclarationInfo.isProtected (this)

    def isPrivate = FieldDeclarationInfo.isPrivate (this)

    def isFinal = FieldDeclarationInfo.isFinal (this)

    def isStatic = FieldDeclarationInfo.isStatic (this)

    def isTransient = FieldDeclarationInfo.isTransient (this)

    def isVolatile = FieldDeclarationInfo.isVolatile (this)

    def isEnum = FieldDeclarationInfo.isEnum (this)

    def isDeprecated = hasDeprecatedAttribute

    def isSynthetic = FieldDeclarationInfo.isSynthetic(this)

}

object FieldDeclarationInfo
{


    def isPublic(fieldDeclaration: FieldDeclarationInfo) =
        ACC_PUBLIC ∈ fieldDeclaration.accessFlags


    def isProtected(fieldDeclaration: FieldDeclarationInfo) =
        ACC_PROTECTED ∈ fieldDeclaration.accessFlags


    def isPrivate(fieldDeclaration: FieldDeclarationInfo) =
        ACC_PRIVATE ∈ fieldDeclaration.accessFlags

    def isStatic(fieldDeclaration: FieldDeclarationInfo) =
        ACC_STATIC ∈ fieldDeclaration.accessFlags

    def isFinal(fieldDeclaration: FieldDeclarationInfo) =
        ACC_FINAL ∈ fieldDeclaration.accessFlags

    def isTransient(fieldDeclaration: FieldDeclarationInfo) =
        ACC_TRANSIENT ∈ fieldDeclaration.accessFlags

    def isVolatile(fieldDeclaration: FieldDeclarationInfo) =
        ACC_VOLATILE ∈ fieldDeclaration.accessFlags

    def isEnum(fieldDeclaration: FieldDeclarationInfo) =
        ACC_ENUM ∈ fieldDeclaration.accessFlags

    def isSynthetic(fieldDeclaration: FieldDeclarationInfo) =
        ACC_SYNTHETIC ∈ fieldDeclaration.accessFlags ||
            (fieldDeclaration.hasSyntheticAttribute)

}