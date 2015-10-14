package sae.example.hospital

import idb.observer.Observer
import idb.{Relation, Table}
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

		val q2 = //: Relation[(Int, String, String)] =
			compile(
				root(
					SELECT DISTINCT
						((person : Rep[Person], patientSymptom : Rep[(Patient, String)], knowledgeData : Rep[KnowledgeData]) => (person.personId, person.name, knowledgeData.diagnosis))
					FROM
						(personDatabase, UNNEST (patientDatabase, (x : Rep[Patient]) => x.symptoms), knowledgeDatabase)
					WHERE
						((person : Rep[Person], patientSymptom : Rep[(Patient, String)], knowledgeData : Rep[KnowledgeData]) =>
							person.personId == patientSymptom._1.personId AND
								patientSymptom._2 == knowledgeData.symptom)
				)
			)


		executeExample(q2, personDatabase, patientDatabase, knowledgeDatabase)

	}

	def distributedExample(): Unit = {
		import idb.syntax.iql._
		import idb.syntax.iql.IR._

		try {
			val q2 = //: Relation[(Int, String, String)] =
				compile(
					root(
						SELECT DISTINCT
							((person : Rep[Person], patientSymptom : Rep[(Patient, String)], knowledgeData : Rep[KnowledgeData]) => (person.personId, person.name, knowledgeData.diagnosis))
						FROM
							(distributedPersonDatabase, UNNEST (distributedPatientDatabase, (x : Rep[Patient]) => x.symptoms), distributedKnowledgeDatabase)
						WHERE
							((person : Rep[Person], patientSymptom : Rep[(Patient, String)], knowledgeData : Rep[KnowledgeData]) =>
								person.personId == patientSymptom._1.personId AND
									patientSymptom._2 == knowledgeData.symptom)
					)
				)

			executeExample(q2, distributedPersonDatabase, distributedPatientDatabase, distributedKnowledgeDatabase)

		} finally {
			RemoteView.system.shutdown()

		}
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

	def distributedExample2(): Unit = {
		import idb.syntax.iql._
		import idb.syntax.iql.IR._

		try {
			val q2 = //: Relation[(Int, String, String)] =
				compile(
					root(
						SELECT DISTINCT
							((person : Rep[Person], knowledgeData : Rep[KnowledgeData], patientSymptom : Rep[(Patient, String)]) => (person.personId, person.name, knowledgeData.diagnosis))
						FROM
							(distributedPersonDatabase, distributedKnowledgeDatabase, UNNEST (distributedPatientDatabase, (x : Rep[Patient]) => x.symptoms))
						WHERE
							((person : Rep[Person], knowledgeData : Rep[KnowledgeData], patientSymptom : Rep[(Patient, String)]) =>
								person.personId == patientSymptom._1.personId AND
									patientSymptom._2 == knowledgeData.symptom)
					)
				)

			executeExample(q2, distributedPersonDatabase, distributedPatientDatabase, distributedKnowledgeDatabase)

		} finally {
			RemoteView.system.shutdown()

		}
	}

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

		//It takes some time to push the data through the actor system. We need to wait some time to print the results.
		Predef.println("---> Wait...")
		Thread.sleep(5000)

		Predef.println("---> Finished.")
		qMat foreach Predef.println
		Predef.println("<---")


	}


	def main(args : Array[String]): Unit = {

		distributedExample2()

		System.exit(0)
	}


}
