package idb.integration.test.operators

import akka.actor.ActorSystem
import idb.query.QueryEnvironment$
import idb.schema.university.Student
import org.junit.Test
import idb.integration.test.UniversityTestData
import idb.{MaterializedView, Relation}

/**
 * Abstract test for queries with an underlying relation of students. The tests are testing non-transactional
 * behaviour of the queries.
 *
 * @author Mirko Köhler
 */
trait AbstractStudentOperatorTest[Range] extends AbstractOperatorTest[Student, Range] with UniversityTestData {

	//implicit val queryContext = QueryContext.noRemote

	@Test
	def testAddToEmptyA() {
		val q = getQuery.asMaterialized
		val e = table

		//Test
		e += johnDoe
		e.endTransaction()

		assertAddToEmptyA(q)
	}

	def assertAddToEmptyA(q : MaterializedView[Range])

	@Test(expected = classOf[IllegalStateException])
	def testRemoveFromEmpty() {
		val q = getQuery.asMaterialized
		val e = table

		//Test
		e -= johnDoe
		e.endTransaction()

	}

	@Test(expected = classOf[IllegalStateException])
	def testUpdateEmpty() {
		val q = getQuery.asMaterialized
		val e = table

		//Test
		e ~= (johnDoe, johnFields)
		e.endTransaction()

	}

	@Test
	def testAddToFilledA() {
		val q = getQuery.asMaterialized
		val e = table

		//SetUp
		e += johnDoe
		e.endTransaction()

		//Test
		e += sallyFields
		e.endTransaction()

		assertAddToFilled(q)
	}

	@Test
	def testAddToFilledB() {
		val q = getQuery.asMaterialized
		val e = table

		//SetUp
		e += sallyFields
		e.endTransaction()

		//Test
		e += johnDoe
		e.endTransaction()

		assertAddToFilled(q)
	}

	def assertAddToFilled(q : MaterializedView[Range])

	@Test
	def testUpdateA() {
		val q = getQuery.asMaterialized
		val e = table

		//SetUp
		e += johnDoe
		e.endTransaction()

		//Test
		e ~= (johnDoe, sallyFields)
		e.endTransaction()

		assertUpdateA(q)
	}

	def assertUpdateA(q : MaterializedView[Range])

	@Test
	def testUpdateB() {
		val q = getQuery.asMaterialized
		val e = table

		//SetUp
		e += sallyFields
		e.endTransaction()

		//Test
		e ~= (sallyFields, johnDoe)
		e.endTransaction()

		assertUpdateB(q)
	}

	def assertUpdateB(q : MaterializedView[Range])

	@Test
	def testRemove() {
		val q = getQuery.asMaterialized
		val e = table

		//SetUp
		e += sallyFields
		e.endTransaction()

		//Test
		e -= sallyFields
		e.endTransaction()

		assertRemove(q)
	}

	def assertRemove(q : MaterializedView[Range])


	@Test
	def testAddDoubleA() {
		val q = getQuery.asMaterialized
		val e = table

		//SetUp
		e += johnDoe
		e.endTransaction()

		//Test
		e += johnDoe
		e.endTransaction()

		assertAddDoubleA(q)
	}

	def assertAddDoubleA(q : MaterializedView[Range])

	@Test
	def testUpdateDouble() {
		val q = getQuery.asMaterialized
		val e = table

		//SetUp
		e += johnDoe
		e += johnDoe
		e.endTransaction()

		//Test
		e ~= (johnDoe, sallyFields)
		e.endTransaction()

		assertUpdateDouble(q)
	}

	def assertUpdateDouble(q : MaterializedView[Range])

	@Test
	def testRemoveDouble() {
		val q = getQuery.asMaterialized
		val e = table

		//SetUp
		e += johnDoe
		e += johnDoe
		e.endTransaction()

		//Test
		e -= johnDoe
		e.endTransaction()

		assertRemoveDouble(q)
	}

	def assertRemoveDouble(q : MaterializedView[Range])

	@Test
	def testRemoveNonEmptyResultA() {
		val q = getQuery.asMaterialized
		val e = table

		//SetUp
		e += johnDoe
		e += sallyFields
		e.endTransaction()

		//Test
		e -= johnDoe
		e.endTransaction()

		assertRemoveNonEmptyResultA(q)
	}

	def assertRemoveNonEmptyResultA(q : MaterializedView[Range])

	@Test
	def testRemoveNonEmptyResultB() {
		val q = getQuery.asMaterialized
		val e = table

		//SetUp
		e += johnDoe
		e += sallyFields
		e.endTransaction()

		//Test
		e -= sallyFields
		e.endTransaction()

		assertRemoveNonEmptyResultB(q)
	}

	def assertRemoveNonEmptyResultB(q : MaterializedView[Range])

	@Test
	def testUpdateTriple() {
		val q = getQuery.asMaterialized
		val e = table

		//SetUp
		e += johnDoe
		e += johnDoe
		e += sallyFields
		e.endTransaction()

		//Test
		e ~= (johnDoe, sallyFields)
		e.endTransaction()

		assertUpdateTriple(q)
	}

	def assertUpdateTriple(q : MaterializedView[Range])

	@Test
	def testRemoveFromTriple() {
		val q = getQuery.asMaterialized
		val e = table

		//SetUp
		e += johnDoe
		e += johnDoe
		e += sallyFields
		e.endTransaction()

		//Test
		e -= johnDoe
		e.endTransaction()

		assertRemoveFromTriple(q)
	}

	def assertRemoveFromTriple(q : MaterializedView[Range])

	@Test
	def testRemoveTwice() {
		val q = getQuery.asMaterialized
		val e = table

		//SetUp
		e += johnDoe
		e += sallyFields
		e.endTransaction()

		//Test
		e -= johnDoe
		e -= sallyFields
		e.endTransaction()

		assertRemoveTwice(q)
	}

	def assertRemoveTwice(q : MaterializedView[Range])

	@Test
	def testReadd() {
		val q = getQuery.asMaterialized
		val e = table

		//SetUp
		e += johnDoe
		e.endTransaction()

		//Test
		e -= johnDoe
		e += johnDoe
		e.endTransaction()

		assertReadd(q)
	}

	def assertReadd(q : MaterializedView[Range])










}
