package unisson.query.code_model

import de.tud.cs.st.vespucci.interfaces.IMethodDeclaration
import sae.bytecode.structure.{MethodComparison, MethodInfo}
import de.tud.cs.st.bat.resolved.{FieldType, Type, ReferenceType, ObjectType}


/**
 *
 * Author: Ralf Mitschke
 * Date: 11.12.11
 * Time: 13:13
 *
 */
class DirectMethodInfoAdapter(val receiverType: ReferenceType,
                              val name: String,
                              val parameterTypes: Seq[FieldType],
                              val returnType: Type)
    extends IMethodDeclaration with MethodComparison
{

    def this(element: MethodInfo) = this (element.receiverType, element.name, element.parameterTypes, element.returnType)

    def getPackageIdentifier: String = {
        if (receiverType.isObjectType)
            receiverType.asInstanceOf[ObjectType].packageName
        else
            ""
    }

    def getSimpleClassName = {
        if (receiverType.isObjectType)
            receiverType.asInstanceOf[ObjectType].simpleName
        else
            receiverType.toJava
    }

    def getReturnTypeQualifier = returnType.toJava

    def getParameterTypeQualifiers = parameterTypes.map (_.toJava).toArray

    def getLineNumber = -1

    def getMethodName = name

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
        super.equals (obj)
    }

    override def toString = receiverType.toJava + "." +
        name +
        "(" + (
        if (getParameterTypeQualifiers.isEmpty) {
            ""
        }
        else
        {
            getParameterTypeQualifiers.reduceLeft (_ + "," + _)
        }
        ) + ")" +
        ":" + returnType.toJava
}