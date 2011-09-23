package sae.operators

import sae._
import sae.collections.Set

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
 * This intersection operation distinct set semantics for elements
 *
 * This class stores a relation for computing which elements are already in the intersection
 */
class SetIntersection[Domain <: AnyRef]
    (
    val left: IndexedView[Domain],
    val right: IndexedView[Domain]
)
        extends Intersection[Domain]
                with Set[Domain]
{
    val leftIndex = left.index(identity)

    val rightIndex = right.index(identity)

    leftIndex addObserver LeftObserver

    rightIndex addObserver RightObserver



    def lazyInitialize
    {
        leftIndex.foreach(
            {
                case (key, element) if ( rightIndex.isDefinedAt(element)) =>
                    add_element(element)
                case _ => // do nothing
            }
        )
        initialized = true
    }

    object LeftObserver extends Observer[(Domain, Domain)]
    {

        /**
         * We have just added to left (leftIndex.elementCountAt(v) >= 1).
         * While we add elements to left and
         * have less than or equal elements compared to right, we generate new duplicates.
         *
         */
        def added(kv: (Domain, Domain))
        {
            val v = kv._1
            if ( rightIndex.isDefinedAt(v) && !contains(v) )
            {
                element_added(v)
                add_element(v)
            }
            initialized = true
        }

        /**
         * as long as left has more elements than right we only remove excess duplicates
         */
        def removed(kv: (Domain, Domain))
        {
            val v = kv._1
            // need to check the left index also to see whether the last element was removed
            if ( !(rightIndex.isDefinedAt(v) && leftIndex.isDefinedAt(v)) )
            {
                element_removed(v)
                remove_element(v)
            }
            initialized = true
        }

        def updated(oldKV: (Domain, Domain), newKV: (Domain, Domain))
        {
            val oldV = oldKV._1
            val newV = newKV._1

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
                remove_element(oldV)
            }

            if (newDef)
            {
                element_added(newV)
                add_element(newV)
            }

            initialized = true
        }
    }


    object RightObserver extends Observer[(Domain, Domain)]
    {

        /**
         * We have just added to left (leftIndex.elementCountAt(v) >= 1).
         * While we add elements to left and
         * have less than or equal elements compared to right, we generate new duplicates.
         *
         */
        def added(kv: (Domain, Domain))
        {
            val v = kv._1

            if( leftIndex.isDefinedAt(v) && !contains(v) )
            {
                element_added(v)
                add_element(v)
            }

            initialized = true
        }

        /**
         * as long as left has more elements than right we only remove excess duplicates
         */
        def removed(kv: (Domain, Domain))
        {
            val v = kv._1

            if ( !(rightIndex.isDefinedAt(v) && leftIndex.isDefinedAt(v)) )
            {
                element_removed(v)
                remove_element(v)
            }

            initialized = true
        }

        def updated(oldKV: (Domain, Domain), newKV: (Domain, Domain))
        {
            val oldV = oldKV._1
            val newV = newKV._1

            val oldDef = leftIndex.isDefinedAt(oldV)
            val newDef = leftIndex.isDefinedAt(newV)
            if (oldDef && !newDef)
            {
                // the element was in A but will not be in A and in B thus it is not be in the intersection
                element_removed(newV)
                remove_element(oldV)
            }

            if (!oldDef && newDef)
            {
                // the element was not in A but oldV will  be in B thus the oldV is added to the intersection
                element_added(oldV)
                add_element(newV)
            }

            initialized = true
        }
    }

}

