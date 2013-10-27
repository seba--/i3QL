package idb.integration.test.operators

import idb.syntax.iql._
import org.junit.Assert._
import org.hamcrest.CoreMatchers._
import org.junit.Test
import idb.syntax.iql.IR._
import idb.algebra.print.RelationalAlgebraPrintPlan
import idb.integration.test.UniversityTestData
import idb.integration.test.UniversityDatabase._
import idb.schema.university.{Person, Course, Student, Registration}


/**
 * This class tests selections. A selection is specified by a simple WHERE clause in the query language.
 *
 * @author Mirko Köhler
 */
class TestSelfMaintainedAggregation extends UniversityTestData with RelationalAlgebraPrintPlan {

	val IR = idb.syntax.iql.IR

	val printQuery = true

	@Test
	def testSimpleQuery1 () {
		//Initialize query
		val queryUncompiled = plan(
			SELECT (SUM ((c : Rep[Course]) => c.creditPoints)) FROM (courses)
		)
		if (printQuery)	Predef.println(quoteRelation(queryUncompiled))

		val query = compile(queryUncompiled).asMaterialized

		//Add element
		courses += ics1
		courses.endTransaction()

		assertThat (query contains 9, is (true))
		assertThat (query.size, is (1))

		//Add elements
		courses += ics2 += sedc
		courses.endTransaction()

		Predef.println("****************************")
		query.foreach(Predef.println(_))
		Predef.println("****************************")

		assertThat (query contains 24, is (true))
		assertThat (query.size, is (1))

		//Update element
		courses ~= (ics1, ics1ForPhysics)
		courses.endTransaction()

		assertThat (query contains 20, is (true))
		assertThat (query.size, is (1))

		//Add double element
		courses += ics2
		courses.endTransaction()

		assertThat (query contains 29, is (true))
		assertThat (query.size, is (1))

		//Update double element
		courses ~= (ics2, ics2ForPhysics)
		courses.endTransaction()

		assertThat (query contains 25, is (true))
		assertThat (query.size, is (1))

		//Remove double element
		courses -= ics2ForPhysics
		courses.endTransaction()

		assertThat (query contains 20, is (true))
		assertThat (query.size, is (1))

		//Remove elements
		courses -= ics1ForPhysics -= ics2
		courses.endTransaction()

		assertThat (query contains 6, is (true))
		assertThat (query.size, is (1))

		//Remove all elements
		courses -= sedc
		courses.endTransaction()

		assertThat (query.size, is (0))
	}

	//TODO test aggregation without tuple but with grouping

	@Test
	def testQueryWithGrouping1 () {
		//Initialize query
		val queryUncompiled = plan(
			SELECT ((s : Rep[String]) => s, COUNT (*)) FROM (students) GROUP BY ((s : Rep[Student]) => s.lastName)
		)
		if (printQuery)	Predef.println(quoteRelation(queryUncompiled))

		val query = compile(queryUncompiled).asMaterialized

		//Add element
		students += johnDoe
		students.endTransaction()
		//John Doe

		assertThat (query contains ("Doe", 1), is (true))
		assertThat (query.size, is (1))

		//Add element that does not group
		students += sallyFields
		students.endTransaction()
		//John Doe / Sally Fields

		assertThat (query contains  ("Doe", 1), is (true))
		assertThat (query contains  ("Fields", 1), is (true))

		assertThat (query.size, is (2))

		//Add element that does group
		students += janeDoe += sallyDoe
		students.endTransaction()
		//John Doe, Jane Doe, Sally Doe / Sally Fields

		assertThat (query contains ("Doe", 3), is (true))
		assertThat (query contains ("Fields", 1), is (true))

		assertThat (query.size, is (2))

		//Update element
		students ~= (johnDoe, johnFields)
		students.endTransaction()
		//Jane Doe, Sally Doe / Sally Fields, John Fields

		assertThat (query contains ("Doe", 2), is (true))
		assertThat (query contains ("Fields", 2), is (true))

		assertThat (query.size, is (2))

		//Add double element
		students += sallyFields
		students.endTransaction()
		//Jane Doe, Sally Doe / Sally Fields, Sally Fields, John Fields

		assertThat (query contains ("Doe", 2), is (true))
		assertThat (query contains ("Fields", 3), is (true))

		assertThat (query.size, is (2))

		//Update double element
		students ~= (sallyFields, sallyDoe)
		students.endTransaction()
		//Jane Doe, Sally Doe, Sally Doe / Sally Fields, John Fields

		assertThat (query contains ("Doe", 3), is (true))
		assertThat (query contains ("Fields", 2), is (true))

		assertThat (query.size, is (2))

		//Remove double element
		students -= sallyDoe
		students.endTransaction()
		//Jane Doe, Sally Doe / Sally Fields, John Fields

		assertThat (query contains ("Doe", 2), is (true))
		assertThat (query contains ("Fields", 2), is (true))

		assertThat (query.size, is (2))

		//Remove elements
		students -= sallyDoe -= janeDoe
		students.endTransaction()
		// Sally Fields, John Fields

		assertThat (query contains ("Fields", 2), is (true))

		assertThat (query.size, is (1))

		//Remove all elements
		students -= sallyFields -= johnFields
		students.endTransaction()

		assertThat (query.size, is (0))

	}

	@Test
	def testQueryWithTupledAggregation3 () {
		//Initialize query
		val queryUncompiled = plan(
			SELECT (
				(s : Rep[String]) => s, SUM ((s : Rep[Student], r : Rep[Registration], c : Rep[Course]) => c.creditPoints)
			) FROM (
				students, registrations, courses
			) WHERE (
				(s : Rep[Student], r : Rep[Registration], c : Rep[Course]) => s.matriculationNumber == r.studentMatriculationNumber && r.courseNumber == c.number
			) GROUP BY (
				(s : Rep[Person], r : Rep[Registration], c : Rep[Course]) => s.lastName
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
