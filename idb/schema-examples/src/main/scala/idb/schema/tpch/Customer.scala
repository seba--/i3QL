package idb.schema.tpch

import scala.virtualization.lms.common.StructExp

import scala.language.implicitConversions

/**
  * TODO: Add documentation of class!
  *
  * @author mirko
  */
case class Customer(
	custKey : Int,
	name : String,
	address : String,
	nationKey : Int,
	phone : String,
	acctBal : Double,
	mktSegment : String,
	comment : String
)

trait CustomerSchema {
	val IR: StructExp
	import IR._

	def Customer (
		custKey : Rep[Int],
		name : Rep[String],
		address : Rep[String],
		nationKey : Rep[Int],
		phone : Rep[String],
		acctBal : Rep[Double],
		mktSegment : Rep[String],
		comment : Rep[String]
    ) : Rep[Customer] =
		struct[Customer](
			ClassTag[Customer]("Customer"),
			Seq ("custKey" -> custKey, "name" -> name, "address" -> address, "nationKey" -> nationKey,
				"phone" -> phone, "acctBal" -> acctBal, "mktSegment" -> mktSegment, "comment" -> comment)
		)

	case class CustomerInfixOps (x: Rep[Customer]) {
		def custKey : Rep[Int] = field[Int](x, "custKey")
		def name : Rep[String] = field[String](x, "name")
		def address : Rep[String] = field[String](x, "address")
		def nationKey : Rep[Int] = field[Int](x, "nationKey")
		def phone : Rep[String] = field[String](x, "phone")
		def acctBal : Rep[Double] = field[Double](x, "acctBal")
		def mktSegment : Rep[String] = field[String](x, "mktSegment")
		def comment : Rep[String] = field[String](x, "comment")
	}

	implicit def customerToInfixOps (x: Rep[Customer]) : CustomerInfixOps = CustomerInfixOps (x)
}
