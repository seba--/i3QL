package unisson.query.code_model

import de.tud.cs.st.bat.resolved.ObjectType
import de.tud.cs.st.vespucci.interfaces.IClassDeclaration

/**
 *
 * Author: Ralf Mitschke
 * Date: 11.12.11
 * Time: 13:05
 *
 */
class DirectClassTypeAdapter(val className: String)
    extends IClassDeclaration
{
    def this(element: ObjectType) = this (element.className)

    def getPackageIdentifier = ObjectType.packageName (className)

    def getSimpleClassName = ObjectType.simpleName (className)

    def getTypeQualifier = className.replace ('/', '.')

    def getLineNumber = -1

    override def hashCode() = className.hashCode * 43

    override def equals(obj: Any): Boolean = {
        if (obj.isInstanceOf[IClassDeclaration])
        {
            val other = obj.asInstanceOf[IClassDeclaration]
            return (getPackageIdentifier == other.getPackageIdentifier) && (getSimpleClassName == other.getSimpleClassName)
        }
        false
    }

    override def toString = getTypeQualifier
}