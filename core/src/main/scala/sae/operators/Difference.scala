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
trait Difference[Domain]
        extends Relation[Domain]
{
    def left: MaterializedRelation[Domain]

    def right: MaterializedRelation[Domain]

    def isSet = left.isSet
}



