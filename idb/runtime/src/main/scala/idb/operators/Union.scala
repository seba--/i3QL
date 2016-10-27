package idb.operators

import java.io.PrintStream

import idb.{Relation, View}


/**
 *
 * Author: Ralf Mitschke
 * Created: 25.05.11 12:33
 *
 * In set theory, the union (denoted as ∪) of a collection of sets is the set of
 * all distinct elements in the collection
 *
 * The Union in our algebra is always non-distinct
 */
trait Union[Range, DomainA <: Range, DomainB <: Range]
    extends View[Range]
{

    def left: Relation[DomainA]

    def right: Relation[DomainB]

    override def children = List (left, right)

    override protected[idb] def printInternal(out : PrintStream)(implicit prefix: String = " "): Unit = {
        out.println(prefix + s"Union(")
        printNested(out, left)
        printNested(out, right)
        out.println(prefix + ")")
    }
}



