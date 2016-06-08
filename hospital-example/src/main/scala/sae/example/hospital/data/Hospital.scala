package sae.example.hospital.data

import akka.actor.ActorSystem
import idb.{SetTable, Table}
import idb.query.{LocalHost, QueryEnvironment, RemoteHost}
import idb.query.colors.Color

/**
 * @author Mirko Köhler
 */
object Hospital {

	protected trait BaseHospital extends HospitalSchema {
		override val IR = idb.syntax.iql.IR
	}

	object LocalSetup extends BaseHospital {

		implicit val queryEnv = QueryEnvironment.Local

		val person = SetTable.empty[Person]
		val patient = SetTable.empty[Patient]
		val knowledge = SetTable.empty[KnowledgeData]
		val finance = SetTable.empty[FinancialData]
		val treatment = SetTable.empty[TreatmentData]
	}

	object DistributedSetup1 extends BaseHospital {
		val patientHost = RemoteHost("PatientDBServer")
		val personHost =  RemoteHost("PersonDBServer")
		val knowledgeHost = RemoteHost("KnowledgeDBServer")

		implicit val queryEnv = QueryEnvironment.create(
			actorSystem = ActorSystem("example"),
			permissions = Map (
				LocalHost -> Set("hospital", "research"),
				patientHost -> Set("hospital"),
				personHost -> Set("hospital", "research"),
				knowledgeHost -> Set("research")
			)
		)

		val person =
			IR.table(Hospital.LocalSetup.person, color = Color("hospital"), host = personHost)

		val patient =
			IR.table(Hospital.LocalSetup.patient, color = Color.group("hospital", "research"), host = patientHost)

		val knowledge =
			IR.table(Hospital.LocalSetup.knowledge, color = Color.group("research"), host = knowledgeHost)

	}



}
