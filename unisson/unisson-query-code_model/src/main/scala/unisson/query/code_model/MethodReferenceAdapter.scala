package unisson.query.code_model

import de.tud.cs.st.vespucci.interfaces.IMethodDeclaration
import sae.bytecode.model.MethodReference


/**
 *
 * Author: Ralf Mitschke
 * Date: 11.12.11
 * Time: 13:13
 *
 */
class MethodReferenceAdapter(val element: MethodReference)
        extends IMethodDeclaration with SourceElement[MethodReference]
{

    def getPackageIdentifier = element.declaringRef.packageName

    def getSimpleClassName = element.declaringRef.simpleName

    def getReturnTypeQualifier = element.returnType.signature

    lazy val getParameterTypeQualifiers = element.parameters.map(_.signature).toArray

    def getLineNumber = -1

    def getMethodName = element.name

    override def hashCode() = element.hashCode()

    override def equals(obj: Any): Boolean = {
        if (!obj.isInstanceOf[MethodReferenceAdapter]) {
            return false
        }
        element.equals(obj.asInstanceOf[MethodReferenceAdapter].element)
    }

    override def toString = element.declaringRef.signature +
            element.name +
            "(" + (
            if (getParameterTypeQualifiers.isEmpty) {
                ""
            }
            else {
                getParameterTypeQualifiers.reduceLeft(_ + "," + _)
            }
            ) + ")" +
            ":" + element.returnType.signature
}