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
    extends Relation[Domain]
{
    def left: MaterializedRelation[Domain]

    def right: MaterializedRelation[Domain]

    def isSet = left.isSet || right.isSet

    override protected def children = List (left, right)
}






