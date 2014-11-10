package sae.typecheck

import org.scalatest.{BeforeAndAfterEach, FunSuite}
import Generator._

/**
 * Created by seba on 05/11/14.
 */
class TestInitialChecking(checker: TypeCheck) extends FunSuite with BeforeAndAfterEach {
  def makeShared(h: Int) = makeBinAddTree(h, () => Num(1))
  val shared5 = makeShared(5)
  val shared10 = makeShared(10)
  val shared15 = makeShared(15)
  val shared20 = makeShared(20)

  override def beforeEach(): Unit = {
    checker.reset()
  }

  override def afterEach(): Unit = {
    if (Constraint.mergeFreshTime != 0)
      Util.log(f"Time to merge fresh vars was   ${Constraint.mergeFreshTime}%.3fms")
    if (Constraint.renameSolutionTime != 0)
      Util.log(f"Time to rename solutions was   ${Constraint.renameSolutionTime}%.3fms")
    if (Constraint.mergeReqsTime != 0)
      Util.log(f"Time to merge requirements was ${Constraint.mergeReqsTime}%.3fms")
    if (Constraint.mergeSolutionTime != 0)
      Util.log(f"Time to merge solutions was    ${Constraint.mergeSolutionTime}%.3fms")
    if (Constraint.extendSolutionTime != 0)
      Util.log(f"Time to extend solutions was   ${Constraint.extendSolutionTime}%.3fms")
    Constraint.mergeFreshTime = 0
    Constraint.renameSolutionTime = 0
    Constraint.mergeReqsTime = 0
    Constraint.mergeSolutionTime = 0
    Constraint.extendSolutionTime = 0
  }

//  test ("maximally shared tree with height 5") {
//    val res = checker.typecheck(shared5)
//    assertResult(Left(TNum))(res)
//  }
//
//  test ("maximally shared tree with height 10") {
//    val res = checker.typecheck(shared10)
//    assertResult(Left(TNum))(res)
//  }
//
//  test ("maximally shared tree with height 15") {
//    val res = checker.typecheck(shared15)
//    assertResult(Left(TNum))(res)
//  }
//
//  test ("maximally shared tree with height 20") {
//    val res = checker.typecheck(shared20)
//    assertResult(Left(TNum))(res)
//  }


  def makeUnshared(h: Int) = {
    var i = 1
    def next() = {val r = i; i += 1; r}
    makeBinAddTree(h, () => Num(next()))
  }
  val unshared5 = makeUnshared(5)
  val unshared10 = makeUnshared(10)
  val unshared15 = makeUnshared(15)
  val unshared17 = makeUnshared(17)


  test ("unshared tree with height 5") {
    val resUnshared5 = Util.logTime("Prepare unshared tree with height 5"){checker.typecheck(unshared5)}
    Util.logTime("check unshared tree with height 5") {
      val res = resUnshared5()
      assertResult(Left(TNum))(res)
    }
  }

  test ("unshared tree with height 10") {
    val resUnshared10 = Util.logTime("Prepare unshared tree with height 10"){checker.typecheck(unshared10)}
    Util.logTime("check unshared tree with height 10") {
      val res = resUnshared10()
      assertResult(Left(TNum))(res)
    }
  }

  test ("unshared tree with height 15") {
    val resUnshared15 = Util.logTime("prepare unshared tree with height 15"){checker.typecheck(unshared15)}
    Util.logTime("check unshared tree with height 15") {
      val res = resUnshared15()
      assertResult(Left(TNum))(res)
    }
  }

  test ("unshared tree with height 17") {
    val resUnshared17 = Util.logTime("prepare unshared tree with height 17"){checker.typecheck(unshared17)}
    Util.logTime("check unshared tree with height 17") {
      val res = resUnshared17()
      assertResult(Left(TNum))(res)
    }
  }

  def makeVarShared(h: Int) = Abs('x, makeBinAddTree(h, () => Var('x)))
  val varShared5 = makeVarShared(5)
  val varShared10 = makeVarShared(10)
  val varShared15 = makeVarShared(15)
  val varShared20 = makeVarShared(20)

