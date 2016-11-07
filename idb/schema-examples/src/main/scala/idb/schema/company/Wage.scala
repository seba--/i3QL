package idb.schema.company

import idb.schema.{Benchmarkable, BenchmarkableSchema}

import scala.virtualization.lms.common.StructExp
import scala.language.implicitConversions

/**
  * Created by mirko on 07.11.16.
  */
case class Wage(employeeId : Int, wagePerHour : Double, hoursPerMonth : Double)
	extends Benchmarkable

trait WageSchema extends BenchmarkableSchema {
	val IR: StructExp

	import IR._

	case class WageInfixOp (p: Rep[Wage])
	{

		def employeeId: Rep[Int] = field[Int](p, "employeeId")

		def wagePerHour: Rep[Double] = field[Double](p, "wagePerHour")

		def hoursPerMonth: Rep[Double] = field[Double](p, "hoursPerMonth")


	}

	implicit def wageToInfixOp (p: Rep[Wage]) : WageInfixOp =
		WageInfixOp (p)
}