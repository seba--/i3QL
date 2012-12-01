package unisson.query.code_model

import de.tud.cs.st.vespucci.interfaces.ICodeElement
import de.tud.cs.st.bat.resolved.{ArrayType, ObjectType}

/**
 *
 * Author: Ralf Mitschke
 * Date: 23.01.12
 * Time: 13:59
 *
 */
class ArrayDeclaration (val element: ArrayType)
        extends ICodeElement with SourceElement[ArrayType]
{
    def getPackageIdentifier = element.packageName

    def getTypeQualifier = element.signature

    def getSimpleClassName = element.simpleName

    def getLineNumber = -1

    override def hashCode() = element.hashCode()

    override def equals(obj: Any) : Boolean = {
        if( !obj.isInstanceOf[ArrayDeclaration] ){
            return false
        }
        element.equals(obj.asInstanceOf[ArrayDeclaration].element)
    }

    override def toString = getTypeQualifier

    def getSootIdentifier = element.toJava
}