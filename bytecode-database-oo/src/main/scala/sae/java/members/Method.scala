package sae.java.members

import sae.java.types._

/**
 *  Methods can be either called on arrays or on objects.
 *  Thus the receiver is declared as ReferenceType
 */
case class Method(
                     DeclaringEntity: ObjectType,
                     Name: String,
                     Parameters: Seq[Type],
                     ReturnType: Type
                 )
{
    def IsSourceAvailable : Boolean = this.DeclaringEntity.IsSourceAvailable

    def isConstructor =
    {
        this.Name == "<init>"
    }

    def isStaticInitializer =
    {
        this.Name == "<clinit>"
    }
}