package idb.integration.test.operators

import idb.syntax.iql._
import org.junit.Assert._
import org.hamcrest.CoreMatchers._
import org.junit.Test
import idb.schema.university.{Registration, Student}
import idb.syntax.iql.IR._
import idb.algebra.print.RelationalAlgebraPrintPlan
import idb.integration.test.UniversityTestData
import idb.integration.test.UniversityDatabase._
import idb.schema.university.Student
import idb.schema.university.Registration


/**
 * This class tests selections. A selection is specified by a WHERE clause in the query language
 * which compares elements of different relations.
 *
 * @author Mirko Köhler
 */
class TestEquiJoin extends UniversityTestData with RelationalAlgebraPrintPlan {

	val IR = idb.syntax.iql.IR

	val printQuery = true

	@Test
	def testQuery2 () {
		//Initialize query
		val queryUncompiled = plan(
			SELECT (
				*
			) FROM (
				students, registrations
			) WHERE (
				(s : Rep[Student], r : Rep[Registration]) => s.matriculationNumber == r.studentMatriculationNumber
			)
		)

		if (printQuery)	Predef.println(quoteRelation(queryUncompiled))

		val query = compile(queryUncompiled).asMaterialized

		//Add one element to left relation

		//Add one element to right relation

		//Add element to left relation that is filtered.

		//Add element to right relation that is filtered

		//Add more elements

		//Update element of left relation

		//Update element of right relation

		//Add double element to left relation

		//Add double element to right relation

		//Update double element of left relation

		//Update double element of right relation

		//Remove element of left relation

		//Add last removed element again

		//Remove element of right relation

		//Remove all elements of left relation

		//Re-add the last removed elements

		//Remove all elements of right relation

		//Remove all elements of left relation
	}
}
