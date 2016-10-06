package idb.collections

import idb.MaterializedView
import idb.observer.NotifyObservers

trait Collection[V]
  extends MaterializedView[V]
  with NotifyObservers[V] {

  /**
   * Add a data tuple to this relation.
   * The element is added and then a change event is fired.
   * On the first call this method initializes the collection.
   * This is required, so no inconsistent state emerges, when a
   * collection is used as intermediary view. Otherwise data is
   * pumped into the collection and during first use, lazyInitialize is called.
   */
  def +=(v: V): Collection[V] = {
    add_element(v)
    this
  }

  def ++=(vs: Seq[V]): Collection[V] = {
    add_elements(vs)
    this
  }

  /**
   * Internal implementation of the add method
   */
  def add_element (v: V)
  def add_elements (vs: Seq[V])

  /**
   * Remove a data tuple from this relation
   * The element is removed and then a change event is fired.
   * On the first call this method initializes the collection.
   * This is required, so no inconsistent state emerges, when a
   * collection is used as intermediary view. Otherwise data is
   * pumped into the collection and during first use, lazyInitialize is called.
   */
  def -=(v: V): Collection[V] = {
    remove_element(v)
    this
  }

  def --=(vs: Seq[V]): Collection[V] = {
    remove_elements(vs)
    this
  }

  /**
   * internal implementation remove method
   */
  def remove_element(v: V)
  def remove_elements(vs: Seq[V])

}