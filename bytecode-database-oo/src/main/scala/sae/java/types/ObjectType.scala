package sae.java.types

import sae.java.Package
import sae.java.members.{Field, Method}

/**
 * 
 * Author: Ralf Mitschke
 * Created: 26.08.11 10:01
 *
 */
trait ObjectType
    extends Type
{
    val Package : Package

    val Name : String

    var IsSourceAvailable : Boolean = false // this is class where we have the 'source code', i.e. bytecode

    var SuperType : Option[Class] = None // java.lang.Object will not have a super type, this must be evident to traverse the type hierarchy

    var Interfaces : Seq[Interface] = Nil // with Set[Interface]

    var Methods: Seq[Method] = Nil // with Set[Method]

    var Fields: Seq[Field] = Nil // with Set[Field]

    var InnerClasses: Seq[Class] = Nil // with Set[Class]
}