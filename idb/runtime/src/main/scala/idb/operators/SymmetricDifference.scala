package idb.operators

import idb.{View, Relation}

/**
 * The symmetric difference of two relations A and B only contains elements that are in either A or B but not in both.
 */
trait SymmetricDifference[Domain] extends View[Domain] {

	def left : Relation[Domain]

	def right : Relation[Domain]

	override def prettyprint(implicit prefix: String) = prefix +
		s"SymmetricDifference(${nested(left)}, ${nested(right)})"
}
