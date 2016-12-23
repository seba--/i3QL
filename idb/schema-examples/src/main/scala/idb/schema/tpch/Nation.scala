package idb.schema.tpch

import scala.virtualization.lms.common.StructExp

import scala.language.implicitConversions


/**
  * TODO: Add documentation of class!
  *
  * @author mirko
  */
case class Nation(nationKey : Int, name : String, regionKey : Int, comment : String)

trait NationSchema {
	val IR: StructExp
	import IR._

	def Nation (nationKey : Rep[Int], name : Rep[String], regionKey : Rep[Int], comment : Rep[String]) : Rep[Nation] =
		struct[Nation](
			ClassTag[Nation]("Nation"),
			Seq ("nationKey" -> nationKey, "name" -> name, "regionKey" -> regionKey, "comment" -> comment)
		)

	case class NationInfixOps (x: Rep[Nation]) {
		def nationKey : Rep[Int] = field[Int](x, "nationKey")
		def name : Rep[String] = field[String](x, "name")
		def regionKey : Rep[Int] = field[Int](x, "regionKey")
		def comment : Rep[String] = field[String](x, "comment")
	}

	implicit def nationToInfixOps (x: Rep[Nation]) : NationInfixOps = NationInfixOps (x)
}

