package idb.schema.company

import idb.schema.{Benchmarkable, BenchmarkableSchema}

import scala.virtualization.lms.common.StructExp
import scala.language.implicitConversions

/**
  * Created by mirko on 07.11.16.
  */
case class SC(supplierId : Int, componentId : Int, inventory : Int, price : Double)
	extends Benchmarkable

trait SCSchema extends BenchmarkableSchema {
	val IR: StructExp

	import IR._

	case class SCInfixOp (p: Rep[SC])
	{

		def supplierId: Rep[Int] = field[Int](p, "supplierId")

		def componentId: Rep[Int] = field[Int](p, "componentId")

		def inventory: Rep[Int] = field[Int](p, "inventory")

		def price: Rep[Double] = field[Double](p, "price")

	}

	implicit def scToInfixOp (p: Rep[SC]) : SCInfixOp =
		SCInfixOp (p)
}