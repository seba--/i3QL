package idb.integration.test


import akka.actor.ActorSystem
import idb.query._
import idb.BagTable
import idb.schema.university.{Registration, Student}
import org.junit.Assert._
import org.junit.Test
import org.junit.Ignore
import idb.syntax.iql._
import UniversityDatabase._
import idb.query.colors.Color
import org.hamcrest.CoreMatchers._


/**
 * @author Mirko Köhler
 */
class TestRemote extends UniversityTestData {


	@Test
	def testRemote(): Unit = {

		val registrationHost = NamedHost("RegistrationServer")
		val studentHost = NamedHost("StudentServer")

		//Initialize query context as implicit value
		implicit val queryEnvironment = QueryEnvironment.create(
			actorSystem = ActorSystem("test1"),
			permissions = Map(
				LocalHost -> Set("registration", "students"),
				registrationHost -> Set("registration"),
				studentHost -> Set("students")
			)

		)

		//Initialize remote tables
		import idb.syntax.iql.IR._

		val registrationTable = BagTable.empty[Registration]
		val remoteRegistrations = table(
			table = registrationTable,
			host = registrationHost,
			color = Color("registration")
		)

		val studentTable = BagTable.empty[Student]
		val remoteStudents = table(
			table = studentTable,
			host = studentHost,
			color = Color("students")
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
		registrationTable.add(johnTakesEise)

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

		val registrationHost = NamedHost("RegistrationServer")
		val studentHost = NamedHost("StudentServer")

		//Initialize query context as implicit value
		implicit val queryEnvironment = QueryEnvironment.create(
			actorSystem = ActorSystem("test2"),
			permissions = Map(
				LocalHost -> Set("registration", "students"),
				registrationHost -> Set("registration"),
				studentHost -> Set("students")
			)

		)

		//Initialize remote tables
		import idb.syntax.iql.IR._

		val registrationTable = BagTable.empty[Registration]
		val remoteRegistrations = table(
			table = registrationTable,
			host = registrationHost,
			color = Color("registration")
		)

		val studentTable = BagTable.empty[Student]
		val remoteStudents = table(
			table = studentTable,
			host = studentHost,
			color = Color("students")
		)


		val q =
			plan(
				SELECT (*) FROM (remoteStudents, remoteStudents, remoteRegistrations, remoteStudents)
			)


		val compiledQ = compile(q).asMaterialized

		studentTable.add(johnDoe)
		registrationTable.add(johnTakesEise)

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
		val registrationHost = NamedHost("RegistrationServer")
		val studentHost = NamedHost("StudentServer")

		//Initialize query context as implicit value
		implicit val queryEnvironment = QueryEnvironment.create(
			actorSystem = ActorSystem("test1"),
			permissions = Map(
				LocalHost -> Set("registration", "students"),
				registrationHost -> Set("registration"),
				studentHost -> Set("students")
			)

		)

		//Initialize remote tables
		import idb.syntax.iql.IR._

		val registrationTable = BagTable.empty[Registration]
		val remoteRegistrations = table(
			table = registrationTable,
			host = registrationHost,
			color = Color("registration")
		)

		val studentTable = BagTable.empty[Student]
		val remoteStudents = table(
			table = studentTable,
			host = studentHost,
			color = Color("students")
		)

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

		studentTable.add(sallyFields)
		registrationTable.add(sallyTakesIcs1)
		studentTable.add(jackBlack)
		registrationTable.add(jackTakesIcs1)
		registrationTable.add(sallyTakesIcs2)
		registrationTable.add(jackTakesIcs2)

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
		val studentHost = NamedHost("StudentServer")

		//Initialize query context as implicit value
		implicit val queryEnvironment = QueryEnvironment.create(
			actorSystem = ActorSystem("test1"),
			permissions = Map(
				LocalHost -> Set("students"),
				studentHost -> Set("students")
			)
		)

		//Initialize remote tables
		import idb.syntax.iql.IR._

		val studentTable = BagTable.empty[Student]
		val remoteStudents = table(
			table = studentTable,
			host = studentHost,
			color = Color("students")
		)

		val q =
			plan(
				SELECT (COUNT(*))
				FROM remoteStudents
				WHERE (s1 =>
					s1.lastName == "Doe"
				)

		)

		val compiledQ = compile(q).asMaterialized

		studentTable += johnDoe
		studentTable += sallyFields

		//Wait for actors
		Thread.sleep(500)

		//Test output
		assertThat (compiledQ.size, is (1))
		assertThat (compiledQ.contains(1), is (true))


		//Close context
		queryEnvironment.close()
	}


//	@Ignore
//	@Test
/*	def testRemoteAirports(): Unit = {
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
	}   */

}
