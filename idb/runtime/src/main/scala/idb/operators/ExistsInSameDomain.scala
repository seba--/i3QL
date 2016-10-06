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
trait ExistsInSameDomain[Domain]
    extends View[Domain]
{
  //  def left: MaterializedRelation[Domain]

  //  def right: MaterializedRelation[Domain]

	def left: Relation[Domain]

	def right: Relation[Domain]

    //def isSet = left.isSet

    //def isStored = left.isStored && right.isStored // should always be true

    override def children() = List (left, right)

  override def prettyprint(implicit prefix: String) = prefix +
    s"ExistsInSameDomain(${nested(left)}, ${nested(right)})"

}



