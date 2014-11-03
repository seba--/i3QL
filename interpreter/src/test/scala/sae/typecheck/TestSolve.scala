package sae.typecheck

import idb.operators.NotSelfMaintainableAggregateFunction
import org.scalatest.FunSuite
import sae.typecheck.SolveHelper.{Unsolvable, TSubst}
import Constraint.Constraint
import ConstraintTypecheck._
import Type._

/**
 * Created by seba on 03/11/14.
 */
class TestSolve(solver: NotSelfMaintainableAggregateFunction[Constraint, ()=>(TSubst, Unsolvable)] with Resetable) extends FunSuite {

  def addEq(t1: Type, t2: Type) = solver.add(EqConstraint(t1, t2), Seq())
  def remEq(t1: Type, t2: Type) = solver.remove(EqConstraint(t1, t2), Seq())

  def assertSolution(s: TSubst) = {
    val (was, unres) = solver.get()
    assert(unres.isEmpty, s"Expected solution $s but found unresolvable constraints $unres")
    assertResult(s)(was)
  }

  def assertUnsolvable() = {
    val (was, unres) = solver.get()
    assert(!unres.isEmpty, s"Expected at least one unresolvable constraint, but got solution $was")
  }

  test ("independent constraints") {
    addEq(TVar('x), TNum)
    addEq(TVar('y), TString)
    assertSolution(Map('x -> TNum, 'y -> TString))

    addEq(TNum, TVar('z1))
    addEq(TString, TVar('z2))
    assertSolution(Map('x -> TNum, 'y -> TString, 'z1 -> TNum, 'z2 -> TString))

    remEq(TNum, TVar('z1))
    assertSolution(Map('x -> TNum, 'y -> TString, 'z2 -> TString))

    remEq(TVar('x), TNum)
    remEq(TVar('y), TString)
    assertSolution(Map('z2 -> TString))

    solver.reset()
  }


  test ("dependent constraints") {
    addEq(TVar('x), TNum)
    addEq(TVar('y), TString)
    assertSolution(Map('x -> TNum, 'y -> TString))

    addEq(TNum, TVar('x))
    assertSolution(Map('x -> TNum, 'y -> TString))

    addEq(TString, TVar('x))
    assertUnsolvable()

    remEq(TVar('x), TNum)
    assertUnsolvable()

    remEq(TNum, TVar('x))
    assertSolution(Map('x -> TString, 'y -> TString))

    solver.reset()
  }

  test ("nested constraints") {
    addEq(TFun(TVar('x), TVar('y)), TVar('f))
    addEq(TFun(TVar('y), TVar('x)), TVar('f))
    assertSolution(Map('x -> TVar('y), 'f -> TFun(TVar('y), TVar('y))))

    addEq(TVar('y), TFun(TVar('z1), TVar('z2)))
    assertSolution(Map('x -> TFun(TVar('z1), TVar('z2)), 'f -> TFun(TFun(TVar('z1), TVar('z2)), TFun(TVar('z1), TVar('z2))), 'y -> TFun(TVar('z1), TVar('z2))))

    addEq(TVar('f), TFun(TFun(TVar('a), TVar('b)), TFun(TVar('c), TVar('c))))
    assertSolution(Map('x -> TFun(TVar('c), TVar('c)), 'f -> TFun(TFun(TVar('c), TVar('c)), TFun(TVar('c), TVar('c))), 'y -> TFun(TVar('c), TVar('c)), 'a -> TVar('c), 'b -> TVar('c), 'z1 -> TVar('c), 'z2 -> TVar('c)))

    remEq(TFun(TVar('y), TVar('x)), TVar('f))
    assertSolution(Map('x -> TFun(TVar('a), TVar('b)), 'f -> TFun(TFun(TVar('a), TVar('b)), TFun(TVar('c), TVar('c))), 'y -> TFun(TVar('c), TVar('c)), 'z1 -> TVar('c), 'z2 -> TVar('c)))

    solver.reset()
  }
}


class RunTest extends TestSolve(new SolveIntern[Constraint](x => x))
