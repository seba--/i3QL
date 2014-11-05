package sae.typecheck

import idb.syntax.iql._
import idb.syntax.iql.IR._

import Exp._
import TypeStuff._
import Constraint._

/**
* Created by seba on 26/10/14.
*/
object ConstraintSolutionTypecheck {

  case object Num extends ExpKind
  case object Add extends ExpKind
  case object Mul extends ExpKind
  case object Var extends ExpKind
  case object Abs extends ExpKind
  case object App extends ExpKind
  case object If0 extends ExpKind
  case object Fix extends ExpKind


  case class EqConstraint(expected: Type, actual: Type) extends Constraint {
    def rename(ren: Map[Symbol, Symbol]) = EqConstraint(expected.rename(ren), actual.rename(ren))
    def solve = expected.unify(actual)
    def vars = expected.vars ++ actual.vars
  }

  case class VarRequirement(x: Symbol, t: Type) extends Requirement {
    def merge(r: Requirement) = r match {
      case VarRequirement(`x`, t2) => scala.Some((scala.Seq(EqConstraint(t, t2)), scala.Seq(this)))
      case _ => None
    }
    def rename(ren: Map[Symbol, Symbol]) = VarRequirement(ren.getOrElse(x, x), t.rename(ren))
    def vars = t.vars
  }

  def typecheckStepRep: Rep[((ExpKey, ExpKind, Seq[Lit], Seq[ConstraintSolutionData])) => ConstraintSolutionData] = staticData (
    (p: (ExpKey, ExpKind, Seq[Lit], Seq[ConstraintSolutionData])) => {
//      Predef.println(p._1)
      val d = typecheckStep(p._2, p._3, p._4)

      val usedVars = d._1.vars ++ solutionVars(d._2) ++ requirementVars(d._3)
      val freshVars = d._4

      if (!usedVars.forall(freshVars.contains(_))) {
        Predef.println(s"$usedVars not in $freshVars")
        Predef.println(s"${p._1}  -> type vars\t\t${d._1.vars}")
        Predef.println(s"   -> solution vars\t${solutionVars(d._2)}")
        Predef.println(s"   -> requires vars\t${requirementVars(d._3)}")
        Predef.println(s"   -> fresh vars\t${d._4.toSet}")
      }
//      Predef.println(s"${p._1} -> $d")
      d
    }
  )

