package sae.example.hospital

import akka.actor.ActorSystem
import idb.observer.Observer
import idb.query.QueryEnvironment
import idb.remote.RemoteView
import sae.example.hospital.data.HospitalDatabase._
import sae.example.hospital.data.Patient
import sae.example.hospital.data.Person
import sae.example.hospital.data._

/**
 * @author Mirko Köhler
 */
object Queries extends HospitalTestData {

	def normalExample(): Unit = {
		import idb.syntax.iql._
		import idb.syntax.iql.IR._

		implicit val queryContext = QueryEnvironment.Local

		val q2 = //: Relation[(Int, String, String)] =
			compile(
				SELECT DISTINCT
					((person : Rep[Person], patientSymptom : Rep[(Patient, String)], knowledgeData : Rep[KnowledgeData]) => (person.personId, person.name, knowledgeData.diagnosis))
				FROM
					(personDatabase, UNNEST (patientDatabase, (x : Rep[Patient]) => x.symptoms), knowledgeDatabase)
				WHERE
					((person : Rep[Person], patientSymptom : Rep[(Patient, String)], knowledgeData : Rep[KnowledgeData]) =>
						person.personId == patientSymptom._1.personId AND
							patientSymptom._2 == knowledgeData.symptom)
			)

		executeExample(q2, personDatabase, patientDatabase, knowledgeDatabase)



		/*	val q1 = //: Relation[(Int, String, String)] =
				compile(
					root(
						SELECT
							((person : Rep[Person], patient : Rep[Patient], knowledgeData : Rep[KnowledgeData]) => (person.personId, person.name, knowledgeData.diagnosis))
						FROM
							(personDatabase, patientDatabase, knowledgeDatabase)
						WHERE
							((person : Rep[Person], patient : Rep[Patient], knowledgeData : Rep[KnowledgeData]) =>
								person.personId == patient.personId AND
								patient.symptoms.contains(knowledgeData.symptom))
					)
				)   */

	}

	def distributedExample(): Unit = {
		import idb.syntax.iql._
		import idb.syntax.iql.IR._

		implicit val queryContext = QueryEnvironment.create(
			actorSystem = ActorSystem("example")
		)


		val q2 = // : Relation[(Int, String, String)] =
			compile (
				SELECT DISTINCT
					((person : Rep[Person], patientSymptom : Rep[(Patient, String)], knowledgeData : Rep[KnowledgeData]) => (person.personId, person.name, knowledgeData.diagnosis))
				FROM
					(distributedPersonDatabase, UNNEST (distributedPatientDatabase, (x : Rep[Patient]) => x.symptoms), distributedKnowledgeDatabase)
				WHERE
					((person : Rep[Person], patientSymptom : Rep[(Patient, String)], knowledgeData : Rep[KnowledgeData]) =>
						person.personId == patientSymptom._1.personId AND
							patientSymptom._2 == knowledgeData.symptom)
			)

		executeExample(q2, distributedPersonDatabase, distributedPatientDatabase, distributedKnowledgeDatabase)

		queryContext.close()
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
					(distributedPersonDatabase, distributedKnowledgeDatabase, UNNEST (distributedPatientDatabase, (x : Rep[Patient]) => x.symptoms))
				WHERE
					((person : Rep[Person], knowledgeData : Rep[KnowledgeData], patientSymptom : Rep[(Patient, String)]) =>
						person.personId == patientSymptom._1.personId AND
							patientSymptom._2 == knowledgeData.symptom)
			)



		executeExample(q2, distributedPersonDatabase, distributedPatientDatabase, distributedKnowledgeDatabase)

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
						(distributedPersonDatabase, distributedPatientDatabase, distributedKnowledgeDatabase)
					WHERE
						((person : Rep[Person], patient : Rep[Patient], knowledgeData : Rep[KnowledgeData]) =>
							person.personId == patient.personId AND
								patient.symptoms.contains(knowledgeData.symptom))
				)



			executeExample(q2, distributedPersonDatabase, distributedPatientDatabase, distributedKnowledgeDatabase)

		} finally {
			queryContext.close()
		}
	}


	import idb.{Table, Relation}

	private def executeExample(resultRelation : Relation[_], _personDatabase : Table[Person], _patientDatabase : Table[Patient], _knowledgeDatabase : Table[KnowledgeData]): Unit = {
		Predef.println(resultRelation.prettyprint(""))

		val qMat = resultRelation.asMaterialized

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

		distributedExample()

		System.exit(0)
	}


}
