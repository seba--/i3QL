package idb.schema.company

import idb.schema.{Benchmarkable, BenchmarkableSchema}

import scala.virtualization.lms.common.StructExp
import scala.language.implicitConversions

/**
  * Created by mirko on 07.11.16.
  */
case class PC(productId : Int, componentId : Int, quantity : Int)
	extends Benchmarkable

trait PCSchema extends BenchmarkableSchema  {
	val IR: StructExp

	import IR._

	case class PCInfixOp (p: Rep[PC])
	{

		def productId: Rep[Int] = field[Int](p, "productId")

		def componentId: Rep[Int] = field[Int](p, "componentId")

		def quantity: Rep[Int] = field[Int](p, "quantity")

	}

	implicit def pcToInfixOp (p: Rep[PC]) : PCInfixOp =
		PCInfixOp (p)
}