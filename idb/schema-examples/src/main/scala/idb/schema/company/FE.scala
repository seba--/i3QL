package idb.schema.company

import idb.schema.{Benchmarkable, BenchmarkableSchema}

import scala.virtualization.lms.common.StructExp
import scala.language.implicitConversions

/**
  * Factory-Employee
  */
case class FE(factoryId : Int, employeeId : Int, job : String)
	extends Benchmarkable

trait FESchema extends BenchmarkableSchema {
	val IR: StructExp

	import IR._

	case class FEInfixOp (p: Rep[FE])
	{

		def factoryId: Rep[Int] = field[Int](p, "factoryId")

		def employeeId: Rep[Int] = field[Int](p, "employeeId")

		def job: Rep[String] = field[String](p, "job")

	}

	implicit def feToInfixOp (p: Rep[FE]) : FEInfixOp =
		FEInfixOp (p)
}