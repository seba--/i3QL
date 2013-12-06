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
class TestDuplicateElimination extends UniversityTestData
	with AbstractStudentOperatorTest[Student] {

	val IR = idb.syntax.iql.IR

	val printQuery = true

	var query : Relation[Student] = null
	var extent : Extent[Student] = null

	@Before
	def setUp() {
		extent = BagExtent.empty[Student]
		query = compile(SELECT DISTINCT (*) FROM (extent))
	}


	def assertAddToEmptyA(q: MaterializedView[Student]) {
		assertThat (q contains johnDoe, is (true))
		assertThat (q.size, is (1))
	}

	def assertAddToFilled(q: MaterializedView[Student]) {
		assertThat (q contains johnDoe, is (true))
		assertThat (q contains sallyFields, is (true))

		assertThat (q.size, is (2))
	}

	def assertUpdateA(q: MaterializedView[Student]) {
		assertThat (q contains johnDoe, is (false))
		assertThat (q contains sallyFields, is (true))

		assertThat (q.size, is (1))
	}

	def assertUpdateB(q: MaterializedView[Student]) {
		assertThat (q contains johnDoe, is (true))
		assertThat (q contains sallyFields, is (false))

		assertThat (q.size, is (1))
	}

	def assertRemove(q: MaterializedView[Student]) {
		assertThat (q contains sallyFields, is (false))

		assertThat (q.size, is (0))
	}

	def assertAddDoubleA(q: MaterializedView[Student]) {
		assertThat (q contains johnDoe, is (true))
		assertThat (q count johnDoe, is (1))

		assertThat (q.size, is (1))
	}

	def assertUpdateDouble(q: MaterializedView[Student]) {
		assertThat (q contains johnDoe, is (true))
		assertThat (q contains sallyFields, is (true))
		assertThat (q count johnDoe, is (1))

		assertThat (q.size, is (2))
	}

	def assertRemoveDouble(q: MaterializedView[Student]) {
		assertThat (q contains johnDoe, is (true))
		assertThat (q count johnDoe, is (1))

		assertThat (q.size, is (1))
	}

	def assertRemoveNonEmptyResultA(q: MaterializedView[Student]) {
		assertThat (q contains johnDoe, is (false))
		assertThat (q contains sallyFields, is (true))

		assertThat (q.size, is (1))
	}

	def assertRemoveNonEmptyResultB(q: MaterializedView[Student]) {
		assertThat (q contains johnDoe, is (true))
		assertThat (q contains sallyFields, is (false))

		assertThat (q.size, is (1))
	}

	def assertUpdateTriple(q: MaterializedView[Student]) {
		assertThat (q contains johnDoe, is (true))
		assertThat (q count johnDoe, is (1))
		assertThat (q contains sallyFields, is (true))
		assertThat (q count sallyFields, is (1))

		assertThat (q.size, is (2))
	}

	def assertRemoveFromTriple(q: MaterializedView[Student]) {
		assertThat (q contains johnDoe, is (true))
		assertThat (q count johnDoe, is (1))
		assertThat (q contains sallyFields, is (true))
		assertThat (q count sallyFields, is (1))

		assertThat (q.size, is (2))
	}

	def assertRemoveTwice(q: MaterializedView[Student]) {
		assertThat (q contains johnDoe, is (false))
		assertThat (q contains sallyFields, is (false))

		assertThat (q.size, is (0))
	}

	def assertReadd(q: MaterializedView[Student]) {
		assertThat (q contains johnDoe, is (true))
		assertThat (q count johnDoe, is (1))

		assertThat (q.size, is (1))
	}
}