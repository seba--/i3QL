package idb.integration.test.operators

import idb.query.QueryEnvironment
import idb.syntax.iql._
import org.junit.Assert._
import org.hamcrest.CoreMatchers._
import org.junit.{Before, Test}
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
class TestSelfMaintainedAggregationUngrouped extends AbstractStudentOperatorTest[Int] with UniversityTestData {

	val IR = idb.syntax.iql.IR

	val printQuery = true

	var query : Relation[Int] = null
	var table : Table[Student] = null

	@Before
	def setUp() {
		implicit val queryEnvironment = QueryEnvironment.Local

		table = BagTable.empty[Student]
		query = compile (
			SELECT (SUM ((s : Rep[Student]) => s.matriculationNumber)) FROM table
		)
	}



	def assertAddToEmptyA(q : MaterializedView[Int]) {
		assertThat (q contains 2, is (true))
		assertThat (q.size, is (1))
	}

	def assertAddToFilled(q: MaterializedView[Int]) {
		assertThat (q contains 3, is (true))

		assertThat (q.size, is (1))
	}

	def assertUpdateA(q: MaterializedView[Int]) {
		assertThat (q contains 1, is (true))

		assertThat (q.size, is (1))
	}

	def assertUpdateB(q: MaterializedView[Int]) {
		assertThat (q contains 2, is (true))

		assertThat (q.size, is (1))
	}

	def assertRemove(q: MaterializedView[Int]) {
		//(assertThat (q contains 0, is (true))

		assertThat (q.size, is (0))
	}

	def assertAddDoubleA(q: MaterializedView[Int]) {
		assertThat (q contains 4, is (true))

		assertThat (q.size, is (1))

	}

	def assertUpdateDouble(q: MaterializedView[Int]) {
    	assertThat (q contains 3, is (true))

		assertThat (q.size, is (1))
	}


	def assertRemoveDouble(q: MaterializedView[Int]) {
		assertThat (q contains 2, is (true))

		assertThat (q.size, is (1))
	}

	def assertRemoveNonEmptyResultA(q: MaterializedView[Int]) {
		assertThat (q contains 1, is (true))

		assertThat (q.size, is (1))
	}

	def assertRemoveNonEmptyResultB(q: MaterializedView[Int]) {
		assertThat (q contains 2, is (true))

		assertThat (q.size, is (1))
	}

	def assertUpdateTriple(q: MaterializedView[Int]) {
		assertThat (q contains 4, is (true))

		assertThat (q.size, is (1))
	}

	def assertRemoveFromTriple(q: MaterializedView[Int]) {
		assertThat (q contains 3, is (true))

		assertThat (q.size, is (1))
	}

	def assertRemoveTwice(q: MaterializedView[Int]) = {
		//assertThat (q contains 0, is (true))

		assertThat (q.size, is (0))
	}

	def assertReadd(q: MaterializedView[Int]) {
		assertThat (q contains 2, is (true))

		assertThat (q.size, is (1))
	}

}
