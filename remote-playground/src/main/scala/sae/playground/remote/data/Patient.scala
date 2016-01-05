package sae.playground.remote.data

import scala.virtualization.lms.common.StructExp
import scala.language.implicitConversions


/**
 * @author Mirko Köhler
 */
case class Patient (personId : Int, yearOfTreatment : Int, symptoms : Seq[String])



trait PatientSchema
{
	val IR: StructExp

	import IR._

	def Patient (personId: Rep[Int], yearOfTreatment: Rep[Int], symptoms : Rep[Seq[String]]) =
		struct[Patient](
			ClassTag[Patient]("Patient"),
			Seq ("personId" -> personId, "yearOfTreatment" -> yearOfTreatment, "symptoms" -> symptoms)
		)

	case class PatientInfixOps (x: Rep[Patient])
	{
		def personId: Rep[Int] = field[Int](x, "personId")

		def yearOfTreatment: Rep[Int] = field[Int](x, "yearOfTreatment")

		def symptoms: Rep[Seq[String]] = field[Seq[String]](x, "symptoms")


	}

	implicit def patientToInfixOps (x: Rep[Patient]) : PatientInfixOps = PatientInfixOps (x)
}
