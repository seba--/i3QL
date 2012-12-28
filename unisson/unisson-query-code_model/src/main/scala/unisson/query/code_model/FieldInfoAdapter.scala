package unisson.query.code_model

import de.tud.cs.st.vespucci.interfaces.IFieldDeclaration
import sae.bytecode.structure.FieldInfo

/**
 *
 * Author: Ralf Mitschke
 * Date: 12.12.11
 * Time: 12:48
 *
 */
class FieldInfoAdapter(val element: FieldInfo)
    extends IFieldDeclaration with SourceElement[FieldInfo]
{
    def getPackageIdentifier = element.declaringType.packageName

    def getSimpleClassName = element.declaringType.simpleName

    def getLineNumber = -1

    override def hashCode() = element.hashCode ()

    override def equals(obj: Any): Boolean = {
        if (obj.isInstanceOf[SourceElement[FieldInfo]]) {
            return element.equals (obj.asInstanceOf[SourceElement[FieldInfo]].element)
        }
        if (obj.isInstanceOf[IFieldDeclaration]) {
            val other = obj.asInstanceOf[IFieldDeclaration]
            return this.getPackageIdentifier == other.getPackageIdentifier &&
                this.getSimpleClassName == other.getSimpleClassName &&
                this.getFieldName == other.getFieldName &&
                this.getTypeQualifier == other.getTypeQualifier
        }
        false
    }


    def getFieldName = element.name

    def getTypeQualifier = element.fieldType.toJava

    override def toString = element.declaringType.toJava +
        element.name +
        ":" + element.fieldType.toJava

}