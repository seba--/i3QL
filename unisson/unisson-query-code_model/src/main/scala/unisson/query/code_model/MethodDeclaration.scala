package unisson.query.code_model

import de.tud.cs.st.vespucci.interfaces.{IMethodElement, ISourceCodeElement}
import sae.bytecode.model.Method
import collection.JavaConversions


/**
 *
 * Author: Ralf Mitschke
 * Date: 11.12.11
 * Time: 13:13
 *
 */
class MethodDeclaration(val element: Method)
        extends ISourceCodeElement with IMethodElement with SourceElement[Method]
{

    def getPackageIdentifier = element.declaringRef.packageName

    def getSimpleClassName = element.declaringRef.simpleName

    def getLineNumber = -1

    def getMethodName = element.name

    def getReturnType = element.returnType.signature

    lazy val getListParamTypes = JavaConversions.seqAsJavaList(element.parameters.map(_.signature))

    override def hashCode() = element.hashCode()

    override def equals(obj: Any) = element.equals(obj)
}