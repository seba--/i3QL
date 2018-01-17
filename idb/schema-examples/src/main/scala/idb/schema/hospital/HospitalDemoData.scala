package idb.schema.hospital

/**
 * @author Mirko Köhler
 */
trait HospitalDemoData {

	object Diagnosis {
		val commonCold = "common cold"
		val panicDisorder = "panic disorder"
		val allergy = "allergy"
	}

	object Symptoms {
		val cough = "cough"
		val chestPain = "chest pain"
		val sweating = "sweating"
	}


	val johnDoe = Person(0, "John Doe", 1973)
	val sallyFields = Person(1, "Sally Fields", 1980)
	val johnCarter = Person(2, "John Carter", 1958)
	val janeDoe = Person(3, "Jane Doe", 2003)


	val patientJohnDoe2 = Patient(0, 4, 2011, Seq(Symptoms.cough, Symptoms.chestPain))
	val patientSallyFields1 = Patient(1, 5, 1999, Seq(Symptoms.chestPain))
	val patientJohnCarter1 = Patient(2, 6, 2005, Seq(Symptoms.sweating))
	val patientJaneDoe1 = Patient(3, 0, 2014, Seq(Symptoms.cough, Symptoms.sweating))
	val patientJaneDoe2 = Patient(3, 2, 2003, Seq(Symptoms.cough))


	val allergy1 = KnowledgeData(Symptoms.cough, Diagnosis.allergy, 1927)
	val allergy2 = KnowledgeData(Symptoms.chestPain, Diagnosis.allergy, 1929)
	val commonCold1 = KnowledgeData(Symptoms.cough, Diagnosis.commonCold, 1893)
	val panicDisorder1 = KnowledgeData(Symptoms.sweating, Diagnosis.panicDisorder, 1964)

}
