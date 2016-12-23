package idb.schema.tpch

import scala.virtualization.lms.common.StructExp
import scala.language.implicitConversions



/**
  * TODO: Add documentation of class!
  *
  * @author mirko
  */
case class Region(regionKey : Int, name : String, comment : String)


trait RegionSchema {
	val IR: StructExp
	import IR._

	def Region(regionKey : Rep[Int], name : Rep[String], comment : Rep[String]) : Rep[Region] =
		struct[Region](
			ClassTag[Region]("Region"),
			Seq ("regionKey" -> regionKey, "name" -> name, "comment" -> comment)
		)

	case class RegionInfixOps (x: Rep[Region]) {
		def regionKey : Rep[Int] = field[Int](x, "regionKey")
		def name : Rep[String] = field[String](x, "name")
		def comment : Rep[String] = field[String](x, "comment")
	}

	implicit def regionToInfixOps (x: Rep[Region]) : RegionInfixOps = RegionInfixOps (x)
}