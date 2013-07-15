package idb.operators

import idb.Relation

/**
 * The symmetric difference of two relations A and B only contains elements that are in either A or B but not in both.
 */
trait SymmetricDifference[Domain] extends Relation[Domain] {

	def left : Relation[Domain]

	def right : Relation[Domain]

}
