package idb.operators

import java.io.PrintStream

import idb.{Relation, View}

/**
 * A duplicate elimination removes all duplicates from the underlying relation and returns a set.
 */
trait DuplicateElimination[Domain]
    extends View[Domain]
{
    def relation: Relation[Domain]

    override def children = List (relation)

    override protected[idb] def printInternal(out : PrintStream)(implicit prefix: String = " "): Unit = {
        out.println(prefix + s"DuplicateElimination(")
        printNested(out, relation)
        out.println(prefix + ")")
    }
}

