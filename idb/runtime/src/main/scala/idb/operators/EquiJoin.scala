package idb.operators

import idb.{View, Relation}

/**
 * A join based on equality between elements in the underlying relations.
 *
 * This join has the most general form, where a projection is immediately applied
 * without generating a tuple object for results.
 * The form where a tuple (DomainA,DomainB) is returned is more specific and can always be emulated by providing a
 * respective function.
 */
trait EquiJoin[DomainA, DomainB, Range, Key]
    extends View[Range]
{

    def left: Relation[DomainA]

    def right: Relation[DomainB]

    def leftKey: DomainA => Key

    def rightKey: DomainB => Key

    def projection: (DomainA, DomainB) => Range

	override def children() = List(left,right)

    override def prettyprint(implicit prefix: String) = prefix +
      s"EquiJoin(leftKey=$leftKey, rightKey=$rightKey, ${nested(left)}, ${nested(right)})"

}

