package sae.bytecode

/**
 *
 * Author: Ralf Mitschke
 * Date: 31.01.12
 * Time: 14:10
 *
 */
trait MethodIdentifier
{

    def declaringRef: de.tud.cs.st.bat.resolved.ReferenceType

    def name: String

    def parameters: Seq[de.tud.cs.st.bat.resolved.Type]

    def returnType: de.tud.cs.st.bat.resolved.Type

    def isConstructor = {
        name.startsWith ("<init>")
    }

    def isStaticInitializer = {
        name == "<clinit>"
    }

    override def hashCode(): Int = _hashCode

    private lazy val _hashCode: Int = {
        var code = "MethodIdentifier".hashCode ()
        code = code * 41 + (if (declaringRef == null) 0 else declaringRef.hashCode ())
        code = code * 41 + (if (name == null) 0 else name.hashCode ())
        code = code * 41 + (if (parameters == null) 0 else parameters.hashCode ())
        code = code * 41 + (if (returnType == null) 0 else returnType.hashCode ())
        code
    }

    override def equals(obj: Any): Boolean = {
        if (this eq obj.asInstanceOf[AnyRef])
            return true;
        if (!obj.isInstanceOf[MethodIdentifier])
            return false;
        val other = obj.asInstanceOf[MethodIdentifier]
        this.declaringRef == other.declaringRef &&
            this.name == other.name &&
            this.returnType == other.returnType &&
            this.parameters == other.parameters
    }
}