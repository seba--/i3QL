package idb.schema.hospital

import scala.language.implicitConversions
import scala.virtualization.lms.common.StructExp


/**
 * @author Mirko Köhler
 */
case class KnowledgeData (symptom : String, diagnosis : String, year : Int)



trait KnowledgeDataSchema
{
	val IR: StructExp

	import IR._

	def KnowledgeData (symptom: Rep[String], diagnosis: Rep[String], year : Rep[Int]) =
		struct[KnowledgeData](
			ClassTag[KnowledgeData]("KnowledgeData"),
			Seq ("symptom" -> symptom, "diagnosis" -> diagnosis, "year" -> year)
		)

	case class KnowledgeDataInfixOps (x: Rep[KnowledgeData])
	{
		def symptom: Rep[String] = field[String](x, "symptom")

		def diagnosis: Rep[String] = field[String](x, "diagnosis")

		def year: Rep[Int] = field[Int](x, "year")


	}

	implicit def knowledgeDataToInfixOps (x: Rep[KnowledgeData]) : KnowledgeDataInfixOps = KnowledgeDataInfixOps (x)
}
