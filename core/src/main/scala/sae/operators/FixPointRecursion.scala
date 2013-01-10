package sae.operators

import sae._

/**
 * A operation that calculates the fix point of the recursive application of the method step.
 *
 * @author Ralf Mitschke
 */
trait FixPointRecursion[Domain, Range, Key]
    extends Relation[Range]
{
    def source: Relation[Domain]

    def anchorFunction: Domain => Option[Range]

    def domainKeyFunction: Domain => Key

    def rangeKeyFunction: Range => Key

    def step: (Domain, Range) => Range

    def isSet = false

    override protected def children = List (source)
}


