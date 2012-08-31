package sae.bytecode.structure


case class BATDeclaredFieldInfo(declaringClass: BATDeclaredClassInfo,
                                accessFlags: Int,
                                name: String,
                                fieldType: de.tud.cs.st.bat.resolved.FieldType)
{

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