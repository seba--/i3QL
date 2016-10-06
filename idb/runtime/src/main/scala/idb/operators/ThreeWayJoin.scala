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
trait ThreeWayJoin[DomainA, DomainB, DomainC, Range, KeyA, KeyC]
    extends View[Range]
{

    def left: Relation[DomainA]

    def middle: Relation[DomainB]

    def right: Relation[DomainC]

    def leftKey: DomainA => KeyA

    def middleToLeftKey: DomainB => KeyA

    def middleToRightKey: DomainB => KeyC

    def rightKey: DomainC => KeyC

    def projection: (DomainA, DomainB, DomainC) => Range

	  def children() = List(left,middle,right)

    override def prettyprint(implicit prefix: String) = prefix +
      s"ThreeWayJoin(leftKey=$leftKey, rightKey=$rightKey, ${nested(left)}, ${nested(middle)}, ${nested(right)})"

}

