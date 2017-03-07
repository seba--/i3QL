package idb.integration.test.operators

import idb.algebra
import idb.query.QueryEnvironment
import idb.syntax.iql._
import org.junit.Assert._
import org.hamcrest.CoreMatchers._
import org.junit.Test
import idb.schema.university.{Registration, Student}
import idb.algebra.IR._
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
class TestEquiJoin extends UniversityTestData {

	val IR = algebra.IR

	val printQuery = true

	@Test
	def testQuery2 () {
		implicit val env = QueryEnvironment.Local

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

	//	if (printQuery)	Predef.println(quoteRelation(queryUncompiled))

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

		//Add more elements
		students += sallyFields += jackBlack += janeDoe
		students.endTransaction()
		registrations += sallyTakesIcs1 += jackTakesIcs1 += johannaTakesIcs1
		registrations.endTransaction()

		assertThat (query contains (johnDoe, johnTakesEise), is (true))
		assertThat (query contains (sallyFields, sallyTakesIcs1), is (true))
		assertThat (query contains (jackBlack, jackTakesIcs1), is (true))
		query.foreach ((sr : (Student, Registration)) => assertThat (sr._1, is (not (equalTo (janeDoe)))))
		query.foreach ((sr : (Student, Registration)) => assertThat (sr._2, is (not (equalTo (johannaTakesIcs1)))))

		assertThat (query.size, is (3))

		//Update element of left relation
		students ~= (johnDoe, johnFields)
		students.endTransaction()

		assertThat (query contains (johnFields, johnTakesEise), is (true))
		assertThat (query contains (sallyFields, sallyTakesIcs1), is (true))
		assertThat (query contains (jackBlack, jackTakesIcs1), is (true))

		assertThat (query contains (johnDoe, johnTakesEise), is (false))

		assertThat (query.size, is (3))

		//Update element of right relation
		registrations ~= (sallyTakesIcs1, sallyTakesIcs2)
		registrations.endTransaction()

		assertThat (query contains (johnFields, johnTakesEise), is (true))
		assertThat (query contains (sallyFields, sallyTakesIcs2), is (true))
		assertThat (query contains (jackBlack, jackTakesIcs1), is (true))

		assertThat (query contains (sallyFields, sallyTakesIcs1), is (false))

		assertThat (query.size, is (3))

		//Add double element to left relation
		students += jackBlack
		students.endTransaction()

		assertThat (query contains (johnFields, johnTakesEise), is (true))
		assertThat (query contains (sallyFields, sallyTakesIcs2), is (true))
		assertThat (query contains (jackBlack, jackTakesIcs1), is (true))

		assertThat (query count (jackBlack, jackTakesIcs1), is (2))

		assertThat (query.size, is (4))

		//Add double element to right relation
		registrations += sallyTakesIcs2
		registrations.endTransaction()

		assertThat (query contains (johnFields, johnTakesEise), is (true))
		assertThat (query contains (sallyFields, sallyTakesIcs2), is (true))
		assertThat (query contains (jackBlack, jackTakesIcs1), is (true))

		assertThat (query count (jackBlack, jackTakesIcs1), is (2))
		assertThat (query count (sallyFields, sallyTakesIcs2), is (2))

		assertThat (query.size, is (5))

		//TODO How to handle updates of double elements?
		//Update double element of left relation
	/*	students ~= (jackBlack, jackCarter)
		students.endTransaction()

		assertThat (query contains (johnFields, johnTakesEise), is (true))
		assertThat (query contains (sallyFields, sallyTakesIcs2), is (true))
		assertThat (query contains (jackBlack, jackTakesIcs1), is (true))
		assertThat (query contains (jackCarter, jackTakesIcs1), is (true))

		assertThat (query count (jackBlack, jackTakesIcs1), is (1))
		assertThat (query count (sallyFields, sallyTakesIcs2), is (2))

		assertThat (query.size, is (5))

		//Update double element of right relation
		registrations ~= (sallyTakesIcs2, sallyTakesIcs1)
		registrations.endTransaction()

		assertThat (query contains (johnFields, johnTakesEise), is (true))
		assertThat (query contains (sallyFields, sallyTakesIcs2), is (true))
		assertThat (query contains (jackBlack, jackTakesIcs1), is (true))
		assertThat (query contains (jackCarter, jackTakesIcs1), is (true))
		assertThat (query contains (sallyFields, sallyTakesIcs1), is (true))

		assertThat (query count (sallyFields, sallyTakesIcs2), is (1))

		assertThat (query.size, is (5))

		//Remove element of left relation
		students -= jackCarter
		students.endTransaction()

		assertThat (query contains (johnFields, johnTakesEise), is (true))
		assertThat (query contains (sallyFields, sallyTakesIcs2), is (true))
		assertThat (query contains (jackBlack, jackTakesIcs1), is (true))
		assertThat (query contains (sallyFields, sallyTakesIcs1), is (true))

		assertThat (query.size, is (4))

		//Remove element of right relation
		registrations -= sallyTakesIcs1
		registrations.endTransaction()

		assertThat (query contains (johnFields, johnTakesEise), is (true))
		assertThat (query contains (sallyFields, sallyTakesIcs2), is (true))
		assertThat (query contains (jackBlack, jackTakesIcs1), is (true))

		assertThat (query.size, is (3))

		//Remove all elements of left relation
		students -= johnFields -= sallyFields -= jackBlack -= janeDoe
		students.endTransaction()

		assertThat (query.size, is (0))

		//Re-add the last removed elements
		students += johnFields += sallyFields += jackBlack += janeDoe
		students.endTransaction()

		assertThat (query contains (johnFields, johnTakesEise), is (true))
		assertThat (query contains (sallyFields, sallyTakesIcs2), is (true))
		assertThat (query contains (jackBlack, jackTakesIcs1), is (true))

		assertThat (query.size, is (3))

		//Remove all elements of right relation
		registrations -= johnTakesEise -= sallyTakesIcs2 -= jackTakesIcs1 -= johannaTakesIcs1
		registrations.endTransaction()

		assertThat (query.size, is (0))

		//Remove all elements of left relation
		students += johnFields += sallyFields += jackBlack += janeDoe
		students.endTransaction()

		assertThat (query.size, is (0))  */

	}
}
