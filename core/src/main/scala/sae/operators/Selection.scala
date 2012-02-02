package sae
package operators

import sae.collections.Bag

/**
 * A selection operates as a filter on the values in the relation and eliminates
 * unwanted tuples. Thus the projection shrinks the number of relations.
 */
trait Selection[V <: AnyRef]
        extends LazyView[V]
{
    type Value = V

    val filter: V => Boolean

    val relation: LazyView[V]
}

/**
 * The lazy selection stores no values an operates solely on the
 * lazy reevaluation or incremental updates.
 */
class LazySelection[V <: AnyRef](
                                        val filter: V => Boolean,
                                        val relation: LazyView[V]
                                        )
        extends Selection[V]
        with SelfMaintainedView[V, V]
{

    relation addObserver this

    override protected def children = List(relation)

    override protected def childObservers(o: Observable[_]): Seq[Observer[_]] = {
        if (o == relation) {
            return List(this)
        }
        Nil
    }

    def lazyInitialize {

    }

    def lazy_foreach[T](f: (V) => T) {
        relation.lazy_foreach(v => {
            if (filter(v)) {
                f(v)
            }
        })
    }

    def updated_internal(oldV: V, newV: V) {
        if (filter(oldV) && filter(newV)) {
            element_updated(oldV, newV)
        } else {
            // only one of the elements complies to the filter
            if (filter(oldV)) {
                element_removed(oldV)
            }
            if (filter(newV)) {
                element_added(newV)
            }
        }
    }

    def removed_internal(v: V) {
        if (filter(v)) {
            element_removed(v)
        }
    }

    def added_internal(v: V) {
        if (filter(v)) {
            element_added(v)
        }
    }

}

/**
 * A materialized selection stores all selected values in a Bag
 */
class MaterializedSelection[V <: AnyRef](
                                                val filter: V => Boolean,
                                                val relation: LazyView[V]
                                                )
        extends Selection[V]
        with Bag[V]
        with SelfMaintainedView[V, V]
{

    relation addObserver this


    override protected def children = List(relation)

    override protected def childObservers(o: Observable[_]): Seq[Observer[_]] = {
        if (o == relation) {
            return List(this)
        }
        Nil
    }

    def lazyInitialize {
        if (initialized) return
        relation.lazy_foreach(t => {
            if (filter(t)) {
                add_element(t)
            }
        })
        initialized = true
    }

    // update operations
    def updated_internal(oldV: V, newV: V) {
        if (filter(oldV)) {
            this -= oldV
        }
        if (filter(newV)) {
            this += newV
        }
    }

    def removed_internal(v: V) {
        if (filter(v)) {
            this -= v
        }
    }

    def added_internal(v: V) {
        if (filter(v)) {
            this += v
        }
    }
}