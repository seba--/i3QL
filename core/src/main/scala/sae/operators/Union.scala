package sae.operators

import sae._


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

    def left: LazyView[DomainA]

    def right: LazyView[DomainB]
}

/**
 * A self maintained union, that produces count(A) + count(B) duplicates for underlying relations A and B
 */
class AddMultiSetUnion[Range <: AnyRef, DomainA <: Range, DomainB <: Range]
(
    val left: LazyView[DomainA],
    val right: LazyView[DomainB]
    )
    extends Union[Range, DomainA, DomainB]
    with SelfMaintainedView[Range, Range]
{
    left addObserver this

    right addObserver this

    override protected def children = List (left.asInstanceOf[Observable[AnyRef]], right)

    override protected def childObservers(o: Observable[_]): Seq[Observer[_]] = {
        if (o == left || o == right) {
            return List (this)
        }
        Nil
    }

    def lazyInitialize() {
        // do nothing
    }

    def lazy_foreach[T](f: (Range) => T) {
        left.lazy_foreach (f)
        right.lazy_foreach (f)
    }

    def added_internal(v: Range) {
        element_added (v)
    }

    def removed_internal(v: Range) {
        element_removed (v)
    }

    def updated_internal(oldV: Range, newV: Range) {
        element_updated (oldV, newV)
    }
}

/**
 * A not self maintained union, that produces max(count(A) , count(B)) duplicates for underlying relations A and B
 * Required for correctness if we translate conditionA OR conditionB to algebra.
 * Can be omitted if A \intersect B == empty
 */
class MaxMultiSetUnion[Range <: AnyRef, DomainA <: Range, DomainB <: Range]
(
    val left: MaterializedMultiSetView[DomainA],
    val right: MaterializedMultiSetView[DomainB]
    )
    extends Union[Range, DomainA, DomainB]
{
    left addObserver LeftObserver

    right addObserver RightObserver

    override protected def children = List (left.asInstanceOf[Observable[AnyRef]], right)

    override protected def childObservers(o: Observable[_]): Seq[Observer[_]] = {
        if (o == left) {
            return List (LeftObserver)
        }
        if (o == right) {
            return List (RightObserver)
        }
        Nil
    }

    def lazyInitialize() {
        //* do nothing
    }

    def lazy_foreach[T](f: (Range) => T) {
        left.foreachWithCount ((v: Range, count: Int) => {
            val max = scala.math.max (count, right.elementCountAt (v))
            var i = 0
            while (i < max) {
                f (v)
                i += 1
            }
        }
        )
    }

    object LeftObserver extends Observer[DomainA]
    {
        def updated(oldV: DomainA, newV: DomainA) {
            if (oldV == newV)
                return
            // if we assume that update means all instances are updated this is correct
            element_updated (oldV, newV)
        }

        def removed(v: DomainA) {
            val oldCount = left.elementCountAt (v)
            val rightCount = right.elementCountAt (v)
            if (rightCount < oldCount - 1) {
                element_removed (v)
            }
        }

        def added(v: DomainA) {
            val oldCount = left.elementCountAt (v)
            val rightCount = right.elementCountAt (v)
            if (rightCount < oldCount + 1) {
                element_added (v)
            }
        }
    }

    object RightObserver extends Observer[DomainB]
    {
        // update operations on right relation
        def updated(oldV: DomainB, newV: DomainB) {
            if (oldV == newV)
                return
            // if we assume that update means all instances are updated this is correct
            element_updated (oldV, newV)
        }

        def removed(v: DomainB) {
            val leftCount = left.elementCountAt (v)
            val oldCount = right.elementCountAt (v)
            if (leftCount < oldCount - 1) {
                element_removed (v)
            }
        }

        def added(v: DomainB) {
            val leftCount = left.elementCountAt (v)
            val oldCount = right.elementCountAt (v)
            if (leftCount < oldCount + 1) {
                element_added (v)
            }
        }
    }


}