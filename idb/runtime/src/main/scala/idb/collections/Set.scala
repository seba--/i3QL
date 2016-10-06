package idb.collections

/**
 * A relation that is guaranteed to hold each element only once
 */
trait Set[V]
  extends
  Collection[V] {
  private val data: java.util.HashSet[V] = new java.util.HashSet[V]()

  def isSet = true

  import scala.collection.JavaConversions._

  def size: Int = data.size

  def clear() = {
    data.clear()
  }

  def add_element(v: V) {
    if (data.add(v))
      notify_added(v)
  }

  def add_elements(vs: Seq[V]) {
    val added = vs.filter(data.add)
    notify_addedAll(added)
  }

  def remove_element(v: V) {
    if (!data.remove(v))
      throw new IllegalStateException("Element not in set: " + v)
    notify_removed(v)
  }

  def remove_elements(vs: Seq[V]) {
    vs foreach { v =>
      if (!data.remove(v))
        throw new IllegalStateException("Element not in set: " + v)
    }
    notify_removedAll(vs)
  }

  def update_element(oldV: V, newV: V) {
    if (!data.remove(oldV)) {
      throw new IllegalStateException("Unable to update '" + oldV + "': element is not in the bag.")
    } else {
      data add newV
      notify_updated(oldV, newV)
    }
  }

  def foreach[U](f: V => U) {
    data.foreach(f)
  }

  def foreachWithCount[T](f: (V, Int) => T) {
    data.foreach(v => f(v, 1))
  }

  def contains[U >: V](v: U): Boolean = {
    data.contains(v)
  }

  def count[T >: V](v: T) = {
    if (data.contains(v)) {
      1
    }
    else {
      0
    }
  }
}
