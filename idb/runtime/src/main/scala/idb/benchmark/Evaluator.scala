package idb.benchmark

import idb.Relation
import idb.observer.Observer

/**
  * Created by mirko on 25.10.16.
  */
trait Evaluator[Domain, Result] extends Observer[Domain] {

	val relation : Relation[Domain]

	def result() : Result

}
