package unisson.query.code_model

import de.tud.cs.st.vespucci.interfaces.ISourceCodeElement
import sae.bytecode.model.Field

/**
 *
 * Author: Ralf Mitschke
 * Date: 12.12.11
 * Time: 12:48
 *
 */
class FieldDeclaration(val element: Field)
        extends ISourceCodeElement with SourceElement[Field]
{
    def getPackageIdentifier = element.declaringClass.packageName

    def getSimpleClassName = element.declaringClass.simpleName

    def getLineNumber = -1

    override def hashCode() = element.hashCode()

    override def equals(obj: Any) = element.equals(obj)
}