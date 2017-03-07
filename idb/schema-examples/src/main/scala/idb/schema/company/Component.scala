package idb.schema.company

import idb.schema.{Benchmarkable, BenchmarkableSchema}

import scala.virtualization.lms.common.StructExp

import scala.language.implicitConversions

case class Component(id : Int, name : String, material : String)
	extends Nameable with Benchmarkable

trait ComponentSchema
	extends NameableSchema with BenchmarkableSchema {

	val IR: StructExp

	import IR._

	case class ComponentInfixOp (p: Rep[Component]) {
		def material: Rep[String] = field[String](p, "material")
	}

	implicit def componentToInfixOp (p: Rep[Component]) : ComponentInfixOp =
		ComponentInfixOp (p)

}
