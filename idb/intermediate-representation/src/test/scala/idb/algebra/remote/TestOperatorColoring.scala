package idb.algebra.remote

import idb.algebra.TestUtils
import idb.algebra.ir.{RelationalAlgebraIRAggregationOperators, RelationalAlgebraIRBasicOperators}
import idb.lms.extensions.{FunctionUtils, ScalaOpsExpOptExtensions}
import idb.query.QueryEnvironment
import idb.query.colors.Color
import org.junit.Assert._
import org.junit.Test

import scala.language.implicitConversions


case class Course (number: Int, title: String, creditPoints: Int)

trait CourseSchema
{
	import scala.virtualization.lms.common.StructExp
	val IR: StructExp

	import IR._

	def Course (number: Rep[Int], title: Rep[String], creditPoints: Rep[Int]) =
		struct[Course](
			ClassTag[Course]("Course"),
			Seq ("number" -> number, "title" -> title, "creditPoints" -> creditPoints)
		)
}

/**
 * @author Mirko Köhler
 */
class TestOperatorColoring
	extends RelationalAlgebraIRBasicOperators
	with RelationalAlgebraIRAggregationOperators
	with ScalaOpsExpOptExtensions
	with TestUtils
	with FunctionUtils
{

	@Test
	def testCrossProduct(): Unit = {
		implicit val queryEnvironment = QueryEnvironment.Local

		val table1 = table(scala.List.empty[String], color = Color("A"))
		val table2 = table(scala.List.empty[String], color = Color("B"))

		val rel = crossProduct(table1, table2)

		assertEquals(Color.tupled(Color("A"), Color("B")), rel.color)
	}

	@Test
	def testAggregation(): Unit = {
		implicit val queryEnvironment = QueryEnvironment.Local

		val table1 = table(scala.List.empty[Course], color = Color("number" -> Color("blue"), "title" -> Color("red"), "creditPoints" -> Color("green")))

		val rel = aggregationSelfMaintainedWithoutGrouping[Course, Int](
			table1,
			start = 0,
			added = (t : Rep[(Course, Int)]) => field[Int](t, "_2") + field[Int](field[Course](t, "_1"), "creditPoints"),
			removed = (t : Rep[(Course, Int)]) => field[Int](t, "_2") - field(field[Course](t, "_1"), "creditPoints"),
			updated = (t : Rep[(Course, Course, Int)]) => field[Int](t, "_3") + field(field[Course](t, "_1"), "creditPoints") - field(field[Course](t, "_2"), "creditPoints")

		)

		assertEquals(Color("green"), rel.color)
	}




}


