package idb.operators

import java.io.PrintStream

import idb.{Relation, View}

/**
 * The symmetric difference of two relations A and B only contains elements that are in either A or B but not in both.
 */
trait SymmetricDifference[Domain] extends View[Domain] {

	def left : Relation[Domain]

	def right : Relation[Domain]

	override protected[idb] def printInternal(out : PrintStream)(implicit prefix: String = " "): Unit = {
		out.println(prefix + s"SymmetricDifference(")
		printNested(out, left)
		printNested(out, right)
		out.println(prefix + ")")
	}
}