//  test ("var-shared tree with height 5") {
//    val res = checker.typecheck(varShared5)
//    assertResult(Left(TFun(TNum, TNum)))(res)
//  }
//
//  test ("var-shared tree with height 10") {
//    val res = checker.typecheck(varShared10)
//    assertResult(Left(TFun(TNum, TNum)))(res)
//  }

//  test ("var-shared tree with height 15") {
//    val res = checker.typecheck(varShared15)
//    assertResult(Left(TFun(TNum, TNum)))(res)
//  }

//  test ("var-shared tree with height 20") {
//    val res = checker.typecheck(varShared20)
//    assertResult(Left(TFun(TNum, TNum)))(res)
//  }


  def makeVarUnshared(h: Int) = {
    var i = 1
    def next() = {val r = i; i += 1; Symbol(s"x$r")}
    var t = makeBinAddTree(h, () => Var(next()))
    for (j <- 1 until i)
      t = Abs(Symbol(s"x$j"), t)
    t
  }
  val varUnshared5 = makeVarUnshared(5)
  val varUnshared10 = makeVarUnshared(10)
  val varUnshared15 = makeVarUnshared(15)
  val varUnshared20 = makeVarUnshared(20)
  val varUnshared5Type = makeFunType(Math.pow(2,5-1).toInt, TNum, () => TNum)
  val varUnshared10Type = makeFunType(Math.pow(2,10-1).toInt, TNum, () => TNum)
  val varUnshared15Type = makeFunType(Math.pow(2,15-1).toInt, TNum, () => TNum)
  val varUnshared20Type = makeFunType(Math.pow(2,20-1).toInt, TNum, () => TNum)

//  test ("var-unshared tree with height 5") {
//    val res = checker.typecheck(varUnshared5)
//    assertResult(Left(varUnshared5Type))(res)
//  }
//
//  test ("var-unshared tree with height 10") {
//    val res = checker.typecheck(varUnshared10)
//    assertResult(Left(varUnshared10Type))(res)
//  }

//  test ("var-unshared tree with height 15") {
//    val res = checker.typecheck(varUnshared15)
//    assertResult(Left(varUnshared15Type))(res)
//  }

//  test ("var-unshared tree with height 20") {
//    val res = checker.typecheck(varUnshared20)
//    assertResult(Left(varUnshared20Type))(res)
//  }

  def makeVarAppUnshared(h: Int) = {
    var i = 1
    def next() = {val r = i; i += 1; Symbol(s"x$r")}
    var t = makeBinAppTree(h, () => Var(next()))
    for (j <- 1 until i)
      t = Abs(Symbol(s"x$j"), t)
    t
  }
  val varAppUnshared5 = makeVarAppUnshared(5)
  val varAppUnshared10 = makeVarAppUnshared(10)
  val varAppUnshared15 = makeVarAppUnshared(15)
  val varAppUnshared20 = makeVarAppUnshared(20)

//  test ("var-app-unshared tree with height 5") {
//    val res = checker.typecheck(varAppUnshared5)
//    assert(res match {
//      case Left(TFun(_, _) ) => true
//      case _ => false
//    })
//  }
//
//  test ("var-app-unshared tree with height 10") {
//    val res = checker.typecheck(varAppUnshared10)
//    assert(res match {
//      case Left(TFun(_, _) ) => true
//      case _ => false
//    })
//  }

//    test ("var-app-unshared tree with height 15") {
//      val res = checker.typecheck(varUnshared15)
//      assert(res match {
//        case Left(TFun(_, _) ) => true
//        case _ => false
//      })
//    }

//    test ("var-app-unshared tree with height 20") {
//      val res = checker.typecheck(varUnshared20)
//      assert(res match {
//        case Left(TFun(_, _) ) => true
//        case _ => false
//      })
//    }
}

class TestInitialChecking_DownUp extends TestInitialChecking(downup.ConstraintTypeCheck)
//class TestInitialChecking_ConstraintTypeCheck extends TestInitialChecking(bottomup.ConstraintTypeCheck)
class TestInitialChecking_ConstraintSolutionTypeCheck extends TestInitialChecking(bottomup.ConstraintSolutionTypeCheck)