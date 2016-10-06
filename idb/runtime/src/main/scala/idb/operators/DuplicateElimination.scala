package idb.operators

import idb.{View, Relation}

/**
 * A duplicate elimination removes all duplicates from the underlying relation and returns a set.
 */
trait DuplicateElimination[Domain]
    extends View[Domain]
{
    def relation: Relation[Domain]

    override def children() = List (relation)

    override def prettyprint(implicit prefix: String) = prefix +
      s"DuplicateElimination(${nested(relation)})"
}

