package sae.bytecode.impl

import de.tud.cs.st.bat._
import sae.bytecode.FieldIdentifier


/**
 *
 * Author: Ralf Mitschke
 * Date: 31.01.12
 * Time: 16:43
 *
 */
class FieldDeclaration(
                          val declaringClass: de.tud.cs.st.bat.resolved.ObjectType,
                          val name: String,
                          val fieldType: de.tud.cs.st.bat.resolved.FieldType,
                          val accessFlags: Int,
                          val isDeprecated: Boolean,
                          val isSynthetic: Boolean
                          )
    extends FieldIdentifier
{

    def isPublic = FieldDeclaration.isPublic (this)

    def isProtected = FieldDeclaration.isProtected (this)

    def isPrivate = FieldDeclaration.isPrivate (this)

    def isFinal = FieldDeclaration.isFinal (this)

    def isStatic = FieldDeclaration.isStatic (this)

    def isTransient = FieldDeclaration.isTransient (this)

    def isVolatile = FieldDeclaration.isVolatile (this)

    def isEnum = FieldDeclaration.isEnum (this)

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
/*
    def apply(declaringClass: ObjectType,
              name: String,
              fieldType: de.tud.cs.st.bat.FieldType) =
        new FieldDeclaration (declaringClass, name, fieldType, 0, false, false)

    def apply(declaringClass: ObjectType,
              name: String,
              fieldType: de.tud.cs.st.bat.FieldType,
              accessFlags: Int,
              isDeprecated: Boolean,
              isSynthetic: Boolean) =
        new FieldDeclaration (declaringClass, name, fieldType, accessFlags, isDeprecated, isSynthetic)


    def unapply(fieldDeclaration: FieldDeclaration):
    Option[(ObjectType, String, de.tud.cs.st.bat.FieldType, Int, Boolean, Boolean)] =
        if (fieldDeclaration == null) None
        else Some (
            fieldDeclaration.declaringClass,
            fieldDeclaration.name,
            fieldDeclaration.fieldType,
            fieldDeclaration.accessFlags,
            fieldDeclaration.isDeprecated,
            fieldDeclaration.isSynthetic
        )
*/

    def isPublic(fieldDeclaration: FieldDeclaration) =
        ACC_PUBLIC ∈ fieldDeclaration.accessFlags


    def isProtected(fieldDeclaration: FieldDeclaration) =
        ACC_PROTECTED ∈ fieldDeclaration.accessFlags


    def isPrivate(fieldDeclaration: FieldDeclaration) =
        ACC_PRIVATE ∈ fieldDeclaration.accessFlags

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