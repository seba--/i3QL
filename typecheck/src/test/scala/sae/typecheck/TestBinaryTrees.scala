package sae.typecheck

import org.scalatest.FunSuite
import Generator._

/**
 * Created by seba on 05/11/14.
 */
class TestBinaryTrees(checker: TypeCheck) extends FunSuite {

//  test ("maximally shared tree with height 5") {
//    val e = makeBinAddTree(5, () => Num(1))
//    val res = checker.typecheck(e)
//    assertResult(Left(TNum))(res)
//  }

  test ("maximally shared tree with height 10") {
    val e = makeBinAddTree(10, () => Num(1))
    val res = checker.typecheck(e)
    assertResult(Left(TNum))(res)
  }

  test ("maximally shared tree with height 20") {
    val e = makeBinAddTree(20, () => Num(1))
    val res = checker.typecheck(e)
    assertResult(Left(TNum))(res)
  }

//  test ("maximally shared tree with height 21") {
//    val e = makeBinAddTree(21, () => Num(1))
//    val res = checker.typecheck(e)
//    assertResult(Left(TNum))(res)
//  }

//  test ("maximally shared tree with height 22") {
//    val e = makeBinAddTree(22, () => Num(1))
//    val res = checker.typecheck(e)
//    assertResult(Left(TNum))(res)
//  }
}

class TestBinaryTrees_DownUp extends TestBinaryTrees(downup.ConstraintTypeCheck)
class TestBinaryTrees_ConstraintSolutionTypeCheck extends TestBinaryTrees(bottomup.ConstraintSolutionTypeCheck)