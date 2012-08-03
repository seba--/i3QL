package sae
package syntax

import sae.operators._
import sae.operators.intern._
import sae.LazyView

object SQLSyntax
{

    import sae.collections.QueryResult

    // convenience forwarding to not always import conversion, but only the syntax
    implicit def lazyViewToResult[V <: AnyRef](lazyView: LazyView[V]): QueryResult[V] = sae.collections.Conversions
            .lazyViewToResult(
        lazyView
    )




    def *[A](a: A): A = identity(a)


    case class SELECT[Domain <: AnyRef, Range <: AnyRef](projection: Domain => Range)
    {
        def FROM(relation: LazyView[Domain]): LazyView[Range] = new BagProjection[Domain, Range](
            projection,
            relation
        )

        /*
        def FROM(relation: LazyView[Domain])(where : WHERE[Domain]): LazyView[Range] = new BagProjection[Domain, Range](
            projection,
            relation
        )
        */


    }

    case class FROM[Domain <: AnyRef](relation: LazyView[Domain]){

    }

    case class WHERE[Domain <: AnyRef](relation: LazyView[Domain]){

    }
}
