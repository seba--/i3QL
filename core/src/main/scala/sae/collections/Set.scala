package sae
package collections

import scala.collection.immutable.HashSet

/**
 * A relation backed by a set for efficient access to elements.
 * Each element has only one occurrence in this relation.
 */
trait Set[V <: AnyRef]
        extends Collection[V] //	 	with HashIndexedRelation[V]
        {
    private var data : HashSet[V] = new HashSet[V]()

    def materialized_size : Int = data.size

    def materialized_singletonValue : Option[V] = data.headOption

    def add_element(v : V) : Unit =
    {
        data = data + v
    }

    def remove_element(v : V) : Unit =
    {
        data = data - v
    }

    def materialized_foreach[U](f : V => U) : Unit = data.foreach(f)

    protected def materialized_contains(v: V) = data.contains(v)
}

class HashSetView[V <: AnyRef] extends Set[V]
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