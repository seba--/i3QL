package sae
package bytecode
package model

import de.tud.cs.st.bat.ReferenceType


/**
 *
 */
class MethodDeclaration(
                               val declaringRef: ReferenceType,
                               val name: String,
                               val parameters: Seq[de.tud.cs.st.bat.Type],
                               val returnType: de.tud.cs.st.bat.Type,
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

    lazy val isPublic = MethodDeclaration.isPublic(this)

    lazy val isProtected = MethodDeclaration.isProtected(this)

    lazy val isPrivate = MethodDeclaration.isPrivate(this)

    lazy val isStatic = MethodDeclaration.isStatic(this)

    lazy val isFinal = MethodDeclaration.isFinal(this)

    lazy val isSynchronized = MethodDeclaration.isSynchronized(this)

    lazy val isBridge = MethodDeclaration.isBridge(this)

    lazy val isVarArgs = MethodDeclaration.isVarArgs(this)

    lazy val isNative = MethodDeclaration.isNative(this)

    lazy val isAbstract = MethodDeclaration.isAbstract(this)

    lazy val isStrict = MethodDeclaration.isStrict(this)
}

object MethodDeclaration
{

    def apply(declaringRef: ReferenceType,
              name: String,
              parameters: Seq[de.tud.cs.st.bat.Type],
              returnType: de.tud.cs.st.bat.Type,
              accessFlags: Int,
              isDeprecated: Boolean,
              isSynthetic: Boolean
                     ) = new MethodDeclaration(declaringRef, name, parameters, returnType, accessFlags, isDeprecated, isSynthetic)

    /**
     * create a method declaration with default visibility that is neither deprecated nor synthetic
     */
    def apply(declaringRef: ReferenceType,
              name: String,
              parameters: Seq[de.tud.cs.st.bat.Type],
              returnType: de.tud.cs.st.bat.Type
                     ) = new MethodDeclaration(declaringRef, name, parameters, returnType, 0, false, false)

    def unapply(methodDeclaration: MethodDeclaration):
    Option[(ReferenceType, String, Seq[de.tud.cs.st.bat.Type], de.tud.cs.st.bat.Type, Int, Boolean, Boolean)] =
        if (methodDeclaration == null) None
        else Some(
            methodDeclaration.declaringRef,
            methodDeclaration.name,
            methodDeclaration.parameters,
            methodDeclaration.returnType,
            methodDeclaration.accessFlags,
            methodDeclaration.isDeprecated,
            methodDeclaration.isSynthetic
        )

    import de.tud.cs.st.bat.constants.ACC_PUBLIC
    import de.tud.cs.st.bat.constants.ACC_PRIVATE
    import de.tud.cs.st.bat.constants.ACC_PROTECTED
    import de.tud.cs.st.bat.constants.ACC_FINAL
    import de.tud.cs.st.bat.constants.ACC_STATIC
    import de.tud.cs.st.bat.constants.ACC_SYNCHRONIZED
    import de.tud.cs.st.bat.constants.ACC_BRIDGE
    import de.tud.cs.st.bat.constants.ACC_VARARGS
    import de.tud.cs.st.bat.constants.ACC_NATIVE
    import de.tud.cs.st.bat.constants.ACC_ABSTRACT
    import de.tud.cs.st.bat.constants.ACC_STRICT
    import de.tud.cs.st.bat.constants.ACC_SYNTHETIC
    import de.tud.cs.st.bat.constants.Visibility


    def isPublic(methodDeclaration: MethodDeclaration) =
        ACC_PUBLIC ∈ methodDeclaration.accessFlags

    def isProtected(methodDeclaration: MethodDeclaration) =
        ACC_PROTECTED ∈ methodDeclaration.accessFlags

    def isPrivate(methodDeclaration: MethodDeclaration) =
        ACC_PRIVATE ∈ methodDeclaration.accessFlags


    def visibility(methodDeclaration: MethodDeclaration) =
        if (isPublic(methodDeclaration))
            Visibility.Public
        else if (isProtected(methodDeclaration))
            Visibility.Protected
        else if (isPrivate(methodDeclaration))
            Visibility.Private
        else
            Visibility.Default

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