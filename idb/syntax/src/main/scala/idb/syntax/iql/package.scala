package idb.syntax

import idb.syntax.iql.IR._
import idb.syntax.iql.impl.ClauseToAlgebra

/**
 *
 * @author Ralf Mitschke
 *
 */
package object iql
{

    val * : STAR_KEYWORD = impl.StarKeyword

    implicit def extentToQuery[Domain] (ext: Extent[Domain])(
        implicit mDom: Manifest[Domain],
        mExt: Manifest[Extent[Domain]]
    ): Rep[Query[Domain]] = extent (ext)

    implicit def plan[Range: Manifest] (clause: IQL_QUERY[Range]): Rep[Query[Range]] =
        ClauseToAlgebra (clause)

}
