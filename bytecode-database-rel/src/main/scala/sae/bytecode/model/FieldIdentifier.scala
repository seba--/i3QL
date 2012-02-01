package sae.bytecode.model


/**
 *
 * Author: Ralf Mitschke
 * Date: 31.01.12
 * Time: 16:43
 *
 */
trait FieldIdentifier
{
    def declaringClass: de.tud.cs.st.bat.ObjectType

    def name: String

    def fieldType: de.tud.cs.st.bat.FieldType

    override def hashCode(): Int = _hashCode

    private lazy val _hashCode: Int = {
        var code = "FieldIdentifier".hashCode()
        code = code * 41 + (if (declaringClass == null) 0 else declaringClass.hashCode())
        code = code * 41 + (if (name == null) 0 else name.hashCode())
        code = code * 41 + (if (fieldType == null) 0 else fieldType.hashCode())
        code
    }

    override def equals(obj: Any): Boolean = {
        if (this eq obj.asInstanceOf[AnyRef])
            return true;
        if (!obj.isInstanceOf[FieldIdentifier])
            return false;
        val other = obj.asInstanceOf[FieldIdentifier]
        this.declaringClass == other.declaringClass &&
                this.name == other.name &&
                this.fieldType == other.fieldType
    }
}