package idb.integration.test.operators

import idb.{Extent, Relation}

/**
 * @author Mirko Köhler
 */
abstract class AbstractOperatorTest[Domain, Range:Manifest] {

	def getQuery : Relation[Range] = {
		import idb.syntax.iql._

		val result = compile(query)
		reset()
		result
	}

	def query : Relation[Range]

	def extent : Extent[Domain]

  def printRelation(r : Relation[Any]) {
    println("********************************************")
    r.foreach(println(_))
    println("********************************************")

  }

}
