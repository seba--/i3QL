package sae.operators

import sae.{SelfMaintainedView, LazyView}

/**
 *
 * Author: Ralf Mitschke
 * Created: 25.05.11 12:33
 *
 * In set theory, the union (denoted as ∪) of a collection of sets is the set of
 * all distinct elements in the collection
 *
 * The Union in our algebra is always non-distinct
 */
trait Union[Range <: AnyRef, DomainA <: Range, DomainB <: Range]
    extends LazyView[Range]
{
    type Rng = Range

    val left: LazyView[DomainA]

    val right: LazyView[DomainB]
}


class BagUnion[Range <: AnyRef, DomainA <: Range, DomainB <: Range]
(
    val left: LazyView[DomainA],
    val right: LazyView[DomainB]
)
        extends Union[Range,DomainA, DomainB]
        with SelfMaintainedView[Range, Range]
{
    left addObserver this

    right addObserver this

    def lazyInitialize
    {
        // do nothing
    }

    def lazy_foreach[T](f: (Range) => T)
    {
        left.lazy_foreach(f)
        right.lazy_foreach(f)
    }

    def added_internal(v: Range)
    {
        element_added(v)
    }

    def removed_internal(v: Range)
    {
        element_removed(v)
    }

    def updated_internal(oldV: Range, newV: Range)
    {
        element_updated(oldV, newV)
    }
}

