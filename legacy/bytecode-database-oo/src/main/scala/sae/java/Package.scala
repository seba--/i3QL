package sae.java

import types.ObjectType

/**
 *
 * Author: Ralf Mitschke
 * Created: 25.08.11 21:27
 *
 */

case class Package(Name: String)
{
    var Members: Seq[ObjectType] = Nil

}