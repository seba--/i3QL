package sae
package collections


/**
 * A relation that is guaranteed to hold each element only once
 */
trait Set[V <: AnyRef]
    extends SetRelation[V]
    with Collection[V]
    with LazyInitializedQueryResult[V]
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

    protected def isDefinedAt_internal(v: V) = {
        data.contains (v)
    }

    protected def elementCountAt_internal(v: V) = {
        if (data.contains (v)) {
            1
        }
        else
        {
            0
        }
    }
}
