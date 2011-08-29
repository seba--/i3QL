package sae.java.types

import sae.java.Package

/**
 *
 * Author: Ralf Mitschke
 * Created: 26.08.11 10:36
 *
 */
final case class Annotation(
                               Package: Package,
                               Name: String
                           )
        extends ObjectType
{

}