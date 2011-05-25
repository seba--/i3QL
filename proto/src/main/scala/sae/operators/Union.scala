package sae.operators

import sae.{SelfMaintainedView, LazyView}

/**
 *
 * Author: Ralf Mitschke
 * Created: 25.05.11 12:33
 *
 */

/**
 * In set theory, the union (denoted as ∪) of a collection of sets is the set of
 * all distinct elements in the collection
 *
 * The Union in our algebra is always non-distinct
 */
trait Union[Domain <: AnyRef]
{
    type Dom = Domain

    val left: LazyView[Domain]

    val right: LazyView[Domain]
}


class BagUnion[Domain <: AnyRef]
(
    val left: LazyView[Domain],
    val right: LazyView[Domain]
)
        extends Union[Domain]
        with SelfMaintainedView[Domain, Domain]
{
    left addObserver this

    right addObserver this

    def lazyInitialize
    {
        // do nothing
    }

    def lazy_foreach[T](f: (Domain) => T)
    {
        left.lazy_foreach(f)
        right.lazy_foreach(f)
    }

    def added_internal(v: Domain)
    {
        element_added(v)
    }

    def removed_internal(v: Domain)
    {
        element_removed(v)
    }

    def updated_internal(oldV: Domain, newV: Domain)
    {
        element_updated(oldV, newV)
    }
}

