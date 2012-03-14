package sae.bytecode.model

import de.tud.cs.st.bat.ObjectType


/**
 *
 * Author: Ralf Mitschke
 * Date: 31.01.12
 * Time: 16:43
 *
 */
class FieldDeclaration(
                              val declaringClass: ObjectType,
                              val name: String,
                              val fieldType: de.tud.cs.st.bat.FieldType,
                              val accessFlags: Int,
                              val isDeprecated: Boolean,
                              val isSynthetic: Boolean
                              )
        extends FieldIdentifier
{

    lazy val isPublic = FieldDeclaration.isPublic(this)

    lazy val isProtected = FieldDeclaration.isProtected(this)

    lazy val isPrivate = FieldDeclaration.isPrivate(this)

    lazy val isFinal = FieldDeclaration.isFinal(this)

    lazy val isStatic = FieldDeclaration.isStatic(this)

    lazy val isTransient = FieldDeclaration.isTransient(this)

    lazy val isVolatile = FieldDeclaration.isVolatile(this)

    lazy val isEnum = FieldDeclaration.isEnum(this)

    override def toString = "FieldDeclaration(" + declaringClass.toString + "," + name + "," + fieldType.toString + "," + (
            if (isPublic)
                "public"
            else if (isProtected)
                "protected"
            else if (isPrivate)
                "private"
            else
                "default") + ")"
}

object FieldDeclaration
{

    def apply(declaringClass: ObjectType,
              name: String,
              fieldType: de.tud.cs.st.bat.FieldType) =
        new FieldDeclaration(declaringClass, name, fieldType, 0, false, false)

    def apply(declaringClass: ObjectType,
              name: String,
              fieldType: de.tud.cs.st.bat.FieldType,
              accessFlags: Int,
              isDeprecated: Boolean,
              isSynthetic: Boolean) =
        new FieldDeclaration(declaringClass, name, fieldType, accessFlags, isDeprecated, isSynthetic)


    def unapply(fieldDeclaration: FieldDeclaration):
    Option[(ObjectType, String, de.tud.cs.st.bat.FieldType, Int, Boolean, Boolean)] =
        if (fieldDeclaration == null) None
        else Some(
            fieldDeclaration.declaringClass,
            fieldDeclaration.name,
            fieldDeclaration.fieldType,
            fieldDeclaration.accessFlags,
            fieldDeclaration.isDeprecated,
            fieldDeclaration.isSynthetic
        )

    import de.tud.cs.st.bat.constants.ACC_PUBLIC
    import de.tud.cs.st.bat.constants.ACC_PRIVATE
    import de.tud.cs.st.bat.constants.ACC_PROTECTED
    import de.tud.cs.st.bat.constants.ACC_FINAL
    import de.tud.cs.st.bat.constants.ACC_STATIC
    import de.tud.cs.st.bat.constants.ACC_TRANSIENT
    import de.tud.cs.st.bat.constants.ACC_VOLATILE
    import de.tud.cs.st.bat.constants.ACC_ENUM
    import de.tud.cs.st.bat.constants.Visibility


    def isPublic(fieldDeclaration: FieldDeclaration) =
        ACC_PUBLIC ∈ fieldDeclaration.accessFlags


    def isProtected(fieldDeclaration: FieldDeclaration) =
        ACC_PROTECTED ∈ fieldDeclaration.accessFlags


    def isPrivate(fieldDeclaration: FieldDeclaration) =
        ACC_PRIVATE ∈ fieldDeclaration.accessFlags


    def visibility(fieldDeclaration: FieldDeclaration) =
        if (isPublic(fieldDeclaration))
            Visibility.Public
        else if (isProtected(fieldDeclaration))
            Visibility.Protected
        else if (isPrivate(fieldDeclaration))
            Visibility.Private
        else
            Visibility.Default

    def isStatic(fieldDeclaration: FieldDeclaration) =
        ACC_STATIC ∈ fieldDeclaration.accessFlags

    def isFinal(fieldDeclaration: FieldDeclaration) =
        ACC_FINAL ∈ fieldDeclaration.accessFlags

    def isTransient(fieldDeclaration: FieldDeclaration) =
        ACC_TRANSIENT ∈ fieldDeclaration.accessFlags

    def isVolatile(fieldDeclaration: FieldDeclaration) =
        ACC_VOLATILE ∈ fieldDeclaration.accessFlags

    def isEnum(fieldDeclaration: FieldDeclaration) =
        ACC_ENUM ∈ fieldDeclaration.accessFlags

}