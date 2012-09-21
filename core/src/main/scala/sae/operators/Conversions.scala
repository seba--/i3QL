package sae
package operators

import sae.collections.BagResult

/**
 *
 */
object Conversions
{

    // note rewrite to specialized operators makes sense, but may also
    // be irritating from a client perspective.
    // if you use viewX in a query and want to add elements later, then maybe viewX
    // was replaced inside a query and results do not get updated
    def lazyViewToMaterializedView[V <: AnyRef](lazyView: Relation[V]): OLDMaterializedView[V] =
        lazyView match {
            // case view : LazySelection[V]    => new OLDMaterializedSelection(view.filter, view.relation)
            // case view : BagProjection[_, V] => new OLDMaterializedBagProjection[view.Dom, V](view.projection, view.relation)
            case view: OLDMaterializedView[V] => view
            case _ => new BagResult(lazyView)
        }



    def lazyViewToIndexedView[V <: AnyRef](lazyView: Relation[V]): IndexedViewOLD[V] =
        lazyView match {
            case view: IndexedViewOLD[V] => view
            case view: OLDMaterializedView[V] => new HashIndexedViewProxyOLD(view)
            case _ => new HashIndexedViewProxyOLD(lazyViewToMaterializedView(lazyView))
        }

}