package idb.schema.company

import idb.schema.{Benchmarkable, BenchmarkableSchema}

import scala.language.implicitConversions
import scala.virtualization.lms.common.StructExp

/**
  * Created by mirko on 07.11.16.
  */
case class Factory(id : Int, city : String)
	extends Benchmarkable

trait FactorySchema extends BenchmarkableSchema {
	val IR: StructExp

	import IR._

	case class FactoryInfixOp (p: Rep[Factory])
	{

		def id: Rep[Int] = field[Int](p, "id")

		def city: Rep[String] = field[String](p, "city")

	}

	implicit def factoryToInfixOp (p: Rep[Factory]) : FactoryInfixOp =
		FactoryInfixOp (p)
}


