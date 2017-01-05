package idb.algebra.remote

import idb.algebra.TestUtils
import idb.algebra.ir.{RelationalAlgebraIRAggregationOperators, RelationalAlgebraIRBasicOperators}
import idb.lms.extensions.{FunctionUtils, ScalaOpsPkgExpOptExtensions}
import idb.query.QueryEnvironment
import idb.query.taint.Taint
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
class TestOperatorTaint
	extends RelationalAlgebraIRBasicOperators
	with RelationalAlgebraIRAggregationOperators
	with ScalaOpsPkgExpOptExtensions
	with TestUtils
	with FunctionUtils
{

	@Test
	def testCrossProduct(): Unit = {
		implicit val env = QueryEnvironment.Local

		val table1 = table(scala.List.empty[String], taint = Taint("A"))
		val table2 = table(scala.List.empty[String], taint = Taint("B"))

		val rel = crossProduct(table1, table2)

		assertEquals(Taint.tupled(Taint("A"), Taint("B")), rel.taint)
	}

	@Test
	def testAggregation(): Unit = {
		implicit val env = QueryEnvironment.Local

		val table1 = table(scala.List.empty[Course], taint = Taint("number" -> Taint("blue"), "title" -> Taint("red"), "creditPoints" -> Taint("green")))

		val rel = aggregationSelfMaintainedWithoutGrouping[Course, Int](
			table1,
			start = 0,
			added = (t : Rep[(Course, Int)]) => field[Int](t, "_2") + field[Int](field[Course](t, "_1"), "creditPoints"),
			removed = (t : Rep[(Course, Int)]) => field[Int](t, "_2") - field(field[Course](t, "_1"), "creditPoints"),
			updated = (t : Rep[(Course, Course, Int)]) => field[Int](t, "_3") + field(field[Course](t, "_1"), "creditPoints") - field(field[Course](t, "_2"), "creditPoints")

		)

		assertEquals(Taint("green"), rel.taint)
	}




}


