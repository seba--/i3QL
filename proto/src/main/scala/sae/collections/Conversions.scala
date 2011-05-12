package sae
package collections

/**
 *
 */
object Conversions {

    def lazyViewToResult[V <: AnyRef](lazyView : LazyView[V]) : QueryResult[V] =
        lazyView match {
    		case col : Collection[V] => col
            case view : MaterializedView[V] => new MaterializedViewProxyResult(view)
            case _ => new BagResult(lazyView)
        }    
    
}