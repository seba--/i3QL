package sae.example.hospital

import idb.Table
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


		val materializedQ2 = q2.asMaterialized

		personDatabase += johnDoe
		personDatabase += sallyFields
		personDatabase += johnCarter

		knowledgeDatabase += lungCancer1
		knowledgeDatabase += lungCancer2
		knowledgeDatabase += commonCold1
		knowledgeDatabase += panicDisorder1

		patientDatabase += patientJohnDoe2
		patientDatabase += patientSallyFields1
		patientDatabase += patientJohnCarter1

		Predef.println("Query results: ++++++++++++++++++++++++++++++")
		materializedQ2.foreach(Predef.println)
		Predef.println("+++++++++++++++++++++++++++++++++++++++++++++")

		Predef.println(q2.prettyprint(""))
		Predef.println("Hello!")

	}

	def distributedExample(): Unit = {
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
						(distributedPersonDatabase, UNNEST (distributedPatientDatabase, (x : Rep[Patient]) => x.symptoms), distributedKnowledgeDatabase)
						WHERE
						((person : Rep[Person], patientSymptom : Rep[(Patient, String)], knowledgeData : Rep[KnowledgeData]) =>
							person.personId == patientSymptom._1.personId AND
								patientSymptom._2 == knowledgeData.symptom)
				)
			)


		val materializedQ2 = q2.asMaterialized

		distributedPersonDatabase += johnDoe
		distributedPersonDatabase += sallyFields
		distributedPersonDatabase += johnCarter

		distributedKnowledgeDatabase += lungCancer1
		distributedKnowledgeDatabase += lungCancer2
		distributedKnowledgeDatabase += commonCold1
		distributedKnowledgeDatabase += panicDisorder1

		distributedPatientDatabase += patientJohnDoe2
		distributedPatientDatabase += patientSallyFields1
		distributedPatientDatabase += patientJohnCarter1

		Predef.println("Query results: ++++++++++++++++++++++++++++++")
		materializedQ2.foreach(Predef.println)
		Predef.println("+++++++++++++++++++++++++++++++++++++++++++++")

		Predef.println(q2.prettyprint(""))
		Predef.println("Hello!")
	}


	def main(args : Array[String]): Unit = {

		distributedExample()

		System.exit(0)
	}


}
