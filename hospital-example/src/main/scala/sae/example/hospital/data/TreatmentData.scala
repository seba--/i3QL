package sae.example.hospital.data

import scala.virtualization.lms.common.StructExp
import scala.language.implicitConversions


/**
 * @author Mirko Köhler
 */
case class TreatmentData (treatmentId : Int, diagnosis : String, treatment : String)



trait TreatmentDataSchema
{
	val IR: StructExp

	import IR._

	def TreatmentData (treatmentId : Rep[Int], diagnosis : Rep[String], treatment : Rep[String]) =
		struct[TreatmentData](
			ClassTag[TreatmentData]("TreatmentData"),
			Seq ("treatmentId" -> treatmentId, "diagnosis" -> diagnosis, "treatment" -> treatment)
		)

	case class TreatmentDataInfixOps (x: Rep[TreatmentData])
	{
		def treatmentId: Rep[Int] = field(x, "treatmentId")

		def diagnosis: Rep[String] = field(x, "diagnosis")

		def treatment: Rep[Int] = field(x, "treatment")
	}

	implicit def treatmentDataToInfixOps (x: Rep[TreatmentData]) : TreatmentDataInfixOps = TreatmentDataInfixOps (x)
}