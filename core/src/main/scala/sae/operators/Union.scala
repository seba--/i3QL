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
trait Union[Range, DomainA <: Range, DomainB <: Range]
    extends Relation[Range]
{

    def left: Relation[DomainA]

    def right: Relation[DomainB]

    def isSet = false

    def isStored = left.isStored && right.isStored

    override protected def children = List (left, right)
}



