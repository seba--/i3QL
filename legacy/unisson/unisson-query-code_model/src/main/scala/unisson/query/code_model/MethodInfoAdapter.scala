package unisson.query.code_model

import de.tud.cs.st.vespucci.interfaces.IMethodDeclaration
import sae.bytecode.structure.MethodInfo
import de.tud.cs.st.bat.resolved.ObjectType


/**
 *
 * Author: Ralf Mitschke
 * Date: 11.12.11
 * Time: 13:13
 *
 */
class MethodInfoAdapter(val element: MethodInfo)
    extends IMethodDeclaration
{

    def getPackageIdentifier: String = {
        if (element.receiverType.isObjectType)
            element.receiverType.asInstanceOf[ObjectType].packageName
        else
            ""
    }

    def getSimpleClassName = {
        if (element.receiverType.isObjectType)
            element.receiverType.asInstanceOf[ObjectType].simpleName
        else
            element.receiverType.toJava
    }

    def getReturnTypeQualifier = element.returnType.toJava

    def getParameterTypeQualifiers = element.parameterTypes.map (_.toJava).toArray

    def getLineNumber = -1

    def getMethodName = element.name

    override def hashCode() = element.hashCode ()

    override def equals(obj: Any): Boolean = {
        if (obj.isInstanceOf[IMethodDeclaration])
        {
            val other = obj.asInstanceOf[IMethodDeclaration]
            return this.getPackageIdentifier == other.getPackageIdentifier &&
                this.getSimpleClassName == other.getSimpleClassName &&
                this.getMethodName == other.getMethodName &&
                this.getReturnTypeQualifier == other.getReturnTypeQualifier &&
                java.util.Arrays.equals (getParameterTypeQualifiers.asInstanceOf[Array[Object]], other.getParameterTypeQualifiers.asInstanceOf[Array[Object]])
        }
        false
    }

    override def toString = element.receiverType.toJava +
        element.name +
        "(" + (
        if (getParameterTypeQualifiers.isEmpty) {
            ""
        }
        else
        {
            getParameterTypeQualifiers.reduceLeft (_ + "," + _)
        }
        ) + ")" +
        ":" + element.returnType.toJava

}