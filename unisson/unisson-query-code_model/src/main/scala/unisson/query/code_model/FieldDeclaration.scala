package unisson.query.code_model

import sae.bytecode.model.Field
import de.tud.cs.st.vespucci.interfaces.IFieldDeclaration

/**
 *
 * Author: Ralf Mitschke
 * Date: 12.12.11
 * Time: 12:48
 *
 */
class FieldDeclaration(val element: Field)
        extends IFieldDeclaration with SourceElement[Field]
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


    override def toString = element.declaringClass.signature +
            element.name +
            ":" + element.fieldType.signature

}