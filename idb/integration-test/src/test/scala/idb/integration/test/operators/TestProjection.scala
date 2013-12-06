package idb.integration.test.operators

import idb.syntax.iql._
import org.junit.Assert._
import org.hamcrest.CoreMatchers._
import org.junit.{Before, Test}
import idb.syntax.iql.IR._
import idb.integration.test.UniversityTestData
import idb.integration.test.UniversityDatabase._
import idb.schema.university.Student
import idb.{BagExtent, MaterializedView}


/**
 * This class tests selections. A selection is specified by a simple WHERE clause in the query language.
 *
 * @author Mirko Köhler
 */
class TestProjection extends UniversityTestData
	with AbstractStudentOperatorTest[String] {

	val IR = idb.syntax.iql.IR

	val printQuery = true

	var query : Relation[String] = null
	var extent : Extent[Student] = null

	@Before
	def setUp() {
		extent = BagExtent.empty[Student]
		query = compile(SELECT ((s : Rep[Student]) => s.lastName) FROM (extent))
	}



	def assertAddToEmptyA(q : MaterializedView[String]) {
		assertThat (q contains "Doe", is (true))
		assertThat (q.size, is (1))
	}

	def assertAddToFilled(q: MaterializedView[String]) {
		assertThat (q contains "Doe", is (true))
		assertThat (q contains "Fields", is (true))

		assertThat (q.size, is (2))
	}

	def assertUpdateA(q: MaterializedView[String]) {
		assertThat (q contains "Doe", is (false))
		assertThat (q contains "Fields", is (true))

		assertThat (q.size, is (1))
	}

	def assertUpdateB(q: MaterializedView[String]) {
		assertThat (q contains "Doe", is (true))
		assertThat (q contains "Fields", is (false))

		assertThat (q.size, is (1))
	}

	def assertUpdateC(q: MaterializedView[String]) {
		assertThat (q contains "Doe", is (true))
		assertThat (q count "Doe", is (1))

		assertThat (q.size, is (1))
	}

	def assertRemove(q: MaterializedView[String]) {
		assertThat (q contains "Fields", is (false))

		assertThat (q.size, is (0))
	}

	def assertAddDoubleA(q: MaterializedView[String]) {
		assertThat (q contains "Doe", is (true))
		assertThat (q count "Doe", is (2))

		assertThat (q.size, is (2))

	}

	def assertAddDoubleB(q: MaterializedView[String]) {
		assertThat (q contains "Doe", is (true))
		assertThat (q count "Doe", is (2))

		assertThat (q.size, is (2))

	}

	def assertUpdateDouble(q: MaterializedView[String]) {
    	assertThat (q contains "Doe", is (true))
		assertThat (q contains "Fields", is (true))
		assertThat (q count "Doe", is (1))

		assertThat (q.size, is (2))
	}


	def assertRemoveDouble(q: MaterializedView[String]) {
		assertThat (q contains "Doe", is (true))
		assertThat (q count "Doe", is (1))

		assertThat (q.size, is (1))
	}

	def assertRemoveNonEmptyResultA(q: MaterializedView[String]) {
		assertThat (q contains "Doe", is (false))
		assertThat (q contains "Fields", is (true))

		assertThat (q.size, is (1))
	}

	def assertRemoveNonEmptyResultB(q: MaterializedView[String]) {
		assertThat (q contains "Doe", is (true))
		assertThat (q contains "Fields", is (false))

		assertThat (q.size, is (1))
	}

	def assertUpdateTriple(q: MaterializedView[String]) {
		assertThat (q contains "Doe", is (true))
		assertThat (q contains "Fields", is (true))
		assertThat (q count "Doe", is (1))
		assertThat (q count "Fields", is (2))

		assertThat (q.size, is (3))
	}

	def assertRemoveFromTriple(q: MaterializedView[String]) {
		assertThat (q contains "Doe", is (true))
		assertThat (q contains "Fields", is (true))
		assertThat (q count "Doe", is (1))
		assertThat (q count "Fields", is (1))

		assertThat (q.size, is (2))
	}

	def assertRemoveTwice(q: MaterializedView[String]) = {
		assertThat (q contains "Doe", is (false))
		assertThat (q contains "Fields", is (false))

		assertThat (q.size, is (0))
	}

	def assertReadd(q: MaterializedView[String]) {
		assertThat (q contains "Doe", is (true))
		assertThat (q count "Doe", is (1))

		assertThat (q.size, is (1))
	}

	/*
		Additional projection tests
	 */

	@Test
	def testAddDoubleB() {
		val q = query.asMaterialized
		val e = extent

		//SetUp
		e += janeDoe
		e.endTransaction()

		//Test
		e += johnDoe
		e.endTransaction()

		assertAddDoubleB(q)
	}

	@Test
	def testUpdateC() {
		val q = query.asMaterialized
		val e = extent

		//SetUp
		e += janeDoe
		e.endTransaction()

		//Test
		e ~= (janeDoe, johnDoe)
		e.endTransaction()

		assertUpdateC(q)
	}

}
