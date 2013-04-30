package idb.iql.lms.extensions

import scala.virtualization.lms.common.{ScalaOpsPkgExp, LiftAll}
import org.junit.Assert._
import org.junit.Test

/**
 *
 * @author Ralf Mitschke
 */
class TestAlphaEquivalence
  extends LiftAll with ScalaOpsExpOptExtensions with ScalaOpsPkgExp with TestUtils
{

  @Test
  def testSameMethodBody ()
  {
    assertSameReified (
      (x: Rep[Int]) => x + 1,
      (y: Rep[Int]) => y + 1
    )
  }

  @Test
  def testConstantFolding ()
  {
    assertSameReified (
      (x: Rep[Int]) => x + 1 + 1,
      (y: Rep[Int]) => y + 2
    )
  }

  @Test
  def testMethodConcatenation ()
  {
    def f1 (x: Rep[Int]) = x + 1
    def f2 (x: Rep[Int]) = x + 2

    assertSameReified (
      (x: Rep[Int]) => f1 (f2 (x)),
      (y: Rep[Int]) => y + 3
    )

    assertSameReified (
      (x: Rep[Int]) => f2 (f1 (x)),
      (y: Rep[Int]) => y + 3
    )
  }

}
