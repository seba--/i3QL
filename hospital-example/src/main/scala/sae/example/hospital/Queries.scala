package sae.example.hospital

import akka.actor.ActorSystem
import akka.actor.FSM.->
import idb.algebra.print.RelationalAlgebraPrintPlan
import idb.observer.Observer
import idb.query.colors.{Color, StringColor}
import idb.query.{LocalHost, QueryEnvironment, RemoteHost}
import idb.remote.RemoteView
import idb.syntax.iql.DISTINCT
import sae.example.hospital.data.Patient
import sae.example.hospital.data.Person
import sae.example.hospital.data._


/**
 * @author Mirko Köhler
 */
object Queries extends HospitalTestData {

	import idb.syntax.iql._
	import idb.syntax.iql.IR._

	def normalExample(): Unit = {
		import Hospital.LocalSetup._

		val q : Rep[Query[(Int, String, String)]] =
			ROOT (
				SELECT DISTINCT
					((person: Rep[Person], patientSymptom: Rep[(Patient, String)], knowledgeData: Rep[KnowledgeData]) => (person.personId, person.name, knowledgeData.diagnosis))
					FROM
					(person, UNNEST(patient, (x: Rep[Patient]) => x.symptoms), knowledge)
					WHERE
					((person: Rep[Person], patientSymptom: Rep[(Patient, String)], knowledgeData: Rep[KnowledgeData]) =>
						person.personId == patientSymptom._1.personId AND
							patientSymptom._2 == knowledgeData.symptom)
			)

		executeExample(q)
	}

	/*def distributedExample(): Unit = {
		import idb.syntax.iql._
		import idb.syntax.iql.IR._

		implicit val queryContext = QueryEnvironment.create(
			actorSystem = ActorSystem("example")
		)

		try {
			val q2 = // : Relation[(Int, String, String)] =
				compile(
					SELECT DISTINCT
						((person: Rep[Person], patientSymptom: Rep[(Patient, String)], knowledgeData: Rep[KnowledgeData]) => (person.personId, person.name, knowledgeData.diagnosis))
					FROM
						(Hospital.DistributedSetup1.person, UNNEST(Hospital.DistributedSetup1.patient, (x: Rep[Patient]) => x.symptoms), Hospital.DistributedSetup1.knowledge)
					WHERE
						((person: Rep[Person], patientSymptom: Rep[(Patient, String)], knowledgeData: Rep[KnowledgeData]) =>
							person.personId == patientSymptom._1.personId AND
								patientSymptom._2 == knowledgeData.symptom)
				)

			executeExample(q2, Hospital.person, Hospital.patient, Hospital.knowledge)
		} finally {
			queryContext.close()
		}
	}

	def distributedExample2(): Unit = {
		import idb.syntax.iql._
		import idb.syntax.iql.IR._

		implicit val queryContext = QueryEnvironment.create(
			actorSystem = ActorSystem("example")
		)


		val q2 = //: Relation[(Int, String, String)] =
			compile(
				SELECT DISTINCT
					((person : Rep[Person], knowledgeData : Rep[KnowledgeData], patientSymptom : Rep[(Patient, String)]) => (person.personId, person.name, knowledgeData.diagnosis))
				FROM
					(Hospital.DistributedSetup1.person, Hospital.DistributedSetup1.knowledge, UNNEST(Hospital.DistributedSetup1.patient, (x: Rep[Patient]) => x.symptoms))
				WHERE
					((person : Rep[Person], knowledgeData : Rep[KnowledgeData], patientSymptom : Rep[(Patient, String)]) =>
						person.personId == patientSymptom._1.personId AND
							patientSymptom._2 == knowledgeData.symptom)
			)



		executeExample(q2, Hospital.person, Hospital.patient, Hospital.knowledge)

		queryContext.close()
	}

	def distributedExample3(): Unit = {
		import idb.syntax.iql._
		import idb.syntax.iql.IR._

		implicit val queryContext = QueryEnvironment.create(
			actorSystem = ActorSystem("example")
		)

		try {
			val q2 = // : Relation[(Int, String, String)] =
				compile(
					SELECT
						((person : Rep[Person], patient : Rep[Patient], knowledgeData : Rep[KnowledgeData]) => (person.personId, person.name, knowledgeData.diagnosis))
					FROM
						(Hospital.DistributedSetup1.person, Hospital.DistributedSetup1.patient, Hospital.DistributedSetup1.knowledge)
					WHERE
						((person : Rep[Person], patient : Rep[Patient], knowledgeData : Rep[KnowledgeData]) =>
							person.personId == patient.personId AND
								patient.symptoms.contains(knowledgeData.symptom))
				)



			executeExample(q2, Hospital.person, Hospital.patient, Hospital.knowledge)

		} finally {
			queryContext.close()
		}
	}          */

	def distributedExample1(): Unit = {
		import Hospital.DistributedSetup1._

		try {

			val q : Rep[Query[(Int, String, String)]] = // : Relation[(Int, String, String)] =
				ROOT (
					SELECT DISTINCT
						((person : Rep[Person], patientSymptom : Rep[(Patient, String)], knowledgeData : Rep[KnowledgeData]) => (person.personId, person.name, knowledgeData.diagnosis))
					FROM
						(person, UNNEST(patient, (x: Rep[Patient]) => x.symptoms), knowledge)
					WHERE
						((person : Rep[Person], patientSymptom : Rep[(Patient, String)], knowledgeData : Rep[KnowledgeData]) =>
							person.personId == patientSymptom._1.personId AND
								patientSymptom._2 == knowledgeData.symptom)
				)

			executeExample(q)
		} finally {
			queryEnv.close()
		}
	}

