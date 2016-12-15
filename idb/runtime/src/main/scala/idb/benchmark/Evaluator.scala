package idb.benchmark

import idb.Relation
import idb.observer.Observer

trait Evaluator[Domain, Result] extends Observer[Domain] {

	val relation : Relation[Domain]

	def result() : Result

}
