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
        extends MaterializedView[V]
                with Size
                with SingletonValue[V]
                with Listable[V]
{

}

/**
 * A result that materializes all data from the underlying relation into a bag
 */
class BagResult[V <: AnyRef](
                                val relation: LazyView[V]
                            )
        extends QueryResult[V]
                with Bag[V]
                with Observer[V]
{

    relation addObserver this

    def lazyInitialize
    {
        relation.lazy_foreach(
                v =>
                add_element(v)
        )
    }

    def updated(oldV: V, newV: V)
    {
        if (!initialized) {
            initialized = true
        }
        update(oldV, newV)
    }

    def removed(v: V)
    {
        if (!initialized) {
            initialized = true
        }

        this -= v
    }

    def added(v: V)
    {
        if (!initialized) {
            initialized = true
        }
        this += v
    }

    // def toAst = "QueryResult( " + relation.toAst + " )"
}

/**
 * A result that uses the underlying relation knowing that it is already
 * materialized.
 */
class MaterializedViewProxyResult[V <: AnyRef](
                                                  val relation: MaterializedView[V]
                                              )
        extends QueryResult[V]
{

    def lazyInitialize
    {
        // the relation will initialize itself on calls to the materialized_* methods
    }

    protected def materialized_contains(v: V) = relation.contains(v)

    protected def materialized_singletonValue = relation.singletonValue

    protected def materialized_size = relation.size

    protected def materialized_foreach[T](f: (V) => T)
    {
        relation.foreach(f)
    }

    // def toAst = "QueryResult( " + relation.toAst + " )"
}
