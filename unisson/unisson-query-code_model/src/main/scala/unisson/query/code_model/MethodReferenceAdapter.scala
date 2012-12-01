package unisson.query.code_model

import de.tud.cs.st.vespucci.interfaces.IMethodDeclaration
import sae.bytecode.model.MethodIdentifier


/**
 *
 * Author: Ralf Mitschke
 * Date: 11.12.11
 * Time: 13:13
 *
 */
class MethodReferenceAdapter(val element: MethodIdentifier)
        extends IMethodDeclaration with SourceElement[MethodIdentifier]
{

    def getPackageIdentifier = element.declaringRef.packageName

    def getSimpleClassName = element.declaringRef.simpleName

    def getReturnTypeQualifier = element.returnType.toJava

    lazy val getParameterTypeQualifiers = element.parameters.map(_.toJava).toArray

    def getLineNumber = -1

    def getMethodName = element.name

    override def hashCode() = element.hashCode()

    override def equals(obj: Any): Boolean = {
        if (obj.isInstanceOf[SourceElement[MethodIdentifier]]) {
            return element.equals(obj.asInstanceOf[SourceElement[MethodIdentifier]].element)
        }
        if( obj.isInstanceOf[IMethodDeclaration])
        {
            val other = obj.asInstanceOf[IMethodDeclaration]
            return this.getPackageIdentifier == other.getPackageIdentifier &&
                    this.getSimpleClassName == other.getSimpleClassName &&
                    this.getMethodName == other.getMethodName &&
                    this.getReturnTypeQualifier == other.getReturnTypeQualifier &&
                    java.util.Arrays.equals (getParameterTypeQualifiers.asInstanceOf[Array[Object]],other.getParameterTypeQualifiers.asInstanceOf[Array[Object]])
        }
        false
    }

    override def toString = element.declaringRef.toJava +
            element.name +
            "(" + (
            if (getParameterTypeQualifiers.isEmpty) {
                ""
            }
            else {
                getParameterTypeQualifiers.reduceLeft(_ + "," + _)
            }
            ) + ")" +
            ":" + element.returnType.toJava

    lazy val getSootIdentifier =
        "<" + element.declaringRef.toJava + ":" + element.returnType.toJava + " " + element.name +
                "(" +
                (
                        if (element.parameters.isEmpty) {
                            ""
                        }
                        else {
                            element.parameters.map(_.toJava).reduceLeft(_ + " " + _)
                        }
                        ) +
                ")" +
                ">"
}