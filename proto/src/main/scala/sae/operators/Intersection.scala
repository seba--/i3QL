package sae.operators

import sae._

/**
 *
 * Author: Ralf Mitschke
 * Created: 25.05.11 12:33
 *
 */

/**
 * In set theory, the intersection (denoted as A ∩ B) of a collection of sets is the set of
 * all elements in A that are also in B
 *
 */
trait Intersection[Domain <: AnyRef]
    extends LazyView[Domain]
{
    type Dom = Domain

    val left: IndexedView[Domain]

    val right: IndexedView[Domain]
}

/**
 * The intersection operation in our algebra has non-distinct bag semantics
 *
 * This class can compute the intersection efficiently by relying on indices from the underlying relations.
 * The operation itself does not store any intermediate results.
 * Updates are computed based on indices and foreach is recomputed on every call.
 *
 * The size is cached internally to avoid recomputations
 */
class BagIntersection[Domain <: AnyRef]
    (
    val left: IndexedView[Domain],
    val right: IndexedView[Domain]
)
        extends Difference[Domain]
                with MaterializedView[Domain]
                with SelfMaintainedView[Domain, Domain]
{
    left addObserver this

    right addObserver RightObserver

    val leftIndex = left.index(identity)

    val rightIndex = right.index(identity)

    var cached_size = 0

    def lazyInitialize
    {
        left.foreach(element =>
            if ( rightIndex.isDefinedAt(element))
                cached_size += 1
        )
        initialized = true
    }

    def materialized_foreach[T](f: (Domain) => T)
    {
        left.foreach(element =>
            if ( rightIndex.isDefinedAt(element))
                f(element)
        )
    }


    protected def materialized_singletonValue = left.singletonValue match {
        case None => None
        case singletonValue@Some(v) => {
            if ( rightIndex.isDefinedAt(v))
                singletonValue
            else
                None
        }
    }

    protected def materialized_size = this.cached_size


    protected def materialized_contains(v: Domain) = left.contains(v) && right.contains(v)

    def added_internal(v: Domain)
    {
        if ( rightIndex.isDefinedAt(v))
        {
            element_added(v)
            cached_size += 1
        }
    }

    def removed_internal(v: Domain)
    {
        if ( rightIndex.isDefinedAt(v))
        {
            element_removed(v)
            cached_size -= 1
        }
    }

    def updated_internal(oldV: Domain, newV: Domain)
    {
        val oldDef = rightIndex.isDefinedAt(oldV)
        val newDef = rightIndex.isDefinedAt(newV)
        if (oldDef && newDef)
        {
            element_updated(oldV, newV)
            return
        }
        if (oldDef)
        {
            element_removed(oldV)
            cached_size -= 1
        }

        if (newDef)
        {
            element_added(newV)
            cached_size += 1
        }
    }


    object RightObserver extends Observer[Domain]
    {
        // update operations on right relation
        def updated(oldV: Domain, newV: Domain)
        {
            val oldDef = leftIndex.isDefinedAt(oldV)
            val newDef = leftIndex.isDefinedAt(newV)
            if (oldDef && !newDef)
            {
                // the element was in A but will not be in A and in B thus it is not be in the intersection
                element_removed(newV)
                cached_size -= 1
            }

            if (!oldDef && newDef)
            {
                // the element was not in A but oldV will  be in B thus the oldV is added to the intersection
                element_added(oldV)
                cached_size += 1
            }
            initialized = true
        }

        def removed(v: Domain)
        {
            if( leftIndex.isDefinedAt(v) )
            {
                element_removed(v)
                cached_size -= 1
            }
            initialized = true
        }

        def added(v: Domain)
        {
            if( leftIndex.isDefinedAt(v) )
            {
                element_added(v)
                cached_size += 1
            }

            initialized = true
        }
    }

}

