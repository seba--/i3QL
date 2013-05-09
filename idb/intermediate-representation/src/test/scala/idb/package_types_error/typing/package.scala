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

    type FinalT = T

    def matching (i: Impl): T = i match {
        case MyImpl (t) => wrapped (t)
    }

}
