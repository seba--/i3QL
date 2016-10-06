package sae.typecheck

import idb.observer.NotifyObservers
import org.scalatest.{BeforeAndAfterEach, FunSuite}
import Generator._

/**
 * Created by seba on 05/11/14.
 */
class TestInitialChecking(checker: TypeCheck) extends FunSuite with BeforeAndAfterEach {
//  NotifyObservers.DEBUG = true

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
    if (Constraint.computeReqsTime != 0)
      Util.log(f"Time to compute req cons was   ${Constraint.computeReqsTime}%.3fms")
    Constraint.mergeFreshTime = 0
    Constraint.renameSolutionTime = 0
    Constraint.mergeReqsTime = 0
    Constraint.mergeSolutionTime = 0
    Constraint.extendSolutionTime = 0
    Constraint.computeReqsTime = 0
  }

  def testTypeCheck(desc: String, e: Exp, t: Type): Unit = {
    test(desc) {
      val prepared = Util.logTime("prepare " + desc)(checker.typecheck(e))
      Util.logTime("check " + desc) {
        val res = prepared()
        assertResult(Left(t))(res)
      }
    }
  }
  def testTypeCheck(desc: String, e: Exp)(f: Either[Type,TypeStuff.TError] => Boolean): Unit = {
    test(desc) {
      val prepared = Util.logTime("prepare " + desc)(checker.typecheck(e))
      Util.logTime("check " + desc) {
        val res = prepared()
        assert(f(res))
      }
    }
  }

  testTypeCheck("maximally shared tree with height 5", shared5, TNum)
  testTypeCheck("maximally shared tree with height 10", shared10, TNum)
  testTypeCheck("maximally shared tree with height 15", shared15, TNum)
//  testTypeCheck("maximally shared tree with height 20", shared20, TNum)


  def makeUnshared(h: Int) = {
    var i = 1
    def next() = {val r = i; i += 1; r}
    makeBinAddTree(h, () => Num(next()))
  }

  val unshared5 = makeUnshared(5)
  val unshared10 = makeUnshared(10)
  val unshared15 = makeUnshared(15)
  val unshared17 = makeUnshared(17)


  testTypeCheck("unshared tree with height 5", unshared5, TNum)
  testTypeCheck("unshared tree with height 10", unshared10, TNum)
  testTypeCheck("unshared tree with height 15", unshared15, TNum)
//  testTypeCheck("unshared tree with height 17", unshared17, TNum)

  def makeVarShared(h: Int) = Abs('x, makeBinAddTree(h, () => Var('x)))
  val varShared5 = makeVarShared(5)
  val varShared10 = makeVarShared(10)
  val varShared15 = makeVarShared(15)
  val varShared20 = makeVarShared(20)

//  testTypeCheck("var-shared tree with height 5", varShared5, TFun(TNum, TNum))
//  testTypeCheck("var-shared tree with height 10", varShared10, TFun(TNum, TNum))
//  testTypeCheck("var-shared tree with height 15", varShared15, TFun(TNum, TNum))
//  testTypeCheck("var-shared tree with height 20", varShared20, TFun(TNum, TNum))


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

//  testTypeCheck("var-unshared tree with height 5", varUnshared5, varUnshared5Type)
//  testTypeCheck("var-unshared tree with height 10", varUnshared10, varUnshared10Type)
//  testTypeCheck("var-unshared tree with height 15", varUnshared15, varUnshared15Type)
//  testTypeCheck("var-unshared tree with height 20", varUnshared20, varUnshared20Type)

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

  testTypeCheck("var-app-unshared tree with height 5", varAppUnshared5) {
    case Left(TFun(_, _) ) => true
    case _ => false
  }
  testTypeCheck("var-app-unshared tree with height 10", varAppUnshared10) {
    case Left(TFun(_, _) ) => true
    case _ => false
  }
//  testTypeCheck("var-app-unshared tree with height 15", varAppUnshared15) {
//    case Left(TFun(_, _) ) => true
//    case _ => false
//  }
//  testTypeCheck("var-app-unshared tree with height 20", varAppUnshared20) {
//    case Left(TFun(_, _) ) => true
//    case _ => false
//  }
}

class TestInitialChecking_DownUp extends TestInitialChecking(downup.ConstraintTypeCheck)
//class TestInitialChecking_ConstraintTypeCheck extends TestInitialChecking(bottomup.ConstraintTypeCheck)
class TestInitialChecking_ConstraintSolutionTypeCheck extends TestInitialChecking(bottomup.ConstraintSolutionTypeCheck)