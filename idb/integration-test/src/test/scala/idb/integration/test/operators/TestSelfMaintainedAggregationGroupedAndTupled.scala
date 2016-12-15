package idb.integration.test.operators

import idb.query.QueryEnvironment
import idb.syntax.iql._
import org.junit.Assert._
import org.hamcrest.CoreMatchers._
import org.junit.{Test, Before}
import idb.syntax.iql.IR._
import idb.integration.test.UniversityTestData
import idb.integration.test.UniversityDatabase._
import idb.schema.university.Student
import idb.{BagTable, MaterializedView}


/**
 * This class tests selections. A selection is specified by a simple WHERE clause in the query language.
 *
 * @author Mirko Köhler
 */
class TestSelfMaintainedAggregationGroupedAndTupled extends AbstractStudentOperatorTest[(String, Int)] with UniversityTestData {

	val IR = idb.syntax.iql.IR

	val printQuery = true

	var query : Relation[(String, Int)] = null
	var table : Table[Student] = null

	@Before
	def setUp() {
		implicit val env = QueryEnvironment.Local

		table = BagTable.empty[Student]
		query = compile (
			SELECT ((s : Rep[String]) => s, SUM ((s : Rep[Student]) => s.matriculationNumber)) FROM table GROUP BY ((s : Rep[Student]) => s.lastName)
		)
	}



	def assertAddToEmptyA(q : MaterializedView[(String, Int)]) {
		assertThat (q contains ("Doe", 2), is (true))

		assertThat (q.size, is (1))
	}

	def assertAddToFilled(q: MaterializedView[(String, Int)]) {
		assertThat (q contains ("Fields", 1), is (true))
		assertThat (q contains ("Doe", 2), is (true))
		assertThat (q.size, is (2))
	}

	def assertUpdateA(q: MaterializedView[(String, Int)]) {
		assertThat (q contains ("Fields", 1), is (true))
		assertThat (q.size, is (1))
	}

	def assertUpdateB(q: MaterializedView[(String, Int)]) {
		assertThat (q contains ("Doe", 2), is (true))

		assertThat (q.size, is (1))
	}

	def assertRemove(q: MaterializedView[(String, Int)]) {
		//(assertThat (q contains 0, is (true))

		assertThat (q.size, is (0))
	}

	def assertAddDoubleA(q: MaterializedView[(String, Int)]) {
		assertThat (q contains ("Doe", 4), is (true))

		assertThat (q.size, is (1))

	}

	def assertUpdateDouble(q: MaterializedView[(String, Int)]) {
		assertThat (q contains ("Doe", 2), is (true))
		assertThat (q contains ("Fields", 1), is (true))

		assertThat (q.size, is (2))
	}


	def assertRemoveDouble(q: MaterializedView[(String, Int)]) {
		assertThat (q contains ("Doe", 2), is (true))

		assertThat (q.size, is (1))
	}

	def assertRemoveNonEmptyResultA(q: MaterializedView[(String, Int)]) {
		assertThat (q contains ("Fields", 1), is (true))

		assertThat (q.size, is (1))
	}

	def assertRemoveNonEmptyResultB(q: MaterializedView[(String, Int)]) {
		assertThat (q contains ("Doe", 2), is (true))

		assertThat (q.size, is (1))
	}

	def assertUpdateTriple(q: MaterializedView[(String, Int)]) {
		assertThat (q contains ("Fields", 2), is (true))
		assertThat (q contains ("Doe", 2), is (true))

		assertThat (q.size, is (2))
	}

	def assertRemoveFromTriple(q: MaterializedView[(String, Int)]) {
		assertThat (q contains ("Fields", 1), is (true))
		assertThat (q contains ("Doe", 2), is (true))

		assertThat (q.size, is (2))
	}

	def assertRemoveTwice(q: MaterializedView[(String, Int)]) = {
		//assertThat (q contains 0, is (true))

		assertThat (q.size, is (0))
	}

	def assertReadd(q: MaterializedView[(String, Int)]) {
		assertThat (q contains ("Doe", 2), is (true))

		assertThat (q.size, is (1))
	}

	def assertAddGrouped(q: MaterializedView[(String, Int)]) {
		assertThat (q contains ("Fields", 1), is (true))
		assertThat (q contains ("Doe", 7), is (true))

		assertThat (q.size, is (2))
	}

	def assertRemoveGrouped(q: MaterializedView[(String, Int)]) {
		assertThat (q contains ("Fields", 1), is (true))
		assertThat (q contains ("Doe", 2), is (true))

		assertThat (q.size, is (2))
	}

	def assertUpdateGroupedA(q: MaterializedView[(String, Int)]) {
		assertThat (q contains ("Fields", 6), is (true))
		assertThat (q contains ("Doe", 2), is (true))

		assertThat (q.size, is (2))
	}

	def assertUpdateGroupedB(q: MaterializedView[(String, Int)]) {
		assertThat (q contains ("Doe", 4), is (true))
		assertThat (q contains ("Fields", 1), is (true))

		assertThat (q.size, is (2))
	}

	/*
		Additional aggregation tests
	 */
	@Test
	def testAddGrouped() {
		val q = query.asMaterialized
		val e = table

		//SetUp
		e += johnDoe
		e += sallyFields
		e.endTransaction()

		//Test
		e += janeDoe
		e.endTransaction()

		assertAddGrouped(q)
	}



	@Test
	def testRemoveGrouped() {
		val q = query.asMaterialized
		val e = table

		//SetUp
		e += johnDoe
		e += sallyFields
		e += janeDoe
		e.endTransaction()

		//Test
		e -= janeDoe
		e.endTransaction()

		assertRemoveGrouped(q)
	}



	@Test
	def testUpdateGroupedA() {
		val q = query.asMaterialized
		val e = table

		//SetUp
		e += johnDoe
		e += sallyFields
		e += janeDoe
		e.endTransaction()

		//Test
		e ~= (janeDoe, janeFields)
		e.endTransaction()

		assertUpdateGroupedA(q)
	}



	@Test
	def testUpdateGroupedB() {
		val q = query.asMaterialized
		val e = table

		//SetUp
		e += johnDoe
		e += sallyFields
		e += janeDoe
		e.endTransaction()

		//Test
		e ~= (janeDoe, johnDoe)
		e.endTransaction()

		assertUpdateGroupedB(q)
	}






}
