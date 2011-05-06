package sae
package operators
import sae.collections.Bag
import sae.core.LazyInitializer

/**
 * A selection operates as a filter on the values in the relation and eliminates
 * unwanted tuples. Thus the projection shrinks the number of relations.
 */
class Selection[V <: AnyRef](
    val filter : V => Boolean,
    val relation : View[V])
        extends SelfMaintainedView[V, V] {
    relation addObserver this

    def foreach[T](f : (V) => T) : Unit =
        {
            relation.foreach(v =>
                {
                    if (filter(v)) {
                        f(v)
                    }
                })
        }

    def updated(oldV : V, newV : V) : Unit =
        {
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

    def removed(v : V) : Unit =
        {
            if (filter(v)) {
                element_removed(v)
            }
        }

    def added(v : V) : Unit =
        {
            if (filter(v)) {
                element_added(v)
            }
        }

}

/**
 * A materialized selection extends a selection to be materialized
 */
class MaterializedSelection[V <: AnyRef](
    val filter : V => Boolean,
    val relation : View[V])
        extends Bag[V]
        with MaterializedView[V]
        with SelfMaintainedView[V, V]
        with LazyInitializer[V] {
    relation addObserver this

    def lazyInitialize() : Unit =
        {
            relation.foreach(t =>
                {
                    if (filter(t)) {
                        add_element(t)
                    }
                })
        }

    // update operations
    def updated(oldV : V, newV : V) : Unit =
        {
            if (filter(oldV)) {
                this -= oldV
            }
            if (filter(newV)) {
                this += newV
            }
        }

    def removed(v : V) : Unit =
        {
            if (filter(v)) {
                this -= v
            }
        }

    def added(v : V) : Unit =
        {
            if (filter(v)) {
                this += v
            }
        }
}
