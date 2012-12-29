package unisson.query.code_model

import de.tud.cs.st.vespucci.interfaces.ICodeElement
import de.tud.cs.st.bat.resolved.ArrayType

/**
 *
 * Author: Ralf Mitschke
 * Date: 23.01.12
 * Time: 13:59
 *
 */
class ArrayTypeAdapter (val element: ArrayType)
    extends ICodeElement with SourceElement[ArrayType]
{
    def getPackageIdentifier = ""

    def getTypeQualifier = element.toJava

    def getSimpleClassName = element.toJava

    def getLineNumber = -1

    override def hashCode() = element.hashCode ()

    override def equals(obj: Any): Boolean = {
        if (!obj.isInstanceOf[ArrayTypeAdapter]) {
            return false
        }
        element.equals (obj.asInstanceOf[ArrayTypeAdapter].element)
    }

    override def toString = getTypeQualifier

}