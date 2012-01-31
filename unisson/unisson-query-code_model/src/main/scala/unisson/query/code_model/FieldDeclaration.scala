package unisson.query.code_model

import sae.bytecode.model.FieldReference
import de.tud.cs.st.vespucci.interfaces.IFieldDeclaration

/**
 *
 * Author: Ralf Mitschke
 * Date: 12.12.11
 * Time: 12:48
 *
 */
class FieldDeclaration(val element: FieldReference)
        extends IFieldDeclaration with SourceElement[FieldReference]
{
    def getPackageIdentifier = element.declaringClass.packageName

    def getSimpleClassName = element.declaringClass.simpleName

    def getLineNumber = -1

    override def hashCode() = element.hashCode()

    override def equals(obj: Any) : Boolean = {
        if( !obj.isInstanceOf[FieldDeclaration] ){
            return false
        }
        element.equals(obj.asInstanceOf[FieldDeclaration].element)
    }


    def getFieldName = element.name

    def getTypeQualifier = element.fieldType.signature

    override def toString = element.declaringClass.signature +
            element.name +
            ":" + element.fieldType.signature

}