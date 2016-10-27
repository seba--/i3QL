package idb.operators

import java.io.PrintStream

import idb.{Relation, View}


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
trait Difference[Domain]
        extends View[Domain]
{
    def left: Relation[Domain]

    def right: Relation[Domain]

	override def children = List (left, right)

	override protected[idb] def printInternal(out : PrintStream)(implicit prefix: String = " "): Unit = {
		out.println(prefix + s"Difference(")
		printNested(out, left)
		printNested(out, right)
		out.println(prefix + ")")
    }

}



