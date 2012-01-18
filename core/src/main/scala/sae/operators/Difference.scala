package sae.operators

import sae._

/**
 *
 * Author: Ralf Mitschke
 * Created: 25.05.11 12:33
 *
 */

/**
 * In set theory, the difference (denoted as A ∖ B) of a collection of sets is the set of
 * all elements in A that are not also in B
 *
 */
trait Difference[Domain <: AnyRef]
        extends LazyView[Domain]
{
    type Dom = Domain

    def left: IndexedView[Domain]

    def right: IndexedView[Domain]
}

/**
 * The difference operation in our algebra has non-distinct bag semantics
 *
 * This class can compute the difference efficiently by relying on indices from the underlying relations.
 * The operation itself does not store any intermediate results.
 * Updates are computed based on indices and foreach is recomputed on every call.
 *
 * The size is cached internally to avoid recomputations
 */
class BagDifference[Domain <: AnyRef]
(
        val left: IndexedView[Domain],
        val right: IndexedView[Domain]
        )
        extends Difference[Domain]
        with MaterializedView[Domain]
{
    left addObserver LeftObserver

    right addObserver RightObserver

    override protected def children = List(left, right)

    override protected def childObservers(o: Observable[_]): Seq[Observer[_]] = {
        if (o == left) {
            return List(LeftObserver, leftIndex)
        }
        if (o == right) {
            return List(RightObserver, rightIndex)
        }
        Nil
    }

    private val leftIndex = left.index(identity)

    private val rightIndex = right.index(identity)

    private var cached_size = 0

    // TODO we can simplify the initialization code in the observers by just calling init on construction
    def lazyInitialize {
        leftIndex.foreachKey(
            (v: Domain) => {
                var difference = leftIndex.elementCountAt(v) - rightIndex.elementCountAt(v)
                if (difference > 0) {
                    cached_size += difference
                }
            }
        )
        initialized = true
    }

    def materialized_foreach[T](f: (Domain) => T) {
        leftIndex.foreachKey(
            (v: Domain) => {
                var difference = leftIndex.elementCountAt(v) - rightIndex.elementCountAt(v)
                while (difference > 0) {
                    f(v)
                    difference = difference - 1
                }
            }
        )
    }


    protected def materialized_singletonValue = left.singletonValue match {
        case None => None
        case singletonValue@Some(v) => {
            if (!rightIndex.isDefinedAt(v))
                singletonValue
            else
                None
        }
    }

    protected def materialized_size = this.cached_size

    protected def materialized_contains(v: Domain) = left.contains(v) && !right.contains(v)

    object LeftObserver extends Observer[Domain]
    {
        def updated(oldV: Domain, newV: Domain) {
            // we are notified after the update, hence the leftIndex will be updated to newV
            var oldCount = leftIndex.elementCountAt(newV) - rightIndex.elementCountAt(oldV)
            var newCount = leftIndex.elementCountAt(newV) - rightIndex.elementCountAt(newV)
            if(!initialized){
                lazyInitialize
                 if(oldCount>0) cached_size -= oldCount
                 if(newCount>0) cached_size +=newCount
            }

            if (oldCount == newCount) {
                element_updated(oldV, newV)
                return
            }
            while(oldCount > 0)
            {
                element_removed(oldV)
                cached_size -= 1
                oldCount -= 1
            }
            while(newCount > 0)
            {
                element_added(oldV)
                cached_size += 1
                newCount -= 1
            }
        }

        def removed(v: Domain) {
            if(!initialized){
                lazyInitialize
                // add the removed element
                if(leftIndex.elementCountAt(v) >= rightIndex.elementCountAt(v)) cached_size += 1
            }

            // check that this was a removal where we still had more elements than right side
            if (leftIndex.elementCountAt(v) >= rightIndex.elementCountAt(v)) {
                element_removed(v)
                cached_size -= 1
            }
        }

        def added(v: Domain) {
            if(!initialized){
                lazyInitialize
                // remove the added element
                if(leftIndex.elementCountAt(v) > rightIndex.elementCountAt(v)) cached_size -= 1
            }

            // check that this was an addition where we did not have less elements than right side
            if (leftIndex.elementCountAt(v) > rightIndex.elementCountAt(v)) {
                element_added(v)
                cached_size += 1
            }
        }
    }

    object RightObserver extends Observer[Domain]
    {
        // update operations on right relation
        def updated(oldV: Domain, newV: Domain) {
            // we are notified after the update, hence the rightIndex will be updated to newV
            var oldCount = if(leftIndex.elementCountAt(oldV) >= rightIndex.elementCountAt(newV) ) rightIndex.elementCountAt(newV) else leftIndex.elementCountAt(oldV)
            var newCount = if(leftIndex.elementCountAt(newV) >= rightIndex.elementCountAt(newV) ) rightIndex.elementCountAt(newV) else leftIndex.elementCountAt(oldV)
            if(!initialized){
                lazyInitialize
                if(oldCount>0) cached_size += oldCount
                if(newCount>0) cached_size -=newCount
            }
            if (oldCount == newCount) {
                element_updated(oldV, newV)
                return
            }
            while(oldCount > 0)
            {
                element_added(oldV)
                cached_size += 1
                oldCount -= 1
            }
            while(newCount > 0)
            {
                element_removed(oldV)
                cached_size -= 1
                newCount -= 1
            }
        }

        def removed(v: Domain) {
            if(!initialized){
                lazyInitialize
                // remove the removed element
                if(leftIndex.elementCountAt(v) > rightIndex.elementCountAt(v)) cached_size -= 1
            }

            // check that this was the last removal of an element not in left side
            if (leftIndex.elementCountAt(v) > rightIndex.elementCountAt(v)) {
                element_added(v)
                cached_size += 1
            }
        }

        def added(v: Domain) {
            if(!initialized){
                lazyInitialize
                // add the added element
                if(leftIndex.elementCountAt(v) >= rightIndex.elementCountAt(v)) cached_size += 1
            }

            // check that this was an addition where we have more or equal amount of elements compared to left side
            if (leftIndex.elementCountAt(v) >= rightIndex.elementCountAt(v)) {
                element_removed(v)
                cached_size -= 1
            }
        }
    }

}

