package idb.syntax

import scala.language.implicitConversions
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

    implicit def funTuple2AsFun[Domain: Manifest, RangeA: Manifest, RangeB: Manifest] (
        functions: (Rep[Domain] => Rep[RangeA], Rep[Domain] => Rep[RangeB])
    ): Rep[Domain => (RangeA, RangeB)] =
        (x: Rep[Domain]) => make_tuple2(functions._1 (x), functions._2 (x))


}
