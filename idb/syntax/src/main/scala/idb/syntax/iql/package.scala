package idb.syntax

import idb.syntax.iql.IR._
import scala.language.implicitConversions
import idb.syntax.iql.util.ClauseToAlgebra

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
    ): Rep[Domain] => Rep[(RangeA, RangeB)] =
        (x: Rep[Domain]) => (functions._1 (x), functions._2 (x))

    /*
    implicit def fun2AsFunTuple2[DomainA: Manifest, DomainB: Manifest, Range: Manifest] (
        function: (Rep[DomainA], Rep[DomainB]) => Rep[Range]
    ): Rep[((DomainA, DomainB)) => Range] =
        (x: Rep[(DomainA, DomainB)]) => function (x._1, x._2)
    */

    implicit def fun2[A1: Manifest, A2: Manifest, B: Manifest] (f: (Rep[A1], Rep[A2]) => Rep[B]): Rep[((A1, A2)) => B] =
        fun ((t: Rep[(A1, A2)]) => f (tuple2_get1 (t), tuple2_get2 (t)))

}
