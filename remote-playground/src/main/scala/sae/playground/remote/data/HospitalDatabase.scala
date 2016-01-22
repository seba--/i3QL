package sae.playground.remote.data

import idb.{Table, SetTable}
import idb.annotations.Remote

/**
 * @author Mirko Köhler
 */
object HospitalDatabase extends HospitalSchema {

	val IR = idb.syntax.iql.IR

	val personDatabase = SetTable.empty[Person]
	val patientDatabase = SetTable.empty[Patient]
	val knowledgeDatabase = SetTable.empty[KnowledgeData]


	@Remote(host = "PersonDBServer", description = "hospital")
	class PersonDatabase extends SetTable[Person]

	val distributedPersonDatabase : Table[Person] = new PersonDatabase


	@Remote(host = "PatientDBServer", description = "hospital")
	class PatientDatabase extends SetTable[Patient]

	val distributedPatientDatabase : Table[Patient] = new PatientDatabase


	@Remote(host = "KnowledgeDBServer", description = "research")
	class KnowledgeDatabase extends SetTable[KnowledgeData]

	val distributedKnowledgeDatabase : Table[KnowledgeData] = new KnowledgeDatabase


}
