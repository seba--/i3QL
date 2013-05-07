package idb.collections

/**
 * A relation backed by a multi set for efficient access to elements.
 * Each element may have multiple occurrences in this relation.
 */
trait Bag[V]
    extends Collection[V]
{

    import com.google.common.collect.HashMultiset

    private val data: HashMultiset[V] = HashMultiset.create[V]()

    def isSet = false

    def foreach[U] (f: V => U) {
        val it = data.iterator ()
        while (it.hasNext) {
            f (it.next ())
        }
    }

    def foreachWithCount[T] (f: (V, Int) => T) {
        val it = data.entrySet ().iterator ()
        while (it.hasNext) {
            val e = it.next ()
            f (e.getElement, e.getCount)
        }
    }


    def contains[U >: V] (v: U): Boolean = {
        data.contains (v)
    }

    def count[T >: V] (v: T) = {
        data.count (v)
    }

    def size: Int = {
        data.size ()
    }

    def add_element (v: V) {
        data.add (v)
    }


    def add_element (v: V, count: Int) {
        data.add (v, count)
    }

    def remove_element (v: V) {
        data.remove (v)
    }

    def remove_element (v: V, count: Int) {
        data.remove (v, count)
    }

    def update_element (oldV: V, newV: V) {
        val count = data.count (oldV)
        data.setCount (oldV, 0)
        data.add (newV, count)
        notify_updated (oldV, newV)
    }

    def update_element (oldV: V, newV: V, count: Int) {
        val oldCount = data.count (oldV)

        assert (oldCount >= count)

        data.setCount (oldV, oldCount - count)
        data.add (newV, count)
        notify_updated (oldV, newV)
    }
}