	def distributedExample2(): Unit = {

		import Hospital.DistributedSetup1._

		try {
			import idb.syntax.iql._
			import idb.syntax.iql.IR._

			val q : Rep[Query[(Int, String, String)]] = // : Relation[(Int, String, String)] =
				ROOT (
					SELECT DISTINCT
						((person : Rep[Person], knowledgeData : Rep[KnowledgeData], patientSymptom : Rep[(Patient, String)]) => (person.personId, person.name, knowledgeData.diagnosis))
					FROM
						(person, knowledge, UNNEST(patient, (x: Rep[Patient]) => x.symptoms))
					WHERE
						((person : Rep[Person], knowledgeData : Rep[KnowledgeData], patientSymptom : Rep[(Patient, String)]) =>
							person.personId == patientSymptom._1.personId AND
								patientSymptom._2 == knowledgeData.symptom)
				)

			executeExample(q)
		} finally {
			queryEnv.close()
		}
	}

	def distributedExample3(): Unit = {

		import Hospital.DistributedSetup2._

		try {
			import idb.syntax.iql._
			import idb.syntax.iql.IR._

			val q : Rep[Query[(Int, String, String)]] = // : Relation[(Int, String, String)] =
				ROOT (
					RECLASS (
						SELECT DISTINCT
							((person : Rep[Person], knowledgeData : Rep[KnowledgeData], patientSymptom : Rep[(Patient, String)]) => (person.personId, person.name, knowledgeData.diagnosis))
							FROM
							(person, knowledge, UNNEST(patient, (x: Rep[Patient]) => x.symptoms))
							WHERE
							((person : Rep[Person], knowledgeData : Rep[KnowledgeData], patientSymptom : Rep[(Patient, String)]) =>
								person.personId == patientSymptom._1.personId AND
									patientSymptom._2 == knowledgeData.symptom),
						Color("research")
					)

				)

			executeExample(q)
		} finally {
			queryEnv.close()
		}
	}

	def distributedExample4(): Unit = {

		import Hospital.DistributedSetup2._

		try {
			import idb.syntax.iql._
			import idb.syntax.iql.IR._

			val q : Rep[Query[(Int, String, String)]] = // : Relation[(Int, String, String)] =
				ROOT (
					SELECT DISTINCT
						((person : Rep[Person], knowledgeData : Rep[KnowledgeData], patientSymptom : Rep[(Patient, String)]) => (person.personId, person.name, knowledgeData.diagnosis))
						FROM
						(person, knowledge, UNNEST(patient, (x: Rep[Patient]) => x.symptoms))
						WHERE
						((person : Rep[Person], knowledgeData : Rep[KnowledgeData], patientSymptom : Rep[(Patient, String)]) =>
							person.personId == patientSymptom._1.personId AND
								patientSymptom._2 == knowledgeData.symptom)
				)



			executeExample(q)
		} finally {
			queryEnv.close()
		}
	}


	//import idb.{Table, Relation}
	private def executeExample[A : Manifest](resultRelation : Rep[Query[A]])(implicit env : QueryEnvironment): Unit = {
		executeExample(resultRelation, Hospital.LocalSetup.person, Hospital.LocalSetup.patient, Hospital.LocalSetup.knowledge)
	}

	private def executeExample[A : Manifest](resultRelation : Rep[Query[A]], _personDatabase : Table[Person], _patientDatabase : Table[Patient], _knowledgeDatabase : Table[KnowledgeData])(implicit env : QueryEnvironment): Unit = {
		//Print query tree
		val printer = new RelationalAlgebraPrintPlan() {
			override val IR = idb.syntax.iql.IR
		}
		Predef.println(printer.quoteRelation(resultRelation))

		//Compile relation
		val r : Relation[A] = compile(resultRelation)

		//Print compiled relation
		Predef.println(r.prettyprint(""))


		val qMat = r.asMaterialized

		Predef.println("---> Add data to tables")
		_personDatabase += johnDoe
		_personDatabase += sallyFields
		_personDatabase += johnCarter

		_knowledgeDatabase += lungCancer1
		_knowledgeDatabase += lungCancer2
		_knowledgeDatabase += commonCold1
		_knowledgeDatabase += panicDisorder1

		_patientDatabase += patientJohnDoe2
		_patientDatabase += patientSallyFields1
		_patientDatabase += patientJohnCarter1
		Predef.println("<---")

		//It takes some time to push the data through the actor system. We need to wait some time to print the results.
		Predef.println("---> Wait...")
		Thread.sleep(1000)
		Predef.println("<---")

		Predef.println("---> Finished.")
		qMat foreach Predef.println
		Predef.println("<---")


	}


	def main(args : Array[String]): Unit = {

		val example = 3

		example match {
			case 0 => normalExample()
			case 1 => distributedExample1()
			case 2 => distributedExample2()
			case 3 => distributedExample3()
			case 4 => distributedExample4()
		}
	}


}
