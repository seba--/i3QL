package sae.example.hospital.data

/**
 * @author Mirko Köhler
 */
trait HospitalTestData {

	object Treatments {
		val none = "None"
		val coughMedicine = "cough medicine"
		val chemoTherapy = "chemo therapy"
		val allergyMedicine = "allergy medicine"
		val inhaler = "inhaler"
		val analgesic = "analgesic"
		val operation = "operation"
		val ataractics = "ataractics"
	}

	object Diagnosis {
		val none = "None"
		val lungCancer = "lung cancer"
		val commonCold = "common cold"
		val coronaryArteryDisease = "coronary artery disease"
		val panicDisorder = "panic disorder"
		val allergy = "allergy"
	}

	object Symptoms {
		val cough = "cough"
		val chestPain = "chest pain"
		val sweating = "sweating"
		val breathingProblems = "breathing problems"
	}

	object Insurance {
		val aok = 0
		val barmer = 1
	}

	val johnDoe = Person(0, "John Doe", 1973)
	val sallyFields = Person(1, "Sally Fields", 1980)
	val johnCarter = Person(2, "John Carter", 1958)
	val janeDoe = Person(3, "Jane Doe", 2003)
	val bobRoss = Person(4, "Bob Ross", 1967)

	val treatmentNone = TreatmentData(0, Diagnosis.none, Treatments.none)
	val treatmentCoughMedicine = TreatmentData(1, Diagnosis.commonCold, Treatments.coughMedicine)
	val treatmentAllergyMedicine = TreatmentData(2, Diagnosis.allergy, Treatments.allergyMedicine)
	val treatmentAllergyInhaler = TreatmentData(3, Diagnosis.allergy, Treatments.inhaler)
	val treatmentLungCancerChemo = TreatmentData(4, Diagnosis.lungCancer, Treatments.chemoTherapy)
	val treatmentArteryOperation = TreatmentData(5, Diagnosis.coronaryArteryDisease, Treatments.operation)
	val treatmentPanicAtaractics = TreatmentData(6, Diagnosis.panicDisorder, Treatments.ataractics)

	val patientJohnDoe1 = Patient(0, 1, 2007, Seq(Symptoms.cough))
	val patientJohnDoe2 = Patient(0, 4, 2011, Seq(Symptoms.cough, Symptoms.chestPain))
	val patientSallyFields1 = Patient(1, 5, 1999, Seq(Symptoms.chestPain))
	val patientJohnCarter1 = Patient(2, 6, 2005, Seq(Symptoms.sweating))
	val patientJaneDoe1 = Patient(3, 0, 2014, Seq(Symptoms.cough, Symptoms.sweating))
	val patientJaneDoe2 = Patient(3, 2, 2003, Seq(Symptoms.cough))
	val patientBobRoss1 = Patient(4, 3, 1990, Seq(Symptoms.chestPain))

	val financeJohnDoe = FinancialData(0, 1340.35, Insurance.aok)
	val financeSallyFields = FinancialData(1, 230.00, Insurance.aok)
	val financeJohnCarter = FinancialData(2, 10, Insurance.barmer)
	val financeJaneDoe = FinancialData(3, 270, Insurance.aok)
	val financeBobRoss = FinancialData(4, 400.70, Insurance.barmer)

	val lungCancer1 = KnowledgeData(Symptoms.cough, Diagnosis.lungCancer, 1800)
	val lungCancer2 = KnowledgeData(Symptoms.chestPain, Diagnosis.lungCancer, 1800)
	val commonCold1 = KnowledgeData(Symptoms.cough, Diagnosis.commonCold, 1800)
	val coronaryArteryDisease1 = KnowledgeData(Symptoms.chestPain , Diagnosis.coronaryArteryDisease, 1800)
	val panicDisorder1 = KnowledgeData(Symptoms.sweating, Diagnosis.panicDisorder, 1800)



}
