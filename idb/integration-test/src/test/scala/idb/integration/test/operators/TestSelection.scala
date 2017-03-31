package idb.integration.test.operators

import idb.query.QueryEnvironment
import idb.syntax.iql.{IR, _}
import org.junit.Assert._
import org.hamcrest.CoreMatchers._
import org.junit.{Before, Test}
import idb.schema.university.{Course, Registration, Student}
import idb.algebra.print.RelationalAlgebraPrintPlan
import idb.integration.test.UniversityTestData
import idb.integration.test.UniversityDatabase._
import idb.schema.university.Student
import idb.schema.university.Student
import idb.{BagTable, MaterializedView, algebra}


/**
 * This class tests selections. A selection is specified by a simple WHERE clause in the query language.
 *
 * @author Mirko Köhler
 */
class TestSelection extends AbstractStudentOperatorTest[Student] with UniversityTestData {

	val IR = IR

	val printQuery = true

	var query : Relation[Student] = null
	var table : Table[Student] = null


	@Before
	def setUp() {
		implicit val env = QueryEnvironment.Local

		table = BagTable.empty[Student]
		query = compile (
			SELECT (*) FROM table WHERE ((s : Rep[Student]) => s.matriculationNumber < 5 )
		)

	}



	def assertAddToEmptyA(q : MaterializedView[Student]) {
		assertThat (q contains johnDoe, is (true))

		assertThat (q.size, is (1))
	}

	def assertAddToEmptyB(q : MaterializedView[Student]) {
		assertThat (q contains jackBlack, is (false))

		assertThat (q.size, is (0))
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

	def assertUpdateC(q: MaterializedView[Student]) {
		assertThat (q contains johnDoe, is (true))
		assertThat (q contains jackBlack, is (false))

		assertThat (q.size, is (1))
	}

	def assertUpdateD(q: MaterializedView[Student]) {
		assertThat (q contains johnDoe, is (false))
		assertThat (q contains jackBlack, is (false))

		assertThat (q.size, is (0))
	}

	def assertRemove(q: MaterializedView[Student]) {
		assertThat (q contains sallyFields, is (false))

		assertThat (q.size, is (0))
	}

	def assertAddDoubleA(q: MaterializedView[Student]) {
		assertThat (q contains johnDoe, is (true))
		assertThat (q count johnDoe, is (2))

		assertThat (q.size, is (2))

	}

	def assertAddDoubleB(q: MaterializedView[Student]) {
		assertThat (q contains jackBlack, is (false))

		assertThat (q.size, is (0))

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
		assertThat (q contains sallyFields, is (true))
		assertThat (q count johnDoe, is (1))
		assertThat (q count sallyFields, is (2))

		assertThat (q.size, is (3))
	}

	def assertRemoveFromTriple(q: MaterializedView[Student]) {
		assertThat (q contains johnDoe, is (true))
		assertThat (q contains sallyFields, is (true))
		assertThat (q count johnDoe, is (1))
		assertThat (q count sallyFields, is (1))

		assertThat (q.size, is (2))
	}

	def assertRemoveTwice(q: MaterializedView[Student]) = {
		assertThat (q contains johnDoe, is (false))
		assertThat (q contains sallyFields, is (false))

		assertThat (q.size, is (0))
	}

	def assertReadd(q: MaterializedView[Student]) {
		assertThat (q contains johnDoe, is (true))
		assertThat (q count johnDoe, is (1))

		assertThat (q.size, is (1))
	}

	/*
		Additional selection tests
	 */
	@Test
	def testAddToEmptyB() {
		val q = getQuery.asMaterialized
		val e = table

		//Test
		e += jackBlack
		e.endTransaction()

		assertAddToEmptyB(q)
	}

	@Test
	def testUpdateC() {
		val q = getQuery.asMaterialized
		val e = table

		//SetUp
		e += jackBlack
		e.endTransaction()

		//Test
		e ~= (jackBlack, johnDoe)
		e.endTransaction()

		assertUpdateC(q)
	}

	@Test
	def testUpdateD() {
		val q = getQuery.asMaterialized
		val e = table

		//SetUp
		e += johnDoe
		e.endTransaction()

		//Test
		e ~= (johnDoe, jackBlack)
		e.endTransaction()

		assertUpdateD(q)
	}

	@Test
	def testAddDoubleB() {
		val q = getQuery.asMaterialized
		val e = table

		//SetUp
		e += jackBlack
		e.endTransaction()

		//Test
		e += jackBlack
		e.endTransaction()

		assertAddDoubleB(q)
	}
}
