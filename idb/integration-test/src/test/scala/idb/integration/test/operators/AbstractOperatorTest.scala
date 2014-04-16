package idb.integration.test.operators

import idb.{Table, Relation}

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

	def table : Table[Domain]

}
