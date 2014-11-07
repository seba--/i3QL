package sae.typecheck

import org.scalatest.{BeforeAndAfterEach, FunSuite}
import Generator._

/**
 * Created by seba on 05/11/14.
 */
class TestInitialChecking(checker: TypeCheck) extends FunSuite {
  def makeShared(h: Int) = makeBinAddTree(h, () => Num(1))
  val shared5 = makeShared(5)
  val shared10 = makeShared(10)
  val shared15 = makeShared(15)
  val shared20 = makeShared(20)

  test ("maximally shared tree with height 5") {
    val res = checker.typecheck(shared5)
    assertResult(Left(TNum))(res)
  }

  test ("maximally shared tree with height 10") {
    val res = checker.typecheck(shared10)
    assertResult(Left(TNum))(res)
  }

  test ("maximally shared tree with height 15") {
    val res = checker.typecheck(shared15)
    assertResult(Left(TNum))(res)
  }

  test ("maximally shared tree with height 20") {
    val res = checker.typecheck(shared20)
    assertResult(Left(TNum))(res)
  }


  def makeUnshared(h: Int) = {
    var i = 1
    def next() = {val r = i; i += 1; r}
    makeBinAddTree(h, () => Num(next()))
  }
  val unshared5 = makeUnshared(5)
  val unshared10 = makeUnshared(10)
  val unshared15 = makeUnshared(15)
  val unshared20 = makeUnshared(20)


  test ("maximally unshared tree with height 5") {
    val res = checker.typecheck(unshared5)
    assertResult(Left(TNum))(res)
  }

  test ("maximally unshared tree with height 10") {
    val res = checker.typecheck(unshared10)
    assertResult(Left(TNum))(res)
  }

  test ("maximally unshared tree with height 15") {
    val res = checker.typecheck(unshared15)
    assertResult(Left(TNum))(res)
  }

  test ("maximally unshared tree with height 20") {
    val res = checker.typecheck(unshared20)
    assertResult(Left(TNum))(res)
  }
}

class TestInitialChecking_DownUp extends TestInitialChecking(downup.ConstraintTypeCheck)
//class TestInitialChecking_ConstraintTypeCheck extends TestInitialChecking(bottomup.ConstraintTypeCheck)
class TestInitialChecking_ConstraintSolutionTypeCheck extends TestInitialChecking(bottomup.ConstraintSolutionTypeCheck)