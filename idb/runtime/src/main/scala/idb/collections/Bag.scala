package idb.collections

/**
 * A relation backed by a multi set for efficient access to elements.
 * Each element may have multiple occurrences in this relation.
 */
trait Bag[V]
  extends Collection[V] {

  import com.google.common.collect.HashMultiset

  private val data: HashMultiset[V] = HashMultiset.create[V]()

  def isSet = false

  def foreach[U](f: V => U) {
    val it = data.iterator()
    while (it.hasNext) {
      f(it.next())
    }
  }

  def foreachWithCount[T](f: (V, Int) => T) {
    val it = data.entrySet().iterator()
    while (it.hasNext) {
      val e = it.next()
      f(e.getElement, e.getCount)
    }
  }


  def contains[U >: V](v: U): Boolean = {
    data.contains(v)
  }

  def count[T >: V](v: T) = {
    data.count(v)
  }

  def size: Int = {
    data.size()
  }

  def clear(): Unit = {
    data.clear()
  }

  def add_element(v: V) {
    data.add(v)
    notify_added(v)
  }

  def add_elements(vs: Seq[V]): Unit = {
    for (v <- vs)
      data.add(v)
    notify_addedAll(vs)
  }

  def add_element (v: V, count: Int) {
    throw new UnsupportedOperationException()
  }

  def remove_element(v: V) {
    if (!data.remove(v))
      throw new IllegalStateException("element is not in bag: " + v)
    notify_removed(v)
  }

  def remove_elements(vs: Seq[V]): Unit = {
    for (v <- vs)
      if (!data.remove(v))
        throw new IllegalStateException("element is not in bag: " + v)
    notify_removedAll(vs)
  }

  def remove_element(v: V, count: Int) {
    throw new UnsupportedOperationException()
  }

  def update_element(oldV: V, newV: V) {
    if (!data.remove(oldV)) {
      throw new IllegalStateException("Unable to update '" + oldV + "': element is not in the bag.")
    } else {
      data add newV
      notify_updated(oldV, newV)
    }
  }

  def update_element(oldV: V, newV: V, count: Int) {
    throw new UnsupportedOperationException()
  }
}