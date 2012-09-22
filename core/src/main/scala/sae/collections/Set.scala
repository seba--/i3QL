package sae
package collections


/**
 * A relation that is guaranteed to hold each element only once
 */
trait Set[V]
    extends SetRelation[V]
    with Collection[V]
    with QueryResult[V]
{
    private var data: scala.collection.immutable.HashSet[V] = new scala.collection.immutable.HashSet[V]()

    def size: Int = data.size

    def singletonValue: Option[V] = data.headOption

    def add_element(v: V)
    {
        data = data + v
    }

    def remove_element(v: V)
    {
        data = data - v
    }

    def foreach[U](f: V => U) {
        data.foreach (f)
    }

    def foreachWithCount[T](f: (V, Int) => T) {
        data.foreach (v => f (v, 1))
    }

    def contains(v: V) = data.contains (v)

    def isDefinedAt(v: V) = {
        data.contains (v)
    }

    def elementCountAt[T >: V](v: T) = {
        if (v.isInstanceOf[V] && data.contains (v.asInstanceOf[V])) {
            1
        }
        else
        {
            0
        }
    }
}
