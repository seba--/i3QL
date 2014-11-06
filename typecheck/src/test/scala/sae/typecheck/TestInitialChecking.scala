package sae.typecheck

import org.scalatest.{BeforeAndAfterEach, FunSuite}
import Generator._

/**
 * Created by seba on 05/11/14.
 */
class TestInitialChecking(checker: TypeCheck) extends FunSuite {
  val e5 = makeBinAddTree(5, () => Num(1))
  val e10 = makeBinAddTree(10, () => Num(1))
  val e20 = makeBinAddTree(20, () => Num(1))
  val e21 = makeBinAddTree(21, () => Num(1))
  val e22 = makeBinAddTree(22, () => Num(1))

  test ("maximally shared tree with height 5") {
    val res = checker.typecheck(e5)
    assertResult(Left(TNum))(res)
  }

  test ("maximally shared tree with height 10") {
    val res = checker.typecheck(e10)
    assertResult(Left(TNum))(res)
  }

  test ("maximally shared tree with height 20") {
    val res = checker.typecheck(e20)
    assertResult(Left(TNum))(res)
  }

  test ("maximally shared tree with height 21") {
    val res = checker.typecheck(e21)
    assertResult(Left(TNum))(res)
  }

  test ("maximally shared tree with height 22") {
    val res = checker.typecheck(e22)
    assertResult(Left(TNum))(res)
  }
}

class TestInitialChecking_DownUp extends TestInitialChecking(downup.ConstraintTypeCheck)
//class TestInitialChecking_ConstraintTypeCheck extends TestInitialChecking(bottomup.ConstraintTypeCheck)
class TestInitialChecking_ConstraintSolutionTypeCheck extends TestInitialChecking(bottomup.ConstraintSolutionTypeCheck)