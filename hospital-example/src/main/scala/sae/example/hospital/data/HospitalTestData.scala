package sae.example.hospital.data

/**
 * @author Mirko Köhler
 */
trait HospitalTestData {

	val johnDoe = Person(0, "John Doe", 1973)
	val sallyFields = Person(1, "Sally Fields", 1980)
	val johnCarter = Person(2, "John Carter", 1958)
	val janeDoe = Person(3, "Jane Doe", 2003)

	val patientJohnDoe1 = Patient(0, 2007, Seq("cough"))
	val patientJohnDoe2 = Patient(0, 2011, Seq("cough", "chest pain"))
	val patientSallyFields1 = Patient(1, 1999, Seq("chest pain"))
	val patientJohnCarter1 = Patient(2, 2005, Seq("sweating"))
	val patientJaneDoe1 = Patient(3, 2014, Seq("cough", "sweating"))

	val lungCancer1 = KnowledgeData("cough", "lung cancer", 1800)
	val lungCancer2 = KnowledgeData("chest pain", "lung cancer", 1800)
	val commonCold1 = KnowledgeData("cough", "common cold", 1800)
	val coronaryArteryDisease1 = KnowledgeData("chest pain" , "coronary artery disease", 1800)
	val panicDisorder1 = KnowledgeData("sweating", "panic disorder", 1800)

}
