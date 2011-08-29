package sae.java.types

import sae.java.Package
import sae.java.members.{Field, Method}

/**
 * 
 * Author: Ralf Mitschke
 * Created: 26.08.11 11:28
 *
 */

object Object
    extends ObjectType
{
    override val Package : Package = new Package("java.lang")

    override val Name : String = "Object"

}