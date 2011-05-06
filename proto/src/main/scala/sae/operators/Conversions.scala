package sae
package operators

/**
 *
 */
object Conversions {

    implicit def lazyViewToMaterializedView[V <: AnyRef](lazyView : LazyView[V]) : MaterializedView[V] =
        lazyView match {
            case view : LazySelection[V]    => new MaterializedSelection(view.filter, view.relation)
            case view : BagProjection[_, V] => new MaterializedBagProjection[view.Dom, V](view.projection, view.relation)
            case view : MaterializedView[V] => view
            case _                          => throw new IllegalArgumentException("could not convert " + lazyView + " to a materialized view")
        }

}