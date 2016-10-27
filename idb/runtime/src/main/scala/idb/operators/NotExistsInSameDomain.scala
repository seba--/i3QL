package idb.operators

import java.io.PrintStream

import idb.{MaterializedView, Relation, View}


/**
 *
 * Author: Ralf Mitschke
 * Created: 25.05.11 12:33
 *
 */

/**
 * In set theory, the difference (denoted as A ∖ B) of a collection of sets is the set of
 * all elements in A that are not also in B
 *
 */
trait NotExistsInSameDomain[Domain]
    extends View[Domain]
{
    def left: MaterializedView[Domain]

    def right: MaterializedView[Domain]

    override def children = List (left, right)

    override protected[idb] def printInternal(out : PrintStream)(implicit prefix: String = " "): Unit = {
        out.println(prefix + s"NotExistsInSameDomain(")
        printNested(out, left)
        printNested(out, right)
        out.println(prefix + ")")
    }

}



