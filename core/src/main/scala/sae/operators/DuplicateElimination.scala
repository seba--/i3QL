package sae
package operators


/**
 * A duplicate elimination removes all duplicates from the underlying relation and returns a set.
 */
trait DuplicateElimination[Domain <: AnyRef]
    extends Relation[Domain]
{
    def relation: Relation[Domain]

    def isSet = true
}

