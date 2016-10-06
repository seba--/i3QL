package idb.operators

import idb.{View, MaterializedView, Relation}


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
trait NotExistsInSameDomain[Domain]
    extends View[Domain]
{
    def left: MaterializedView[Domain]

    def right: MaterializedView[Domain]

    override def children() = List (left, right)

    override def prettyprint(implicit prefix: String) = prefix +
      s"NotExistsInSameDomain(${nested(left)}, ${nested(right)})"

}



