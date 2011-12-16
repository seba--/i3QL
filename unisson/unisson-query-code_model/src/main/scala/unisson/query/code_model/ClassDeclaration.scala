package unisson.query.code_model

import de.tud.cs.st.bat.ObjectType
import de.tud.cs.st.vespucci.interfaces.ISourceCodeElement

/**
 *
 * Author: Ralf Mitschke
 * Date: 11.12.11
 * Time: 13:05
 *
 */
class ClassDeclaration(val element: ObjectType)
        extends ISourceCodeElement with SourceElement[ObjectType]
{
    def getPackageIdentifier = element.packageName

    def getSimpleClassName = element.simpleName

    def getSignature = element.signature

    def getLineNumber = -1

    override def hashCode() = element.hashCode()

    override def equals(obj: Any) = element.equals(obj)
}