package unisson.query.code_model

import de.tud.cs.st.bat.Type

/**
 *
 * Author: Ralf Mitschke
 * Date: 19.02.12
 * Time: 10:42
 *
 */
case class TypeReference(element: Type) extends SourceElement[Type]
{
    def getPackageIdentifier = ""

    def getSimpleClassName = element.toJava

    def getSootIdentifier = element.toJava
}