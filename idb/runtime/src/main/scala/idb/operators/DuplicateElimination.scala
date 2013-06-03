package idb.operators

import idb.Relation

/**
 * A duplicate elimination removes all duplicates from the underlying relation and returns a set.
 */
trait DuplicateElimination[Domain]
    extends Relation[Domain]
{
    def relation: Relation[Domain]

    override protected def children = List (relation)
}

