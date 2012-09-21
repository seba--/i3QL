package sae
package collections

/**
 *
 */
object Conversions {

    implicit def lazyViewToResult[V <: AnyRef](lazyView : Relation[V]) : QueryResult[V] =
        lazyView match {
    		case col : Collection[V] => col
            case view : OLDMaterializedView[V] => new OLDMaterializedViewProxyResult(view)
            case _ => new BagResult(lazyView)
        }    
 
}