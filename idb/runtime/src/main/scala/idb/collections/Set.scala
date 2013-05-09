package idb.collections

/**
 * A relation that is guaranteed to hold each element only once
 */
trait Set[V]
    extends
    Collection[V]
{
    private var data: java.util.HashSet[V] = new java.util.HashSet[V]()

    def isSet = true

    import scala.collection.JavaConversions._

    def size: Int = data.size

    def add_element (v: V) {
        data.add (v)
        data
    }

    def remove_element (v: V) {
        data.remove (v)
        data
    }

    def foreach[U] (f: V => U) {
        data.foreach (f)
    }

    def foreachWithCount[T] (f: (V, Int) => T) {
        data.foreach (v => f (v, 1))
    }

    def contains[U >: V] (v: U): Boolean = {
        data.contains (v)
    }

    def count[T >: V] (v: T) = {
        if (data.contains (v)) {
            1
        }
        else
        {
            0
        }
    }
}
