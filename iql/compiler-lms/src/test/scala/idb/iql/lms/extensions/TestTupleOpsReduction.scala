package idb.iql.lms.extensions

import org.junit.{Ignore, Test}
import scala.virtualization.lms.common.{ScalaOpsPkgExp, LiftAll}

/**
 *
 * @author Ralf Mitschke
 */
class TestTupleOpsReduction
  extends LiftAll with ScalaOpsExpOptExtensions with ScalaOpsPkgExp with TestUtils
{

  @Test
  def testTuple2ReduceDirect ()
  {
    val f1 = (x: Rep[Int]) => (x, x > 0)._2
    val f2 = (x: Rep[Int]) => x > 0

    assertSameReified (f2, f1)
  }

  @Test
  def testTuple2ReduceFunComposeThenDirect ()
  {
    val f1 = (x: Rep[Int]) => (x, x > 0)
    val f2 = (x: Rep[(Int, Boolean)]) => x._2
    val f3 = (x: Rep[Int]) => f2 (f1 (x))

    val f4 = (x: Rep[Int]) => x > 0

    assertSameReified (f3, f4)
  }

  @Test
  def testTuple2ReduceFunComposeThenEqTest ()
  {
    val f1 = (x: Rep[Int]) => (x, x > 0)
    val f2 = (x: Rep[(Int, Boolean)]) => x._2 == true
    val f3 = (x: Rep[Int]) => f2 (f1 (x))

    val f4 = (x: Rep[Int]) => x > 0 == true

    assertSameReified (f3, f4)
  }

  @Test
  @Ignore
  def testTuple2ReduceDirectConditional ()
  {
    val f1 = (x: Rep[Int]) => if (x > 0) (x, unit (true)) else (x, unit (false))._2
    val f2 = (x: Rep[Int]) => if (x > 0) unit (true) else unit (false)

    assertSameReified (f2, f1)
  }

}
