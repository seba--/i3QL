package idb.operators

import idb.Relation


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

    override protected def children = List (left, right)
}



