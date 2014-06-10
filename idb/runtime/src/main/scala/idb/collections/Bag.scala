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
		notify_added (v)
    }


    def add_element (v: V, count: Int) {
    //    data.add (v, count)
		throw new UnsupportedOperationException()
    }

    def remove_element (v: V) {
        if (!data.remove (v))
			throw new IllegalStateException("Unable to remove '" + v + "': element is not in the bag.")
		notify_removed(v)
    }

    def remove_element (v: V, count: Int) {
	/*	val i = data.remove (v, count)
       	if (i < count)
		   throw new IllegalStateException("Unable to remove '" + v + "': element was only "+ i + " times in the bag (remove count was " + count + ").")
    */
		throw new UnsupportedOperationException()
	}

    def update_element (oldV: V, newV: V) {
        if (data remove oldV) {
          data add newV
          notify_updated(oldV, newV)
        } else {
			throw new IllegalStateException("Unable to update '" + oldV + "': element is not in the bag.")
		}

     /*   val count = data.count (oldV)
        data.setCount (oldV, 0)
        data.add (newV, count)
        notify_updated (oldV, newV)    */
    }

    def update_element (oldV: V, newV: V, count: Int) {
	/*	(1 to count) foreach {
			(i) => update_element(oldV, newV)
		}   */
		throw new UnsupportedOperationException()

	/*    val oldCount = data.count (oldV)

        assert (oldCount >= count)

        data.setCount (oldV, oldCount - count)
        data.add (newV, count)
        notify_updated (oldV, newV)  */
    }
}