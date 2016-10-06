package sae.typecheck

import idb.operators.NotSelfMaintainableAggregateFunction
import org.scalatest.FunSuite
import sae.typecheck.Constraint.Unsolvable
import TypeStuff._

/**
 * Created by seba on 03/11/14.
 */
class TestConstraintSolver(solver: NotSelfMaintainableAggregateFunction[Constraint, ()=>(TSubst, Unsolvable)] with Resetable) extends FunSuite {

  def addEq(t1: Type, t2: Type) = solver.add(EqConstraint(t1, t2), Seq())
  def remEq(t1: Type, t2: Type) = solver.remove(EqConstraint(t1, t2), Seq())

  def assertSolution(s: TSubst) = {
    val (was, unres) = solver.get()
    assert(unres.isEmpty, s"Expected solution $s but found unresolvable constraints $unres")

    lazy val diff = (for (k <- was.keys.toSet intersect s.keys.toSet if was(k) != s(k)) yield (k -> (was(k), s(k)))).toMap
    lazy val msg = s"Superfluous bindings ${was.keys.toSeq diff s.keys.toSeq}, missing bindings ${s.keys.toSeq diff was.keys.toSeq}, differing bindings $diff"
    assertResult(s, msg)(was)
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

    remEq(TString, TVar('x))
    assertSolution(Map('y -> TString))

    addEq(TNum, TVar('x))
    assertSolution(Map('y -> TString, 'x -> TNum))

    solver.reset()
  }

  test ("transitive constraints") {
    addEq(TFun(TVar('x), TVar('y)), TVar('f))
    addEq(TFun(TVar('y), TVar('x)), TVar('f))
    assertSolution(Map('x -> TVar('y), 'f -> TFun(TVar('y), TVar('y))))

    addEq(TVar('y), TFun(TVar('z1), TVar('z2)))
    assertSolution(Map('x -> TFun(TVar('z1), TVar('z2)), 'f -> TFun(TFun(TVar('z1), TVar('z2)), TFun(TVar('z1), TVar('z2))), 'y -> TFun(TVar('z1), TVar('z2))))

    addEq(TVar('f), TFun(TFun(TVar('a), TVar('b)), TFun(TVar('c), TVar('c))))
    assertSolution(Map('x -> TFun(TVar('c), TVar('c)), 'f -> TFun(TFun(TVar('c), TVar('c)), TFun(TVar('c), TVar('c))), 'y -> TFun(TVar('c), TVar('c)), 'a -> TVar('c), 'b -> TVar('c), 'z1 -> TVar('c), 'z2 -> TVar('c)))

    addEq(TVar('a), TNum)
    addEq(TVar('b), TString)
    assertUnsolvable()

    remEq(TFun(TVar('y), TVar('x)), TVar('f))
    assertSolution(Map('x -> TFun(TNum, TString), 'f -> TFun(TFun(TNum, TString), TFun(TVar('z2), TVar('z2))), 'y -> TFun(TVar('z2), TVar('z2)), 'z1 -> TVar('z2), 'c -> TVar('z2), 'a -> TNum, 'b -> TString))

    solver.reset()
  }

  test ("realistic constraints") {
    val fac = Fix(Abs('f, Abs('n, If0(Var('n), Num(1), Mul(Var('n), App(Var('f), Add(Var('n), Num(-1))))))))

  }

  val its = 500
  val removes = its/10
  test (s"scaling constraint solving to $its constraints") {
    assertSolution(Map())
    for (i <- 1 to its by 2) {
      addEq(TFun(TVar(Symbol(s"x_$i")), TVar(Symbol(s"x_${i + 1}"))), TFun(TVar(Symbol(s"x_${i + 2}")), TVar(Symbol(s"x_${i + 3}"))))

      val subst = for (j <- 1 to i+1) yield if(j % 2 == 0) (Symbol(s"x_$j") -> TVar(Symbol(s"x_${i+3}"))) else (Symbol(s"x_$j") -> TVar(Symbol(s"x_${i+2}")))
      assertSolution(subst.toMap)
    }

    var subst = (for (j <- 1 to its) yield if (j % 2 == 0) (Symbol(s"x_$j") -> TVar(Symbol(s"x_${its+2}"))) else (Symbol(s"x_$j") -> TVar(Symbol(s"x_${its+1}")))).toMap
    assertSolution(subst)

    for (i <- removes to its by (its/removes)) {
      remEq(TFun(TVar(Symbol(s"x_$i")), TVar(Symbol(s"x_${i + 1}"))), TFun(TVar(Symbol(s"x_${i + 2}")), TVar(Symbol(s"x_${i + 3}"))))
      assertSolution(subst)
    }

    for (i <- 1 until 5)
      subst = subst + (if (i % 2 == 0) (Symbol(s"x_$i") -> TVar(Symbol(s"x_${4+2}"))) else (Symbol(s"x_$i") -> TVar(Symbol(s"x_${4+1}"))))
    for (i <- 5 to Math.min(10, its)) {
      remEq(TFun(TVar(Symbol(s"x_$i")), TVar(Symbol(s"x_${i + 1}"))), TFun(TVar(Symbol(s"x_${i + 2}")), TVar(Symbol(s"x_${i + 3}"))))
      subst = subst - Symbol(s"x_$i")
    }

    assertSolution(subst)
  }
}


//class RunTest extends TestConstraintSolver(new SolveIntern[Constraint](x => x))
//class RunTest extends TestConstraintSolver(new SolveVarTrackingIntern[Constraint](x => x))