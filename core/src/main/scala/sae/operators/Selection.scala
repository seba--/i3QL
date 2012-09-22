package sae
package operators


/**
 * A selection operates as a filter on the values in the relation and eliminates
 * unwanted tuples. A selection is always self-maintainable and requires only the delta of the underlying relation
 */
trait Selection[Domain <: AnyRef]
    extends Relation[Domain]
{
    def filter: Domain => Boolean

    def relation: Relation[Domain]

    def isSet = relation.isSet

    override protected def children = List (relation)
}
