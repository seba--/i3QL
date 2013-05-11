package idb.package_types_error

import idb.package_types_error.traits._
import idb.package_types_error.typing.impl._


/**
 *
 * @author Ralf Mitschke
 */
package object typing
    extends ConcreteTypes
{

    def matching2 (i: Impl): T = i match {
        case MyImpl (t) => wrapped (t) // compiler error
        case _ => throw new UnsupportedOperationException
    }

}
