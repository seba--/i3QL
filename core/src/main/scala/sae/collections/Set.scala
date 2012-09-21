package sae
package collections


/**
 * A relation that is guaranteed to hold each element only once
 */
trait Set[V <: AnyRef]
    extends SetRelation[V]
    with Collection[V]
{

}

/**
 *
 * An implementation of set semantics based on a scala immutable HashSet
 *
 */
trait HashSet[V <: AnyRef]
    extends Set[V]
{
    private var data: scala.collection.immutable.HashSet[V] = new scala.collection.immutable.HashSet[V]()

    def materialized_size: Int = data.size

    def materialized_singletonValue: Option[V] = data.headOption

    def add_element(v: V): Unit =
    {
        data = data + v
    }

    def remove_element(v: V): Unit =
    {
        data = data - v
    }

    def materialized_foreach[U](f: V => U): Unit = data.foreach (f)

    protected def materialized_contains(v: V) = data.contains (v)
}

class HashSetViewOLD[V <: AnyRef] extends HashSet[V]
{


    /**
     * Each materialized view must be able to
     * materialize it's content from the underlying
     * views.
     * The laziness allows a query to be set up
     * on relations (tables) that are already filled.
     * thus the first call to foreach will try to
     * materialize already persisted data.
     */
    def lazyInitialize() {}
}