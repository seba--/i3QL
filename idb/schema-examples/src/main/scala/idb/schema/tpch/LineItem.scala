package idb.schema.tpch

import java.util.Date

import scala.virtualization.lms.common.StructExp

import scala.language.implicitConversions


/**
  * TODO: Add documentation of class!
  *
  * @author mirko
  */
case class LineItem(
	orderKey : Int,
	partKey : Int,
	suppKey : Int,
	lineNumber : Int,
	quantity : Double,
	extendedPrice : Double,
	discount : Double,
	tax : Double,
	returnFlag : Char,
	lineStatus : Char,
	shipUpdate : Date,
	commitDate : Date,
	receiptDate : Date,
	shipInstruct : String,
	shipMode : String,
	comment : String
)

trait LineItemSchema {
	val IR: StructExp
	import IR._

	def LineItem (
		orderKey : Rep[Int],
		partKey : Rep[Int],
		suppKey : Rep[Int],
		lineNumber : Rep[Int],
		quantity : Rep[Double],
		extendedPrice : Rep[Double],
		discount : Rep[Double],
		tax : Rep[Double],
		returnFlag : Rep[Char],
		lineStatus : Rep[Char],
		shipUpdate : Rep[Date],
		commitDate : Rep[Date],
		receiptDate : Rep[Date],
		shipInstruct : Rep[String],
		shipMode : Rep[String],
		comment : Rep[String]
	) : Rep[LineItem] =
		struct[LineItem](
			ClassTag[LineItem]("LineItem"),
			Seq ("orderKey" -> orderKey, "partKey" -> partKey, "suppKey" -> suppKey, "lineNumber" -> lineNumber,
				"quantity" -> quantity, "extendedPrice" -> extendedPrice, "discount" -> discount, "tax" -> tax,
				"returnFlag" -> returnFlag, "lineStatus" -> lineStatus, "shipUpdate" -> shipUpdate, "commitDate" -> commitDate,
				"receiptDate" -> receiptDate, "shipInstruct" -> shipInstruct, "shipMode" -> shipMode, "comment" -> comment
			)
		)

	case class LineItemInfixOps (x: Rep[LineItem]) {
		def orderKey : Rep[Int] = field[Int](x, "orderKey")
		def partKey : Rep[Int] = field[Int](x, "partKey")
		def suppKey : Rep[Int] = field[Int](x, "suppKey")
		def lineNumber : Rep[Int] = field[Int](x, "lineNumber")
		def quantity : Rep[Double] = field[Double](x, "quantity")
		def extendedPrice : Rep[Double] = field[Double](x, "extendedPrice")
		def discount : Rep[Double] = field[Double](x, "discount")
		def tax : Rep[Double] = field[Double](x, "tax")
		def returnFlag : Rep[Char] = field[Char](x, "returnFlag")
		def lineStatus : Rep[Char] = field[Char](x, "lineStatus")
		def shipUpdate : Rep[Date] = field[Date](x, "shipUpdate")
		def commitDate : Rep[Date] = field[Date](x, "commitDate")
		def receiptDate : Rep[Date] = field[Date](x, "receiptDate")
		def shipInstruct : Rep[String] = field[String](x, "shipInstruct")
		def shipMode : Rep[String] = field[String](x, "shipMode")
		def comment : Rep[String] = field[String](x, "comment")
	}

	implicit def lineItemToInfixOps (x: Rep[LineItem]) : LineItemInfixOps = LineItemInfixOps (x)
}
