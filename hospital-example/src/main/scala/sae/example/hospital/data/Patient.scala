package sae.example.hospital.data

import scala.virtualization.lms.common.StructExp
import scala.language.implicitConversions


/**
 * @author Mirko Köhler
 */
case class Patient (personId : Int, treatmentId : Int, yearOfTreatment : Int, symptoms : Seq[String])



trait PatientSchema
{
	val IR: StructExp

	import IR._

	def Patient (personId: Rep[Int], treatmentId : Rep[Int], yearOfTreatment: Rep[Int], symptoms : Rep[Seq[String]]) =
		struct[Patient](
			ClassTag[Patient]("Patient"),
			Seq ("personId" -> personId, "treatmentId" -> treatmentId, "yearOfTreatment" -> yearOfTreatment, "symptoms" -> symptoms)
		)

	case class PatientInfixOps (x: Rep[Patient])
	{
		def personId: Rep[Int] = field(x, "personId")

		def treatmentId : Rep[Int] = field(x, "treatmentId")

		def yearOfTreatment: Rep[Int] = field(x, "yearOfTreatment")

		def symptoms: Rep[Seq[String]] = field(x, "symptoms")
	}

	implicit def patientToInfixOps (x: Rep[Patient]) : PatientInfixOps = PatientInfixOps (x)
}
