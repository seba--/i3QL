package idb.schema.company

import scala.language.implicitConversions
import scala.virtualization.lms.common.StructExp

/**
  * Created by mirko on 07.11.16.
  */
trait Nameable {
	val id : Int
	val name : String
}

trait NameableSchema {
	val IR: StructExp

	import IR._

	case class NameableInfixOp (p: Rep[Nameable])
	{

		def id: Rep[Int] = field[Int](p, "id")

		def name: Rep[String] = field[String](p, "name")

	}

	implicit def nameableToInfixOp (p: Rep[Nameable]) : NameableInfixOp =
		NameableInfixOp (p)
}
