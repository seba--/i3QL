package idb.schema.tpch

import scala.virtualization.lms.common.StructExp
import scala.language.implicitConversions


/**
  * TODO: Add documentation of class!
  *
  * @author mirko
  */
case class PartSupp(
	partKey : Int,
	suppKey : Int,
	availQty : Int,
	supplyCost : Double,
	comment : String
)

trait PartSuppSchema {
	val IR: StructExp
	import IR._

	def PartSupp (
		partKey : Rep[Int],
		suppKey : Rep[Int],
		availQty : Rep[Int],
		supplyCost : Rep[Double],
		comment : Rep[String]
	) : Rep[PartSupp] =
		struct[PartSupp](
			ClassTag[PartSupp]("PartSupp"),
			Seq ("partKey" -> partKey, "suppKey" -> suppKey, "availQty" -> availQty, "supplyCost" -> supplyCost,
				"comment" -> comment)
		)

	case class PartSuppInfixOps (x: Rep[PartSupp]) {
		def partKey : Rep[Int] = field[Int](x, "partKey")
		def suppKey : Rep[Int] = field[Int](x, "suppKey")
		def availQty : Rep[Int] = field[Int](x, "availQty")
		def supplyCost : Rep[Double] = field[Double](x, "supplyCost")
		def comment : Rep[String] = field[String](x, "comment")
	}

	implicit def partSuppToInfixOps (x: Rep[PartSupp]) : PartSuppInfixOps = PartSuppInfixOps (x)
}
