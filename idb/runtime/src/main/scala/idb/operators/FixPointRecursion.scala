package idb.operators

import idb.{View, Relation}


/**
 * A operation that calculates the fix point of the recursive application of the method step.
 *
 * @author Ralf Mitschke
 */
trait FixPointRecursion[Domain, Range, Key]
    extends View[Range]
{
    def source: Relation[Domain]

    def anchorFunction: Domain => Option[Range]

    def domainKeyFunction: Domain => Key

    def rangeKeyFunction: Range => Key

    def step: (Domain, Range) => Range

    override def children() = List (source)

    override def prettyprint(implicit prefix: String) = prefix +
      s"FixPointRecursion(${nested(source)})"

}


