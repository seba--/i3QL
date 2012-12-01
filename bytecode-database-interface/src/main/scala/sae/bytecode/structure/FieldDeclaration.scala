package sae.bytecode.structure

class FieldDeclaration(val declaringClass: ClassDeclaration,
                       val accessFlags: Int,
                       val name: String,
                       val fieldType: de.tud.cs.st.bat.resolved.FieldType)
    extends DeclaredClassMember
    with FieldInfo
{
    def declaringType = declaringClass.classType

    import de.tud.cs.st.bat._

    def isPublic = ACC_PUBLIC ∈ accessFlags

    def isProtected = ACC_PROTECTED ∈ accessFlags

    def isPrivate = ACC_PRIVATE ∈ accessFlags

    def isStatic = ACC_STATIC ∈ accessFlags

    def isFinal = ACC_FINAL ∈ accessFlags

    def isTransient = ACC_TRANSIENT ∈ accessFlags

    def isVolatile = ACC_VOLATILE ∈ accessFlags

    def isEnum = ACC_ENUM ∈ accessFlags

    def isSynthetic = ACC_SYNTHETIC ∈ accessFlags

}