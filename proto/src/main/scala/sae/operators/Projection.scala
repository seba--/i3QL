package sae
package operators

import sae.collections.Bag
import sae.core.LazyInitializer

/**
 * A projection operates as a filter on the relation and eliminates unwanted
 * constituents from the tuples.
 * Thus the projection shrinks the size of relations.
 * The new relations are either a new kind of object or anonymous tuples of the
 * warranted size and types of the projection.
 * Important Note:
 * E.Codd in his seminal work: RELATIONAL COMPLETENESS OF DATA BASE SUBLANGUAGES
 * defined projection as a set operations.
 * Thus the result does NOT contain duplicates.
 * According to other papers this treatment of duplicates complicates things
 * (i.e., in the translation from relational calculus to relational algebra? - TODO check).
 * In particular the following property is not guaranteed:
 * R intersect S is subset of R.
 * In set theory this is trivial. However with the use of duplicates the following situation arises:
 * R := a | b  S := a | b
 *      u | v       u | v
 *
 * Definition of intersection in relational calculus
 * R intersect S = ( R[1,2 = 1,2]S )[1,2].
 * Reads as: R joined with S where column 1 and column 2 are equal and
 * the result contains  column 1 and column 2.
 * Since the projection in the join:
 * R intersect S := a | b
 *                  u | v
 *                  a | b
 *                  u | v
 * The general projection class implemented here used the relational algebra semantics.
 * Specialized classes for SQL semantics are available (see further below).
 */

/**
 * The set projection removes duplicates from the results set.
 * We use the same Multiset as in Bag, but directly increment/decrement counts
 */
class SetProjection[Domain <: AnyRef, Range <: AnyRef](
    val projection : Domain => Range,
    val relation : View[Domain])
        extends MaterializedView[Range]
        with SelfMaintainedView[Domain, Range]
        with LazyView[Range] {

    import com.google.common.collect.HashMultiset;

    private val data : HashMultiset[Range] = HashMultiset.create[Range]()

    private var initialized = false

    relation addObserver this

    def foreach[U](f : Range => U) : Unit =
        if (!initialized) {
            lazyInitialize
        } else {

            val it : java.util.Iterator[Range] = data.iterator()
            while (it.hasNext()) {
                f(it.next())
            }
        }

    def lazyInitialize() : Unit =
        relation.foreach(t =>
            {
                data.add(projection(t))
            }
        )

    /**
     * We use a bag semantics, thus this method
     * returns false if the element is still present in the list
     * otherwise the method returns true, i.e., the element is
     * completely removed.
     */
    private def add_element(v : Range) : Boolean =
        {
            data.add(v)
            return data.count(v) != 0
        }

    /**
     * We use a generalized bag semantics, thus this method
     * returns false if the element was not already present in the list
     * otherwise the method returns true
     */
    private def remove_element(v : Range) : Boolean =
        {
            data.remove(v)
            return data.count(v) != 0
        }

    // update operations
    def updated(oldV : Domain, newV : Domain) : Unit =
        {
            val oldP = projection(oldV)
            val newP = projection(newV)
            if (oldP equals newP)
                return ;
            if (remove_element(oldP)) {
                element_removed(oldP)
            }
            if (add_element(newP)) {
                element_added(newP)
            }

        }

    def removed(v : Domain) : Unit =
        {
            val p = projection(v)
            if (remove_element(p)) {
                element_removed(p)
            }
        }

    def added(v : Domain) : Unit =
        {
            val p = projection(v)
            if (add_element(p)) {
                element_added(p)
            }
        }
}

/**
 * The non set projection has the usual SQL meaning of a projection
 * TODO this is not correctly typed yet
 */
class BagProjection[Domain <: AnyRef, Range <: AnyRef](
    val projection : Domain => Range,
    val relation : View[Domain])
        extends SelfMaintainedView[Domain, Range] {
    relation addObserver this

    def foreach[T](f : (Range) => T) : Unit =
        relation.foreach(v =>
            {
                f(projection(v))
            }
        )

    def updated(oldV : Domain, newV : Domain) : Unit =
        {
            element_updated(projection(oldV), projection(newV))
        }

    def removed(v : Domain) : Unit =
        {
            element_removed(projection(v))
        }

    def added(v : Domain) : Unit =
        {
            element_added(projection(v))
        }

}

/**
 * The materialized non-set projection has the semantics as the NonSetProjection
 */
class MaterializedBagProjection[Domain <: AnyRef, Range <: AnyRef](
    val projection : Domain => Range,
    val relation : View[Domain])
        extends Bag[Range]
        with MaterializedView[Range]
        with SelfMaintainedView[Domain, Range]
        with LazyInitializer[Range] {
    relation addObserver this

    def lazyInitialize() : Unit =
        {
            relation.foreach(t =>
                {
                    this += projection(t)
                }
            )
        }

    // update operations
    def updated(oldV : Domain, newV : Domain) : Unit =
        {
            val oldP = projection(oldV)
            val newP = projection(newV)
            if (oldP equals newP)
                return ;
            this -= oldP
            this += newP
        }

    def removed(v : Domain) : Unit =
        {
            this -= projection(v)
        }

    def added(v : Domain) : Unit =
        {
            this += projection(v)
        }
}