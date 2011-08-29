package sae.java.members

import sae.java.types._

/**
 *  Methods can either called on arrays or on objects.
 *  Thus they are declared as ReferenceType
 */
case class Field(
                    DeclaringEntity: ObjectType,
                    Name: String,
                    Type: Type,
                    IsStatic: Boolean
                )
{
    def IsSourceAvailable : Boolean = this.DeclaringEntity.IsSourceAvailable
}