/**
 * This intersection operation has non-distinct bag semantics
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
        extends Intersection[Domain]
        with MaterializedView[Domain]
{

    val leftIndex = left.index(identity)

    val rightIndex = right.index(identity)

    leftIndex addObserver LeftObserver

    rightIndex addObserver RightObserver

    var cached_size = 0

    def lazyInitialize
    {
        leftIndex.foreach(
            {
                case (key, element) if ( rightIndex.isDefinedAt(key) ) =>
                    cached_size += {
                        if(leftIndex.elementCountAt(key) > rightIndex.elementCountAt(key))
                            rightIndex.elementCountAt(key)
                        else
                            leftIndex.elementCountAt(key)
                    }
                case _ => // do nothing
            }
        )
        initialized = true
    }

    def materialized_foreach[T](f: (Domain) => T)
    {
        leftIndex.foreachKey( (key:Domain) =>
            {
                if ( rightIndex.isDefinedAt(key) )
                {
                    // we compute the min over the two counts
                    var count = scala.math.min(leftIndex.elementCountAt(key), rightIndex.elementCountAt(key))
                    while( count > 0 ){
                        f(key) // the keys and elements are the same as we used identity as key function
                        count -= 1
                    }
                }
            }
        )
    }


    protected def materialized_singletonValue = left.singletonValue match {
        case None => None
        case singletonValue@Some(v) => {
            if ( rightIndex.elementCountAt(v) == 1)
                singletonValue
            else
                None
        }
    }

    protected def materialized_size = this.cached_size


    protected def materialized_contains(v: Domain) = left.contains(v) && right.contains(v)


    object LeftObserver extends Observer[(Domain, Domain)]
    {

        /**
         * We have just added to left (leftIndex.elementCountAt(v) >= 1).
         * While we add elements to left and
         * have less than or equal elements compared to right, we generate new duplicates.
         *
         */
        def added(kv: (Domain, Domain))
        {
            val v = kv._1
            /*
            println("+" + v)
            println("left : " + leftIndex.elementCountAt(v))
            println("right: " + rightIndex.elementCountAt(v))
            */
            if (leftIndex.elementCountAt(v) <= rightIndex.elementCountAt(v)) {
                element_added(v)
                cached_size += 1
            }
            initialized = true
        }

        /**
         * as long as left has more elements than right we only remove excess duplicates
         */
        def removed(kv: (Domain, Domain))
        {
            val v = kv._1
            /*
            println("-" + v)
            println("left : " + leftIndex.elementCountAt(v) )
            println("right: " + rightIndex.elementCountAt(v) )
            */
            if ( leftIndex.elementCountAt(v) < rightIndex.elementCountAt(v) )
            {
                element_removed(v)
                cached_size -= 1
            }
            initialized = true
        }

        def updated(oldKV: (Domain, Domain), newKV: (Domain, Domain))
        {
            val oldV = oldKV._1
            val newV = newKV._1
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
            initialized = true
        }
    }


    object RightObserver extends Observer[(Domain, Domain)]
    {

        /**
         * We have just added to right (leftIndex.elementCountAt(v) >= 1). While we add elements to left and
         * have less than or equal elements compared to right, we generate new duplicates.
         *
         */
        def added(kv: (Domain, Domain))
        {
            val v = kv._1
            /*
            println("+" + v)
            println("left : " + leftIndex.elementCountAt(v) )
            println("right: " + rightIndex.elementCountAt(v) )
            */
            if(  rightIndex.elementCountAt(v) <= leftIndex.elementCountAt(v) )
            {
                element_added(v)
                cached_size += 1
            }

            initialized = true
        }

        /**
         * as long as left has more elements than right we only remove excess duplicates
         */
        def removed(kv: (Domain, Domain))
        {
            val v = kv._1
            /*
            println("-" + v)
            println("left : " + leftIndex.elementCountAt(v) )
            println("right: " + rightIndex.elementCountAt(v) )
            */
            if ( rightIndex.elementCountAt(v) < leftIndex.elementCountAt(v) )
            {
                element_removed(v)
                cached_size -= 1
            }
            initialized = true
        }

        def updated(oldKV: (Domain, Domain), newKV: (Domain, Domain))
        {
            val oldV = oldKV._1
            val newV = newKV._1
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
    }
}

