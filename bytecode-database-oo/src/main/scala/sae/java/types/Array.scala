package sae.java.types

import sae.java.{Package, DefaultPackage}
import sae.java.members.{Field, Method}

/**
 * 
 * Author: Ralf Mitschke
 * Created: 25.08.11 22:33
 *
 */

final case class Array(Type : Type, Dimensions : Int)
    extends ObjectType
{
    override val Package : Package = DefaultPackage

    override val Name : String = "Array[" + Type.Name + "]" + "(" + Dimensions + ")"

    Methods :+ List(Method(this, "clone", List(), Object)) // with Set[MethodReference]
}
