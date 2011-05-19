package sae
package collections

/**
 * A result is a kind of view that offers more convenience operators
 * for working with the underlying data.
 * The result does not need to store all data internally and is thus not a
 * materialized view. In particular if a result is required from a
 * materialized view, a simple Proxy is used.
 */
trait QueryResult[V <: AnyRef]
        extends View[V]
        with Size
        with SingletonValue[V]
        with Listable[V] {

}

/**
 * A result that materializes all data from the underlying relation into a bag
 */
class BagResult[V <: AnyRef](
    val relation : LazyView[V])
        extends QueryResult[V]
        with Bag[V]
        with Observer[V] {

    relation addObserver this

    def lazyInitialize : Unit = {
        relation.lazy_foreach(v =>
            add_element(v)
        )
    }

    def updated(oldV : V, newV : V) : Unit =
        {
            this -= oldV
            this += newV
        }

    def removed(v : V) : Unit =
        {
            this -= v
        }

    def added(v : V) : Unit =
        {
            this += v
        }

    // def toAst = "QueryResult( " + relation.toAst + " )"
}

/**
 * A result that uses the underlying relation knowing that it is already
 * materialized.
 */
class MaterializedViewProxyResult[V <: AnyRef](
    val relation : MaterializedView[V])
        extends QueryResult[V] {

    def foreach[T](f : (V) => T) = relation.foreach(f)

    def size : Int = relation.size

    def singletonValue : Option[V] = relation.singletonValue

    // def toAst = "QueryResult( " + relation.toAst + " )"
}
