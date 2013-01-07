package unisson.query.code_model

import de.tud.cs.st.bat.resolved.Type
import de.tud.cs.st.vespucci.interfaces.ICodeElement

/**
 *
 * Author: Ralf Mitschke
 * Date: 19.02.12
 * Time: 10:42
 *
 */
case class TypeReference(element: Type) extends ICodeElement
{
    def getPackageIdentifier = ""

    def getSimpleClassName = element.toJava

    def getSootIdentifier = element.toJava
}