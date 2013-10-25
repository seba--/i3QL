package idb.integration.test.operators

import idb.syntax.iql._
import org.junit.Assert._
import org.hamcrest.CoreMatchers._
import org.junit.Test
import idb.schema.university.{Registration, Student, Course}
import idb.syntax.iql.IR._
import idb.algebra.print.RelationalAlgebraPrintPlan
import idb.integration.test.UniversityTestData
import idb.integration.test.UniversityDatabase._
import idb.schema.university.Student


/**
 * This class tests selections. A selection is specified by a simple WHERE clause in the query language.
 *
 * @author Mirko Köhler
 */
class TestSelection extends UniversityTestData with RelationalAlgebraPrintPlan {

	val IR = idb.syntax.iql.IR

	val printQuery = true

	@Test
	def testQuery1 () {
		//Initialize query
		val queryUncompiled = plan(
			SELECT (*) FROM (students) WHERE ((s : Rep[Student]) => s.matriculationNumber < 6)
		)
		if (printQuery)	Predef.println(quoteRelation(queryUncompiled))

		val query = compile(queryUncompiled).asMaterialized

		//Add element to relation
		students += johnDoe
		students.endTransaction()

		assertThat (query contains johnDoe, is (true))

		assertThat (query.size, is (1))

		//Add element that is filtered.
		students += jackBlack
		students.endTransaction()

		assertThat (query contains johnDoe, is (true))
		assertThat (query contains jackBlack, is (false))

		assertThat (query.size, is (1))

		//Add elements to relation
		students += judyCarter += jackBlack += janeDoe += johannaOrleans += sallyFields
		students.endTransaction()

		assertThat (query contains johnDoe, is (true))
		assertThat (query contains judyCarter, is (true))
		assertThat (query contains jackBlack, is (false))
		assertThat (query contains janeDoe, is (true))
		assertThat (query contains johannaOrleans, is (false))
		assertThat (query contains sallyFields, is (true))

		assertThat (query.size, is (4))

		//Update element
		students ~= (sallyFields, sallyDoe)
		students.endTransaction()

		assertThat (query contains johnDoe, is (true))
		assertThat (query contains judyCarter, is (true))
		assertThat (query contains janeDoe, is (true))
		assertThat (query contains sallyFields, is (false))
		assertThat (query contains sallyDoe, is (true))

		assertThat (query.size, is (4))

		//Add double element
		students += johnDoe

		assertThat (query contains johnDoe, is (true))
		assertThat (query contains judyCarter, is (true))
		assertThat (query contains janeDoe, is (true))
		assertThat (query contains sallyDoe, is (true))

		assertThat (query count johnDoe, is (2))
		assertThat (query.size, is (5))

		//Update double element
		students ~= (johnDoe, johnFields)
		students.endTransaction()

		assertThat (query contains judyCarter, is (true))
		assertThat (query contains janeDoe, is (true))
		assertThat (query contains sallyDoe, is (true))
		assertThat (query contains johnFields, is (true))

		assertThat (query count johnFields, is (2))
		assertThat (query.size, is (5))

		//Remove double element from the query
		students -= johnFields
		students.endTransaction()

		assertThat (query contains johnFields, is (true))
		assertThat (query contains judyCarter, is (true))
		assertThat (query contains janeDoe, is (true))
		assertThat (query contains sallyDoe, is (true))

		assertThat (query count johnFields, is (1))
		assertThat (query.size, is (4))

		//Remove multiple elements at once
		students -= sallyDoe -= johnFields -= judyCarter
		students.endTransaction()

		assertThat (query contains johnFields, is (false))
		assertThat (query contains judyCarter, is (false))
		assertThat (query contains janeDoe, is (true))
		assertThat (query contains sallyDoe, is (false))

		assertThat (query.size, is (1))

		//Remove all elements
		students -= janeDoe
		students.endTransaction()

		assertThat (query.size, is (0))

	}


/*	@Test
	def testQuery2 () {
		//Initialize query

		val queryUncompiled = plan(
			SELECT (*) FROM (students, registrations)
				WHERE ((s : Rep[Student], r : Rep[Registration]) => (s.matriculationNumber < 6 && r.courseNumber == 1))
		)

		if (printQuery)	Predef.println(quoteRelation(queryUncompiled))

		val query = compile(queryUncompiled).asMaterialized

		//Add one element to left relation
		students += johnDoe
		students.endTransaction()

		assertThat (query.size, is (0))

		//Add one element to right relation
		registrations += johnTakesEise
		registrations.endTransaction()

		assertThat (query contains (johnDoe, johnTakesEise), is (true))
		assertThat (query.size, is (1))

		//Add element to left relation that is filtered.
		students += jackBlack
		students.endTransaction()

		assertThat (query contains (johnDoe, johnTakesEise), is (true))
		query.foreach((sr : (Student, Registration)) => assertThat(sr._1, not (equalTo (jackBlack))))

		assertThat (query.size, is (1))

		//Add element to right relation that is filtered
		registrations += jackTakesIcs1
		registrations.endTransaction()

		assertThat (query contains (johnDoe, johnTakesEise), is (true))
		query.foreach((sr : (Student, Registration)) => assertThat (sr._2, not( equalTo (jackTakesIcs1))))

		assertThat (query.size, is (1))

		//Add more elements
		students += sallyFields += judyCarter += johannaOrleans
		students.endTransaction()
		registrations += sallyTakesIcs1 += judyTakesIcs2 += johannaTakesIcs1
		registrations.endTransaction()

		assertThat (query contains (johnDoe, johnTakesEise), is (true))
		assertThat (query contains (sallyFields, sallyTakesIcs1), is (true))
		assertThat (query contains (judyCarter, judyTakesIcs2), is (true))
		assertThat (query contains (johannaOrleans, johannaTakesIcs1), is (false))

		assertThat (query.size, is (3))

		//Update element of left relation
		students ~= (johnDoe, johnFields)
		students.endTransaction()

		assertThat (query contains (johnFields, johnTakesEise), is (true))
		assertThat (query contains (johnDoe, johnTakesEise), is (false))
		assertThat (query contains (sallyFields, sallyTakesIcs1), is (true))
		assertThat (query contains (judyCarter, judyTakesIcs2), is (true))

		assertThat (query.size, is (3))

		//Update element of right relation
		registrations ~= (johnTakesEise, johnTakesSedc)
		registrations.endTransaction()

		assertThat (query contains (johnFields, johnTakesEise), is (false))
		assertThat (query contains (johnFields, johnTakesSedc), is (true))
		assertThat (query contains (sallyFields, sallyTakesIcs1), is (true))
		assertThat (query contains (judyCarter, judyTakesIcs2), is (true))

		assertThat (query.size, is (3))
		
		//Add double element to left relation
		students += sallyFields
		students.endTransaction()

		assertThat (query contains (johnFields, johnTakesSedc), is (true))
		assertThat (query contains (sallyFields, sallyTakesIcs1), is (true))
		assertThat (query contains (judyCarter, judyTakesIcs2), is (true))

		assertThat (query count (sallyFields, sallyTakesIcs1), is (2))
		assertThat (query.size, is (4))

		//Add double element to right relation
		registrations += sallyTakesIcs1
		registrations.endTransaction()

		assertThat (query contains (johnFields, johnTakesSedc), is (true))
		assertThat (query contains (sallyFields, sallyTakesIcs1), is (true))
		assertThat (query contains (judyCarter, judyTakesIcs2), is (true))

		assertThat (query count (sallyFields, sallyTakesIcs1), is (4))
		assertThat (query.size, is (6))

		//Update double element of left relation
		students ~= (sallyFields, sallyDoe)
		students.endTransaction()

		assertThat (query contains (johnFields, johnTakesSedc), is (true))
		assertThat (query contains (sallyDoe, sallyTakesIcs1), is (false))
		assertThat (query contains (sallyFields, sallyTakesIcs1), is (false))
		assertThat (query contains (judyCarter, judyTakesIcs2), is (true))

		assertThat (query count (sallyFields, sallyTakesIcs1), is (4))
		assertThat (query.size, is (6))
		
		

		//Remove element of left relation
		students -= johnFields
		students.endTransaction()

		assertThat (query contains (johnFields, johnTakesSedc), is (false))
		assertThat (query contains (sallyFields, sallyTakesIcs1), is (true))
		assertThat (query contains (judyCarter, judyTakesIcs2), is (true))

		assertThat (query.size, is (2))

		//Add last removed element again
		students += johnFields
		students.endTransaction()

		assertThat (query contains (johnFields, johnTakesSedc), is (true))
		assertThat (query contains (sallyFields, sallyTakesIcs1), is (true))
		assertThat (query contains (judyCarter, judyTakesIcs2), is (true))

		assertThat (query.size, is (3))

		//Remove element of right relation
		registrations -= johnTakesSedc
		registrations.endTransaction()

		assertThat (query contains (johnFields, johnTakesSedc), is (false))
		assertThat (query contains (sallyFields, sallyTakesIcs1), is (true))
		assertThat (query contains (judyCarter, judyTakesIcs2), is (true))

		assertThat (query.size, is (2))
	} */

}
