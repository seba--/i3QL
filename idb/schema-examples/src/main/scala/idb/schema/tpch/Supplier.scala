package idb.schema.tpch

import scala.virtualization.lms.common.StructExp
import scala.language.implicitConversions


/**
  * TODO: Add documentation of class!
  *
  * @author mirko
  */
case class Supplier(
	suppKey : Int,
	name : String,
	address : String,
	nationKey : Int,
	phone : String,
	acctBal : Double,
	comment : String
)

trait SupplierSchema {
	val IR: StructExp
	import IR._

	def Supplier (
		suppKey : Rep[Int],
		name : Rep[String],
		address : Rep[String],
		nationKey : Rep[Int],
		phone : Rep[String],
		acctBal : Rep[Double],
		comment : Rep[String]
	) : Rep[Supplier] =
		struct[Supplier](
			ClassTag[Supplier]("Supplier"),
			Seq ("suppKey" -> suppKey, "name" -> name, "address" -> address, "nationKey" -> nationKey, "phone" -> phone,
				"acctBal" -> acctBal, "comment" -> comment)
		)

	case class SupplierInfixOps (x: Rep[Supplier]) {
		def suppKey : Rep[Int] = field[Int](x, "suppKey")
		def name : Rep[String] = field[String](x, "name")
		def address : Rep[String] = field[String](x, "address")
		def nationKey : Rep[Int] = field[Int](x, "nationKey")
		def phone : Rep[String] = field[String](x, "phone")
		def acctBal : Rep[Double] = field[Double](x, "acctBal")
		def comment : Rep[String] = field[String](x, "comment")
	}

	implicit def supplierToInfixOps (x: Rep[Supplier]) : SupplierInfixOps = SupplierInfixOps (x)
}
