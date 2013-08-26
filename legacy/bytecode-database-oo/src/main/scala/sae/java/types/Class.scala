package sae.java.types

import sae.java.Package

/**
 *
 * Author: Ralf Mitschke
 * Created: 25.08.11 21:26
 *
 */
final case class Class(
                    Package: Package,
                    Name: String
                )
extends ObjectType
{

}