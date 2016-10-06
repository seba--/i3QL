package idb.operators

import idb.{View, Relation}


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
    extends View[Range]
{

    def left: Relation[DomainA]

    def right: Relation[DomainB]

    override def children() = List (left, right)

    override def prettyprint(implicit prefix: String) = prefix +
      s"Union(${nested(left)},${nested(right)}})"
}



