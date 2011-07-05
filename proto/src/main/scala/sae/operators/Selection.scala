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

    def lazyInitialize
    {
        relation.lazyInitialize
    }

    def lazy_foreach[T](f: (V) => T)
    {
        relation.lazy_foreach(v => {
            if (filter(v)) {
                f(v)
            }
        })
    }

    def updated_internal(oldV: V, newV: V)
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

    def removed_internal(v: V)
    {
        if (filter(v)) {
            element_removed(v)
        }
    }

    def added_internal(v: V)
    {
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

    def lazyInitialize
    {
        relation.lazy_foreach(t => {
            if (filter(t)) {
                add_element(t)
            }
        })
    }

    // update operations
    def updated_internal(oldV: V, newV: V)
    {
        if (filter(oldV)) {
            this -= oldV
        }
        if (filter(newV)) {
            this += newV
        }
    }

    def removed_internal(v: V)
    {
        if (filter(v)) {
            this -= v
        }
    }

    def added_internal(v: V)
    {
        if (filter(v)) {
            this += v
        }
    }
}


/**
 * The lazy selection stores no values and operates solely on the
 * lazy reevaluation or incremental updates.
 */
class DynamicFilterSelection[V <: AnyRef]
    (
    val filter: (V => Boolean) with Observable[(V, Boolean)],
    val relation: IndexedView[V]
)
        extends Selection[V]
            with LazyView[V]
{

    val index = relation.index(identity)

    index addObserver new Observer[(V, V)] {
        def added(kv: (V, V))
        {
            val v = kv._1
            if (filter(v))
                element_added(v)
            initialized = true
        }

        def removed(kv: (V, V))
        {
            val v = kv._1
            if (filter(v))
                element_removed(v)
            initialized = true
        }

        def updated(oldKV: (V, V), newKV: (V, V))
        {
            val oldV = oldKV._1
            val newV = newKV._1
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
            initialized = true
        }
    }

    filter addObserver new Observer[(V, Boolean)]
    {
        def added(vb: (V, Boolean))
        {
            vb match {
                case (v, true) if (index.isDefinedAt(v)) => element_added(v)
                case _ => // do nothing
            }
            initialized = true
        }

        def removed(vb: (V, Boolean))
        {
            vb match {
                case (v, true) if (index.isDefinedAt(v)) => element_removed(v)
                case _ => // do nothing
            }
            initialized = true
        }


        def updated(oldVB: (V, Boolean), newVB: (V, Boolean))
        {
            oldVB match {
                case (oldV, true) if (index.isDefinedAt(oldV)) =>
                {
                    newVB match {
                        case (newV, true) if (index.isDefinedAt(newV)) => element_updated(oldV, newV)
                        case (_, false) => element_removed(oldV)
                        case _ => // do nothing
                    }
                }
                case (_, false) =>
                {
                    newVB match {
                        case (newV, true) if (index.isDefinedAt(newV)) => element_added(newV)
                        case _ => // do nothing
                    }
                }
                case _ => // do nothing
            }
            initialized = true
        }

    }



    def lazy_foreach[T](f: (V) => T)
    {
        relation.foreach(v => if (filter(v)) f(v))
    }

    def lazyInitialize
    {
        // do nothing
    }
}