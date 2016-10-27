package idb.operators

import java.io.PrintStream

import idb.{Relation, View}


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

    def children = List(left,middle,right)

    override protected[idb] def printInternal(out : PrintStream)(implicit prefix: String = " "): Unit = {
        out.println(prefix + s"ThreeWayJoin(leftKey=$leftKey, rightKey=$rightKey,")
        printNested(out, left)
        printNested(out, middle)
        printNested(out, right)
        out.println(prefix + ")")
    }

}

