package idb.schema.company

import idb.schema.{Benchmarkable, BenchmarkableSchema}

import scala.virtualization.lms.common.StructExp
import scala.language.implicitConversions

case class FP(factoryId : Int, productId : Int)
	extends Benchmarkable

trait FPSchema extends BenchmarkableSchema {
	val IR: StructExp

	import IR._

	case class FPInfixOp (p: Rep[FP])
	{
		def factoryId: Rep[Int] = field[Int](p, "factoryId")

		def productId: Rep[Int] = field[Int](p, "productId")
	}

	implicit def fpToInfixOp (p: Rep[FP]) : FPInfixOp =
		FPInfixOp (p)
}
