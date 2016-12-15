package idb.schema.company

import idb.schema.{Benchmarkable, BenchmarkableSchema}

import scala.virtualization.lms.common.StructExp
import scala.language.implicitConversions

case class Supplier(id : Int, name : String, city : String)
	extends Nameable with Benchmarkable

trait SupplierSchema
	extends NameableSchema with BenchmarkableSchema {

	val IR: StructExp

	import IR._

	case class SupplierInfixOp (p: Rep[Supplier]) {
		def city: Rep[String] = field[String](p, "city")
	}

	implicit def supplierToInfixOp (p: Rep[Supplier]) : SupplierInfixOp =
		SupplierInfixOp (p)

}