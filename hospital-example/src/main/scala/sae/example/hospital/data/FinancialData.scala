package sae.example.hospital.data

import scala.virtualization.lms.common.StructExp
import scala.language.implicitConversions


/**
 * @author Mirko Köhler
 */
case class FinancialData (personId : Int, payment : Double, insurance : Int)



trait FinancialDataSchema
{
	val IR: StructExp

	import IR._

	def FinancialData (personId : Rep[Int], payment : Rep[Double], insurance : Rep[Int]) =
		struct[FinancialData](
			ClassTag[FinancialData]("FinancialData"),
			Seq ("personId" -> personId, "payment" -> payment, "insurance" -> insurance)
		)

	case class FinancialDataInfixOps (x: Rep[FinancialData])
	{
		def personId: Rep[Int] = field(x, "personId")

		def payment: Rep[Double] = field(x, "payment")

		def insurance: Rep[Int] = field(x, "insurance")
	}

	implicit def financialDataToInfixOps (x: Rep[FinancialData]) : FinancialDataInfixOps = FinancialDataInfixOps (x)
}