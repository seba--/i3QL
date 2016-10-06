package idb.operators

import idb.{View, Relation}


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
trait Difference[Domain]
        extends View[Domain]
{
    def left: Relation[Domain]

    def right: Relation[Domain]

	override def children() = List (left, right)

  override def prettyprint(implicit prefix: String) = prefix +
    s"Difference(${nested(left)}, ${nested(right)})"

}



