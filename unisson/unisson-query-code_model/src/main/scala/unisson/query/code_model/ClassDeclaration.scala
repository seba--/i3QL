package unisson.query.code_model

import de.tud.cs.st.bat.ObjectType
import de.tud.cs.st.vespucci.interfaces.IClassDeclaration

/**
 *
 * Author: Ralf Mitschke
 * Date: 11.12.11
 * Time: 13:05
 *
 */
class ClassDeclaration(val element: ObjectType)
        extends IClassDeclaration with SourceElement[ObjectType]
{
    def getPackageIdentifier = element.packageName

    def getTypeQualifier = element.signature

    def getSimpleClassName = element.simpleName

    def getLineNumber = -1

    override def hashCode() = element.hashCode()

    override def equals(obj: Any) : Boolean = {
        if( !obj.isInstanceOf[ClassDeclaration] ){
            return false
        }
        element.equals(obj.asInstanceOf[ClassDeclaration].element)
    }

    override def toString = getTypeQualifier
}