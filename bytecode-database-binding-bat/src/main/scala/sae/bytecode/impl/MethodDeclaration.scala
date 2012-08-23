package sae.bytecode.impl

import de.tud.cs.st.bat._
import sae.bytecode.MethodIdentifier


/**
 *
 */
class MethodDeclaration(
                           val declaringRef: de.tud.cs.st.bat.resolved.ReferenceType,
                           val name: String,
                           val parameters: Seq[de.tud.cs.st.bat.resolved.Type],
                           val returnType: de.tud.cs.st.bat.resolved.Type,
                           val accessFlags: Int,
                           val isDeprecated: Boolean,
                           val isSynthetic: Boolean
                           )
    extends MethodIdentifier
{

    override def toString = "MethodDeclaration(" + declaringRef.toString + "," + name + "," + parameters
        .toString + "," + returnType.toString + "," + (
        if (isPublic)
            "public"
        else if (isProtected)
                 "protected"
        else if (isPrivate)
                 "private"
        else
            "default") + ")"

    def isPublic = MethodDeclaration.isPublic (this)

    def isProtected = MethodDeclaration.isProtected (this)

    def isPrivate = MethodDeclaration.isPrivate (this)

    def isStatic = MethodDeclaration.isStatic (this)

    def isFinal = MethodDeclaration.isFinal (this)

    def isSynchronized = MethodDeclaration.isSynchronized (this)

    def isBridge = MethodDeclaration.isBridge (this)

    def isVarArgs = MethodDeclaration.isVarArgs (this)

    def isNative = MethodDeclaration.isNative (this)

    def isAbstract = MethodDeclaration.isAbstract (this)

    def isStrict = MethodDeclaration.isStrict (this)
}

object MethodDeclaration
{
/*
    def apply(declaringRef: ReferenceType,
              name: String,
              parameters: Seq[de.tud.cs.st.bat.Type],
              returnType: de.tud.cs.st.bat.Type,
              accessFlags: Int,
              isDeprecated: Boolean,
              isSynthetic: Boolean
                 ) = new MethodDeclaration (declaringRef, name, parameters, returnType, accessFlags, isDeprecated, isSynthetic)

    /**
     * create a method declaration with default visibility that is neither deprecated nor synthetic
     */
    def apply(declaringRef: ReferenceType,
              name: String,
              parameters: Seq[de.tud.cs.st.bat.Type],
              returnType: de.tud.cs.st.bat.Type
                 ) = new MethodDeclaration (declaringRef, name, parameters, returnType, 0, false, false)

    def unapply(methodDeclaration: MethodDeclaration):
    Option[(ReferenceType, String, Seq[de.tud.cs.st.bat.Type], de.tud.cs.st.bat.Type, Int, Boolean, Boolean)] =
        if (methodDeclaration == null) None
        else Some (
            methodDeclaration.declaringRef,
            methodDeclaration.name,
            methodDeclaration.parameters,
            methodDeclaration.returnType,
            methodDeclaration.accessFlags,
            methodDeclaration.isDeprecated,
            methodDeclaration.isSynthetic
        )
*/


    def isPublic(methodDeclaration: MethodDeclaration) =
        ACC_PUBLIC ∈ methodDeclaration.accessFlags

    def isProtected(methodDeclaration: MethodDeclaration) =
        ACC_PROTECTED ∈ methodDeclaration.accessFlags

    def isPrivate(methodDeclaration: MethodDeclaration) =
        ACC_PRIVATE ∈ methodDeclaration.accessFlags

    def isStatic(methodDeclaration: MethodDeclaration) =
        ACC_STATIC ∈ methodDeclaration.accessFlags

    def isFinal(methodDeclaration: MethodDeclaration) =
        ACC_FINAL ∈ methodDeclaration.accessFlags

    def isSynchronized(methodDeclaration: MethodDeclaration) =
        ACC_SYNCHRONIZED ∈ methodDeclaration.accessFlags

    def isBridge(methodDeclaration: MethodDeclaration) =
        ACC_BRIDGE ∈ methodDeclaration.accessFlags

    def isVarArgs(methodDeclaration: MethodDeclaration) =
        ACC_VARARGS ∈ methodDeclaration.accessFlags

    def isNative(methodDeclaration: MethodDeclaration) =
        ACC_NATIVE ∈ methodDeclaration.accessFlags

    def isAbstract(methodDeclaration: MethodDeclaration) =
        ACC_ABSTRACT ∈ methodDeclaration.accessFlags

    def isStrict(methodDeclaration: MethodDeclaration) =
        ACC_STRICT ∈ methodDeclaration.accessFlags

}