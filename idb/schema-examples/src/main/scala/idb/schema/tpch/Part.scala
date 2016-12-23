package idb.schema.tpch

import scala.virtualization.lms.common.StructExp
import scala.language.implicitConversions


/**
  * TODO: Add documentation of class!
  *
  * @author mirko
  */
case class Part(
	partKey : Int,
	name : String,
	mfgr : String,
	brand : String,
	typ : String,
	size :Int,
	container : String,
	retailPrice : Double,
	comment : String
)

trait PartSchema {
	val IR: StructExp
	import IR._

	def Part (
		partKey : Rep[Int],
		name : Rep[String],
		mfgr : Rep[String],
		brand : Rep[String],
		typ : Rep[String],
		size :Rep[Int],
		container : Rep[String],
		retailPrice : Rep[Double],
		comment : Rep[String]
	) : Rep[Part] =
		struct[Part](
			ClassTag[Part]("Part"),
			Seq ("partKey" -> partKey, "name" -> name, "mfgr" -> mfgr, "brand" -> brand, "typ" -> typ, "size" -> size,
				"container" -> container, "retailPrice" -> retailPrice, "comment" -> comment
			)
		)

	case class PartInfixOps (x: Rep[Part]) {
		def partKey : Rep[Int] = field[Int](x, "partKey")
		def name : Rep[String] = field[String](x, "name")
		def mfgr : Rep[String] = field[String](x, "mfgr")
		def brand : Rep[String] = field[String](x, "brand")
		def typ : Rep[String] = field[String](x, "typ")
		def size : Rep[Int] = field[Int](x, "size")
		def container : Rep[String] = field[String](x, "container")
		def retailPrice : Rep[Double] = field[Double](x, "retailPrice")
		def comment : Rep[String] = field[String](x, "comment")
	}

	implicit def partToInfixOps (x: Rep[Part]) : PartInfixOps = PartInfixOps (x)
}
