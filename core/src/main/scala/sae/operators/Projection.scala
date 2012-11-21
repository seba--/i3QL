package sae
package operators


/**
 * A projection operates as a filter on the relation and eliminates unwanted
 * constituents from the tuples.
 * Thus the projection shrinks the size of relations.
 * The new relations are either a new kind of object or anonymous tuples of the
 * warranted size and types of the projection.
 * Important Note:
 * E.Codd in his seminal work: RELATIONAL COMPLETENESS OF DATA BASE SUBLANGUAGES
 * defined projection as a set operations.
 * Thus the result does NOT contain duplicates.
 * According to other papers this treatment of duplicates complicates things
 * (i.e., in the translation from relational calculus to relational algebra? - TODO check).
 * In particular the following property is not guaranteed:
 * R intersect S is subset of R.
 * In set theory this is trivial. However with the use of duplicates the following situation arises:
 * R := a | b  S := a | b
 * u | v       u | v
 *
 * Definition of intersection in relational calculus
 * R intersect S = ( R[1,2 = 1,2]S )[1,2].
 * Reads as: R joined with S where column 1 and column 2 are equal and
 * the result contains  column 1 and column 2.
 * Since the projection in the join:
 * R intersect S := a | b
 * u | v
 * a | b
 * u | v
 *
 * Specialized classes for SQL semantics are available (see further below).
 * In general the Projection is an operation that takes a projection function
 * from domain to range and a relation of range tuples.
 * The parameterized types are accessible as members for use in
 * constructors during pattern matching
 */
trait Projection[Domain, Range]
    extends Relation[Range]
{
    def projection: Domain => Range

    def relation: Relation[Domain]

    def isSet =  false || forcedSet

    def isStored = relation.isStored

    private var forcedSet = sae.ENABLE_FORCE_TO_SET && false

    override def forceToSet = {
        forcedSet = sae.ENABLE_FORCE_TO_SET && true
        this
    }


    override protected def children = List (relation)
}
