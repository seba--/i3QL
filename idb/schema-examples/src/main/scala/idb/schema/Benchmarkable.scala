package idb.schema

import scala.virtualization.lms.common.StructExp
import scala.language.implicitConversions

trait Benchmarkable {
	val timestamp : Long =
		System.currentTimeMillis()
}

trait BenchmarkableSchema {
	val IR: StructExp

	import IR._

	case class BenchmarkableInfixOp (p: Rep[Benchmarkable])
	{
		def timestamp: Rep[Long] = field[Long](p, "timestamp")
	}

	implicit def nameableToInfixOp (p: Rep[Benchmarkable]) : BenchmarkableInfixOp =
		BenchmarkableInfixOp (p)
}