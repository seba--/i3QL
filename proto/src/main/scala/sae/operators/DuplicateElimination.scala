package sae
package operators

import sae.collections.Bag

/**
 * A duplicate elimination is available as a unary operator by itself
 */
trait DuplicateElimination[Domain <: AnyRef] {
    type Dom = Domain

    val relation : LazyView[Domain]
}
/**
 * The set projection class implemented here used the relational algebra semantics.
 * The set projection removes duplicates from the results set.
 * We use the same Multiset as in Bag, but directly increment/decrement counts
 */
class SetDuplicateElimination[Domain <: AnyRef](
    val relation : LazyView[Domain])
        extends DuplicateElimination[Domain]
        with MaterializedView[Domain]
        with Observer[Domain] {

    import com.google.common.collect.HashMultiset;

    private val data : HashMultiset[Domain] = HashMultiset.create[Domain]()

    relation addObserver this

    def materialized_foreach[U](f : Domain => U)
    {
            val it : java.util.Iterator[Domain] = data.elementSet().iterator()
            while (it.hasNext) {
                f(it.next())
            }
        }

    def materialized_size : Int =
        {
            data.elementSet().size()
        }

    def materialized_singletonValue : Option[Domain] =
        {
            if (size != 1)
                None
            else
                Some(data.iterator().next())
        }

    protected def materialized_contains(v: Domain) =
        data.contains(v)


    def lazyInitialize
    {
        relation.lazy_foreach(
                t => {
                data.add(t)
            }
        )
    }

    /**
     * We use a generalized bag semantics, thus this method
     * returns true if the element was not already present in the list
     * otherwise the method returns false
     */
    private def add_element(v : Domain) : Boolean =
        {
            val result = data.count(v) == 0
            data.add(v)
            result
        }

    /**
     * We use a bag semantics, thus this method
     * returns false if the element is still present in the list
     * otherwise the method returns true, i.e., the element is
     * completely removed.
     */
    private def remove_element(v : Domain) : Boolean =
        {
            data.remove(v)
            data.count(v) == 0
        }

    // update operations
    def updated(oldV : Domain, newV : Domain)
    {
            if (oldV equals newV)
                return ;
            if (remove_element(oldV)) {
                element_removed(oldV)
            }
            if (add_element(newV)) {
                element_added(newV)
            }

        }

    def removed(v : Domain)
    {
            if (remove_element(v)) {
                element_removed(v)
            }
        }

    def added(v : Domain)
    {
            if (add_element(v)) {
                element_added(v)
            }
        }

}