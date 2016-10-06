package idb.integration.test.operators

import idb.query.QueryEnvironment$
import idb.{Table, Relation}

/**
 * @author Mirko Köhler
 */
abstract class AbstractOperatorTest[Domain, Range:Manifest] {


	def getQuery : Relation[Range] = {
		import idb.syntax.iql._

		val result : Relation[Range] = query
		resetCompiler()
		result
	}

	def query : Relation[Range]

	def table : Table[Domain]

}
