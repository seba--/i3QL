package idb.integration.test

import java.util
import java.util.Date

import akka.actor.ActorSystem
import idb.query._
import idb.{SetTable, Table, BagTable}
import idb.algebra.ir._
import idb.algebra.print.RelationalAlgebraPrintPlan
import idb.annotations.Remote
import idb.lms.extensions.FunctionUtils
import idb.lms.extensions.operations.{SeqOpsExpExt, StringOpsExpExt, OptionOpsExp}
import idb.schema.university.{Registration, Student}
import org.junit.Assert._
import org.junit.Test
import org.junit.Ignore
import idb.syntax.iql._
import UniversityDatabase._
import org.hamcrest.CoreMatchers._

import scala.virtualization.lms.common.{TupledFunctionsExp, StaticDataExp, StructExp, ScalaOpsPkgExp}


/**
 * @author Mirko Köhler
 */
class TestRemote extends UniversityTestData {


	@Test
	def testRemote(): Unit = {
		//Initialize query context as implicit value
		implicit val queryEnvironment = QueryEnvironment.create(
			actorSystem = ActorSystem("test1")
		)

		//Initialize remote tables
		//I. Using annotations ...
		@Remote(description = "registrations", host = "RegistrationHost")
		class RemoteRegistrations extends BagTable[Registration]

		val remoteRegistrations = new RemoteRegistrations

		//II. using direct call to table ...
		import idb.syntax.iql.IR._

		val studentTable = BagTable.empty[Student]

		val remoteStudents = table(
			table = studentTable,
			isSet = false,
			host = RemoteHost("StudentServer"),
			remote = NameDescription("students")
		)


		//Query
		val q =
			plan(
				SELECT (*) FROM (remoteStudents, remoteRegistrations)
			)


		/*
		val printer = new RelationalAlgebraPrintPlan {
			override val IR = idb.syntax.iql.IR
		}
		Predef.println(printer.quoteRelation(q))
		*/


		val compiledQ = compile(q).asMaterialized

		//Add data to tables
		studentTable.add(johnDoe)
		remoteRegistrations.add(johnTakesEise)

		//Wait for actors
		Thread.sleep(500)

		//Test output
		assertThat (compiledQ.size, is (1))
		assertThat (compiledQ.contains((johnDoe, johnTakesEise)), is (true))

		//Close context
		queryEnvironment.close()
	}


	@Test
	@Ignore //TODO This test fails if all tests are run. This may have something to do with compiler resetting.
	def testRemote2(): Unit = {

		implicit val queryEnvironment = QueryEnvironment.create(
			actorSystem = ActorSystem("test2")
		)

		@Remote(description = "students", host = "StudentServer")
		class RemoteStudents extends BagTable[Student]

		@Remote(description = "registrations", host = "RegistrationHost")
		class RemoteRegistrations extends BagTable[Registration]

		val remoteStudents = new RemoteStudents
		val remoteRegistrations = new RemoteRegistrations

		import idb.syntax.iql.IR._

		val q =
			plan(
				SELECT (*) FROM (remoteStudents, remoteStudents, remoteRegistrations, remoteStudents)
			)


		val compiledQ = compile(q).asMaterialized

		remoteStudents.add(johnDoe)
		remoteRegistrations.add(johnTakesEise)

		//Wait for actors
		Thread.sleep(500)

		//Test output
		assertThat (compiledQ.size, is (1))
		assertThat (compiledQ.contains((johnDoe, johnDoe, johnTakesEise, johnDoe)), is (true))

		//Close context
		queryEnvironment.close()
	}

	@Test
	def testRemote3(): Unit = {
		implicit val queryEnvironment = QueryEnvironment.create(
			actorSystem = ActorSystem("test3")
		)

		@Remote(description = "students", host = "StudentServer")
		class RemoteStudents extends BagTable[Student]

		@Remote(description = "registrations", host = "RegistrationHost")
		class RemoteRegistrations extends BagTable[Registration]

		val remoteStudents = new RemoteStudents
		val remoteRegistrations = new RemoteRegistrations

		import idb.syntax.iql.IR._

		val q =
			plan(
				SELECT ((i: Rep[Int]) => i)
					FROM (remoteStudents, remoteStudents, remoteRegistrations, remoteRegistrations)
					WHERE ((s1, s2, r1, r2) =>
						s1.matriculationNumber == r1.studentMatriculationNumber AND
						s2.matriculationNumber == r2.studentMatriculationNumber AND
						r1.courseNumber == r2.courseNumber
					)
					GROUP BY ((s1: Rep[Student], s2: Rep[Student], r1: Rep[Registration], r2 : Rep[Registration]) => r1.courseNumber)
			)


		val compiledQ = compile(q).asMaterialized

		remoteStudents.add(sallyFields)
		remoteRegistrations.add(sallyTakesIcs1)
		remoteStudents.add(jackBlack)
		remoteRegistrations.add(jackTakesIcs1)
		remoteRegistrations.add(sallyTakesIcs2)
		remoteRegistrations.add(jackTakesIcs2)

		//Wait for actors
		Thread.sleep(500)

		//Test output
		assertThat (compiledQ.size, is (2))
		assertThat (compiledQ.contains(1), is (true))
		assertThat (compiledQ.contains(2), is (true))


		//Close context
		queryEnvironment.close()
	}

	@Test
	def testRemote4(): Unit = {
		implicit val queryEnvironment = QueryEnvironment.create(
			actorSystem = ActorSystem("test4")
		)

		@Remote(description = "students", host = "StudentServer")
		class RemoteStudents extends BagTable[Student]

		val remoteStudents = new RemoteStudents

		import idb.syntax.iql.IR._

		val q =
			plan(
				SELECT (COUNT(*))
				FROM remoteStudents
				WHERE (s1 =>
					s1.lastName == "Doe"
				)

		)

		val compiledQ = compile(q).asMaterialized

		remoteStudents += johnDoe
		remoteStudents += sallyFields

		//Wait for actors
		Thread.sleep(500)

		//Test output
		assertThat (compiledQ.size, is (1))
		assertThat (compiledQ.contains(1), is (true))


		//Close context
		queryEnvironment.close()
	}


	@Ignore
	@Test
	def testRemoteAirports(): Unit = {
		import idb.syntax.iql.IR._

		implicit val queryEnvironment = QueryEnvironment.create(
			actorSystem = ActorSystem("test")
		)

		@idb.annotations.Remote(description = "airports", host = "AirportServer")
		object RemoteAirports extends SetTable[Airport]

		@idb.annotations.Remote(description = "flights", host = "FlightServer")
		object RemoteFlights extends SetTable[Flight]


		val q = plan(
			SELECT ((s: Rep[String]) => s,
				COUNT(*))
				FROM (RemoteAirports, RemoteAirports, RemoteFlights)
				WHERE ((a1, a2, f) =>
				f.from == a1.id AND
					f.to == a2.id AND
					a2.code == "PDX" AND
					f.takeoff >= new Date(2014, 1, 1) AND
					f.takeoff < new Date(2015, 1, 1))
				GROUP BY ((a1: Rep[Airport], a2: Rep[Airport], f: Rep[Flight]) => a1.city)
			)

		val printer = new RelationalAlgebraPrintPlan {
			override val IR = idb.syntax.iql.IR
		}

		val compiledQ = compile(q).asMaterialized

		Predef.println(printer.quoteRelation(q))
		Predef.println("\n")
		Predef.println(compiledQ.prettyprint("\t"))
		Predef.println("\n")


		compiledQ.foreach(x => Predef.println(x))
		Predef.println("\n")
	}

}