  def typecheckStep(e: ExpKind, lits: Seq[Lit], sub: Seq[ConstraintSolutionData]): ConstraintSolutionData = {
    import scala.collection.immutable._
    e match {
      case Num => (TNum, (Map(), Seq()), Seq(), Seq())
      case k if k == Add || k == Mul =>
        val (t1, sol1, reqs1, free1) = sub(0)
        val (_t2, _sol2, _reqs2, free2) = sub(1)
        val (mfresh, ren) = mergeFresh(free1, free2)
        val (t2, sol2, reqs2) = renameSolution(ren)(_t2, _sol2, _reqs2)

        val lcons = EqConstraint(TNum, t1)
        val rcons = EqConstraint(TNum, t2)
        val (mcons, mreqs) = mergeReqs(reqs1, reqs2)

        val sol12 = mergeSolution(sol1, sol2)
        val sol = extendSolution(sol12, lcons +: rcons +: mcons)
        (TNum, sol, mreqs, mfresh)
      case Var =>
        val x = lits(0).asInstanceOf[Symbol]
        val X = TVar(Symbol("X$" + x.name))
        (X, (Map(), Seq()), Seq(VarRequirement(x, X)), Seq(X.x))
      case App =>
        val (t1, sol1, reqs1, fresh1) = sub(0)
        val (_t2, _sol2, _reqs2, fresh2) = sub(1)
        val (mfresh, ren) = mergeFresh(fresh1, fresh2)
        val (t2, sol2, reqs2) = renameSolution(ren)(_t2, _sol2, _reqs2)

        val X = TVar(tick('X$App, mfresh))
        val fcons = EqConstraint(TFun(t2, X), t1)
        val (mcons, mreqs) = mergeReqs(reqs1, reqs2)

        val sol12 = mergeSolution(sol1, sol2)
        val sol = extendSolution(sol12, fcons +: mcons)

        (X.subst(sol._1), sol, mreqs, X.x +: mfresh)
      case Abs =>
        val x = lits(0).asInstanceOf[Symbol]
        val (t, sol, reqs, fresh) = sub(0)

        val X = TVar(tick('X$Abs, fresh))
        val (xreqs, otherReqs) = reqs.partition{case VarRequirement(`x`, _) => true; case _ => false}
        val xcons = xreqs map {case VarRequirement(_, t) => EqConstraint(X, t)}
        val fsol = extendSolution(sol, xcons)
        (TFun(X, t).subst(fsol._1), fsol, otherReqs, X.x +: fresh)
      case If0 =>
        val (t1, sol1, reqs1, fresh1) = sub(0)
        val (_t2, _sol2, _reqs2, fresh2) = sub(1)
        val (_t3, _sol3, _reqs3, fresh3) = sub(2)
        val (mfresh12, ren12) = mergeFresh(fresh1, fresh2)
        val (t2, sol2, reqs2) = renameSolution(ren12)(_t2, _sol2, _reqs2)
        val (mfresh123, ren23) = mergeFresh(mfresh12, fresh3)
        val (t3, sol3, reqs3) = renameSolution(ren23)(_t3, _sol3, _reqs3)

        val (mcons12, mreqs12) = mergeReqs(reqs1, reqs2)
        val (mcons23, mreqs123) = mergeReqs(mreqs12, reqs3)

        val cond = EqConstraint(TNum, t1)
        val body = EqConstraint(t2, t3)

        val sol123 = mergeSolution(sol1, mergeSolution(sol2, sol3))
        val sol = extendSolution(sol123, cond +: body +: (mcons12 ++ mcons23))

        (t2.subst(sol._1), sol, mreqs123, mfresh123)

      case Fix =>
        val (t, sol, reqs, fresh) = sub(0)
        val X = TVar(tick('X$Fix, fresh))
        val fixCons = EqConstraint(t, TFun(X, X))
        val fsol = extendSolution(sol, Seq(fixCons))
        (X.subst(fsol._1), fsol, reqs, X.x +: fresh)

      case Root.Root =>
        if (sub.isEmpty)
          (TVar('Uninitialized), (Map(), Seq(EqConstraint(TNum, TFun(TNum, TNum)))), Seq(), Seq('Uninitialized))
        else {
          val (t, sol, reqs, free) = sub(0)
          (Root.TRoot(t), sol, reqs, free)
        }
    }
  }

  val constraints = WITH.RECURSIVE[ConstraintSolutionTuple] (constraints =>
      (SELECT ((e: Rep[ExpTuple]) => id(e) -> typecheckStepRep ((id(e), kind(e), lits(e), Seq())))
       FROM Exp.table // 0-ary
       WHERE (e => subseq(e).length == 0))
    UNION ALL (
      (SELECT ((e: Rep[ExpTuple], t1: Rep[ConstraintSolutionTuple]) => id(e) -> typecheckStepRep ((id(e), kind(e), lits(e), Seq(t1._2))))
       FROM (Exp.table, constraints) // 1-ary
       WHERE ((e,t1) => subseq(e).length == 1
                    AND subseq(e)(0) == t1._1))
    UNION ALL
      (SELECT ((e: Rep[ExpTuple], t1: Rep[ConstraintSolutionTuple], t2: Rep[ConstraintSolutionTuple]) => id(e) -> typecheckStepRep ((id(e), kind(e), lits(e), Seq(t1._2, t2._2))))
       FROM (Exp.table, constraints, constraints) // 2-ary
       WHERE ((e,t1,t2) => subseq(e).length == 2
                       AND subseq(e)(0) == t1._1
                       AND subseq(e)(1) == t2._1))
    UNION ALL
      (SELECT ((e: Rep[ExpTuple], t1: Rep[ConstraintSolutionTuple], t2: Rep[ConstraintSolutionTuple], t3: Rep[ConstraintSolutionTuple]) =>
             id(e) -> typecheckStepRep ((id(e), kind(e), lits(e), Seq(t1._2, t2._2, t3._2))))
       FROM (Exp.table, constraints, constraints, constraints) // 2-ary
       WHERE ((e,t1,t2,t3) => subseq(e).length == 3
                          AND subseq(e)(0) == t1._1
                          AND subseq(e)(1) == t2._1
                          AND subseq(e)(2) == t3._1))

    )
  )

  val solver = Solve[Constraint](x => x)()
  var lastConstraints = scala.Seq[Constraint]()
  val rootTypeExtractor: ConstraintSolutionData => Either[Type, TError] = (x: ConstraintSolutionData) => {
    val (t, sol, reqs, free) = x
    if (!reqs.isEmpty)
      scala.Right(s"Unresolved context requirements $reqs, type $t, solution $sol, free $free")
    else if (sol._2.isEmpty)
      t match {
        case Root.TRoot(t) => scala.Left(t)
        case _ => throw new RuntimeException(s"Unexpected root type $t")
      }
    else
      scala.Right(s"Unresolved constraints ${sol._2}, solution $sol, free $free")
  }

  def main(args: Array[String]): Unit = {
    val resultTypes = constraints.asMaterialized
    val root = Root(constraints, staticData (rootTypeExtractor))

    val e = Add(Num(17), Add(Num(10), Num(2)))
    root.set(e)
    Predef.println(s"Type of $e is ${root.Type}")

    val e2 = Abs('x, Add(Num(10), Num(2)))
    root.set(e2)
    Predef.println(s"Type of $e2 is ${root.Type}")

    val e3 = Abs('x, Add(Var('x), Var('x)))
    root.set(e3)
    Predef.println(s"Type of $e3 is ${root.Type}")

    val e4 = Abs('x, Add(Var('err), Var('x)))
    root.set(e4)
    Predef.println(s"Type of $e4 is ${root.Type}")

    val e5 = Abs('x, Abs('y, App(Var('x), Var('y))))
    root.set(e5)
    Predef.println(s"Type of $e5 is ${root.Type}")

    val e6 = Abs('x, Abs('y, Add(Var('x), Var('y))))
    root.set(e6)
    Predef.println(s"Type of $e6 is ${root.Type}")

    val fac = Fix(Abs('f, Abs('n, If0(Var('n), Num(1), Mul(Var('n), App(Var('f), Add(Var('n), Num(-1))))))))
    root.set(fac)
    Predef.println(s"Type of fac function is ${root.Type}")

    val facapp = Abs('x, App(fac, Var('x)))
    root.set(facapp)
    Predef.println(s"Type of faceta function is ${root.Type}")

    val fib = Fix(Abs('f, Abs('n,
      If0(Var('n), Num(1),
        If0(Add(Var('n), Num(-1)), Num(1),
          Add(App(Var('f), Add(Var('n), Num(-1))),
              App(Var('f), Add(Var('n), Num(-2)))))))))
    root.set(fib)
    Predef.println(s"Type of fibonacci function is ${root.Type}")

    val facfib = Abs('x, Add(App(fac, Var('x)), App(fib, Var('x))))
    root.set(facfib)
    Predef.println(s"Type of facfib function is ${root.Type}")

    val e7 = Abs('y, Var('y))
    root.set(e7)
    Predef.println(s"Type of $e7 is ${root.Type}")
  }

}
