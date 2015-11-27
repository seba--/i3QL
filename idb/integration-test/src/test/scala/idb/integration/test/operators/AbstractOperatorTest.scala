package idb.integration.test.operators

import idb.query.QueryContext
import idb.{Table, Relation}

/**
 * @author Mirko Köhler
 */
abstract class AbstractOperatorTest[Domain, Range:Manifest] {

	implicit val queryContext : QueryContext

	def getQuery : Relation[Range] = {
		import idb.syntax.iql._



		val result = compile(query)
		reset()
		result
	}

	def query : Relation[Range]

	def table : Table[Domain]

}
