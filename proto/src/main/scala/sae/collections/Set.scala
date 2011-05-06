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

    def size : Int = data.size

    def singletonValue : Option[V] = data.headOption

    def add_element(v : V) : Unit =
        {
            data = data + v
        }

    def remove_element(v : V) : Unit =
        {
            data = data - v
        }

    def materialized_foreach[U](f : V => U) : Unit = data.foreach(f)

}