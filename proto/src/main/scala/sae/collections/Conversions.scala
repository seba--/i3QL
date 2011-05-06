package sae
package collections

/**
 *
 */
object Conversions {

    implicit def lazyViewToResult[V <: AnyRef](lazyView : LazyView[V]) : Result[V] =
        lazyView match {
        	case col : Collection[V] => col
            case view : MaterializedView[V] => new MaterializedViewResult(view)
            case _ => new BagResult(lazyView)
        }    
    
}