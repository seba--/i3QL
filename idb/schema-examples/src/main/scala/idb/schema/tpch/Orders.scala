package idb.schema.tpch

import java.util.Date

import scala.virtualization.lms.common.StructExp

import scala.language.implicitConversions


/**
  * TODO: Add documentation of class!
  *
  * @author mirko
  */
case class Orders(
	orderKey : Int,
	custKey : Int,
	orderStatus : Char,
	totalPrice : Double,
	orderDate : Date,
	orderPriority : String,
	clerk : String,
	shipPriority : Int,
	comment : String
)

trait OrdersSchema {
	val IR: StructExp
	import IR._

	def Orders (
		orderKey : Rep[Int],
		custKey : Rep[Int],
		orderStatus : Rep[Char],
		totalPrice : Rep[Double],
		orderDate : Rep[Date],
		orderPriority : Rep[String],
		clerk : Rep[String],
		shipPriority : Rep[Int],
		comment : Rep[String]
	) : Rep[Orders] =
		struct[Orders](
			ClassTag[Orders]("Orders"),
			Seq ("orderKey" -> orderKey, "custKey" -> custKey, "orderStatus" -> orderStatus,
				"totalPrice" -> totalPrice, "orderDate" -> orderDate, "orderPriority" -> orderPriority,
				"clerk" -> clerk, "shipPriority" -> shipPriority, "comment" -> comment
			)
		)

	case class OrdersInfixOps (x: Rep[Orders]) {
		def orderKey : Rep[Int] = field[Int](x, "orderKey")
		def custKey : Rep[Int] = field[Int](x, "custKey")
		def orderStatus : Rep[Char] = field[Char](x, "orderKey")
		def totalPrice : Rep[Double] = field[Double](x, "totalPrice")
		def orderDate : Rep[Date] = field[Date](x, "orderDate")
		def orderPriority : Rep[String] = field[String](x, "orderPriority")
		def clerk : Rep[String] = field[String](x, "clerk")
		def shipPriority : Rep[Int] = field[Int](x, "shipPriority")
		def comment : Rep[String] = field[String](x, "comment")
	}

	implicit def ordersToInfixOps (x: Rep[Orders]) : OrdersInfixOps = OrdersInfixOps (x)
}
