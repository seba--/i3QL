package idb.operators

import idb.{MaterializedView, Relation}


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
trait Intersection[Domain]
    extends Relation[Domain]
{
    def left: Relation[Domain]

    def right: Relation[Domain]

    override protected def children = List (left, right)
}






