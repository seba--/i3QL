package idb.schema.hospital

import scala.language.implicitConversions
import scala.virtualization.lms.common.StructExp

/**
 * @author Mirko Köhler
 */



case class Person (personId : Int, name : String, yearOfBirth : Int)



trait PersonSchema
{
	val IR: StructExp

	import IR._

	def Person (personId : Rep[Int], name: Rep[String], yearOfBirth: Rep[Int]) =
		struct[Person](
			ClassTag[Person]("Person"),
			Seq ("personId" -> personId, "name" -> name, "yearOfBirth" -> yearOfBirth)
		)

	case class PersonInfixOps (x: Rep[Person])
	{
		def personId : Rep[Int] = field[Int](x, "personId")
		
		def name: Rep[String] = field[String](x, "name")

		def yearOfBirth: Rep[Int] = field[Int](x, "yearOfBirth")

	}

	implicit def personToInfixOps (x: Rep[Person]) : PersonInfixOps = PersonInfixOps (x)
}


