package sae.typecheck.bottomup;

import idb.syntax.iql._
import idb.syntax.iql.IR._

import sae.typecheck._
import sae.typecheck.Exp
import sae.typecheck.Exp._
import sae.typecheck.TypeStuff._
import sae.typecheck.Constraint._
import sae.typecheck.TypeCheck

/**
* Created by seba on 26/10/14.
*/
object ConstraintTypeCheck extends TypeCheck {

  def typecheckStepRep: Rep[((ExpKind, Seq[Lit], Seq[ConstraintData])) => ConstraintData] = staticData (
    (p: (ExpKind, Seq[Lit], Seq[ConstraintData])) => {
      val d = typecheckStep(p._1, p._2, p._3)
//      Predef.println(s"${p._1} -> $d")
      d
    }
  )

  def typecheckStep(e: ExpKind, lits: Seq[Lit], sub: Seq[ConstraintData]): ConstraintData = {
    import scala.collection.immutable._
    e match {
      case Num => (TNum, Set(), Set(), Set())
      case k if k == Add || k == Mul =>
        val (t1, cons1, reqs1, free1) = sub(0)
        val (_t2, _cons2, _reqs2, free2) = sub(1)
        val (mfresh, ren) = mergeFresh(free1, free2)
        val (t2, cons2, reqs2) = rename(ren)(_t2, _cons2, _reqs2)

        val lcons = EqConstraint(TNum, t1)
        val rcons = EqConstraint(TNum, t2)
        val (mcons, mreqs) = mergeReqs(reqs1, reqs2)
        (TNum, cons1 ++ cons2 ++ mcons + lcons + rcons, mreqs, mfresh)
      case Var =>
        val x = lits(0).asInstanceOf[Symbol]
        val X = TVar(Symbol("X$" + x.name))
        (X, Set(), Set(VarRequirement(x, X)), Set(X.x))
      case App =>
        val (t1, cons1, reqs1, fresh1) = sub(0)
        val (_t2, _cons2, _reqs2, fresh2) = sub(1)
        val (mfresh, ren) = mergeFresh(fresh1, fresh2)
        val (t2, cons2, reqs2) = rename(ren)(_t2, _cons2, _reqs2)

        val X = TVar(tick('X$App, mfresh))
        val fcons = EqConstraint(TFun(t2, X), t1)
        val (mcons, mreqs) = mergeReqs(reqs1, reqs2)
        (X, cons1 ++ cons2 ++ mcons + fcons, mreqs, mfresh + X.x)
      case Abs =>
        val x = lits(0).asInstanceOf[Symbol]
        val (t, cons, reqs, fresh) = sub(0)

        val X = TVar(tick('X$Abs, fresh))
        val (xreqs, otherReqs) = reqs.partition{case VarRequirement(`x`, _) => true; case _ => false}
        val xcons = xreqs map {case VarRequirement(_, t) => EqConstraint(X, t)}
        (TFun(X, t), cons ++ xcons, otherReqs, fresh + X.x)
      case If0 =>
        val (t1, cons1, reqs1, fresh1) = sub(0)
        val (_t2, _cons2, _reqs2, fresh2) = sub(1)
        val (_t3, _cons3, _reqs3, fresh3) = sub(2)
        val (mfresh12, ren12) = mergeFresh(fresh1, fresh2)
        val (t2, cons2, reqs2) = rename(ren12)(_t2, _cons2, _reqs2)
        val (mfresh123, ren23) = mergeFresh(mfresh12, fresh3)
        val (t3, cons3, reqs3) = rename(ren23)(_t3, _cons3, _reqs3)

        val (mcons12, mreqs12) = mergeReqs(reqs1, reqs2)
        val (mcons23, mreqs123) = mergeReqs(mreqs12, reqs3)

        val cond = EqConstraint(TNum, t1)
        val body = EqConstraint(t2, t3)

        (t2, mcons12 ++ mcons23 + cond + body, mreqs123, mfresh123)

      case Fix =>
        val (t, cons, reqs, fresh) = sub(0)
        val X = TVar(tick('X$Fix, fresh))
        val fixCons = EqConstraint(t, TFun(X, X))
        (X, cons + fixCons, reqs, fresh + X.x)

      case Root.Root =>
        if (sub.isEmpty)
          (TVar('Uninitialized), Set(EqConstraint(TNum, TFun(TNum, TNum))), Set(), Set())
        else {
          val (t, cons, reqs, free) = sub(0)
          (Root.TRoot(t), cons, reqs, free)
        }
    }
  }

  val constraints = WITH.RECURSIVE[ConstraintTuple] (constraints =>
      (SELECT ((e: Rep[ExpTuple]) => id(e) -> typecheckStepRep ((kind(e), lits(e), Seq())))
       FROM Exp.table // 0-ary
       WHERE (e => subseq(e).length == 0))
    UNION ALL (
      (SELECT ((e: Rep[ExpTuple], t1: Rep[ConstraintTuple]) => id(e) -> typecheckStepRep ((kind(e), lits(e), Seq(cdata(t1)))))
       FROM (Exp.table, constraints) // 1-ary
       WHERE ((e,t1) => subseq(e).length == 1
                    AND subseq(e)(0) == cid(t1)))
    UNION ALL
      (SELECT ((e: Rep[ExpTuple], t1: Rep[ConstraintTuple], t2: Rep[ConstraintTuple]) => id(e) -> typecheckStepRep ((kind(e), lits(e), Seq(cdata(t1), cdata(t2)))))
       FROM (Exp.table, constraints, constraints) // 2-ary
       WHERE ((e,t1,t2) => subseq(e).length == 2
                       AND subseq(e)(0) == cid(t1)
                       AND subseq(e)(1) == cid(t2)))
    UNION ALL
      (SELECT ((e: Rep[ExpTuple], t1: Rep[ConstraintTuple], t2: Rep[ConstraintTuple], t3: Rep[ConstraintTuple]) =>
             id(e) -> typecheckStepRep ((kind(e), lits(e), Seq(cdata(t1), cdata(t2), cdata(t3)))))
       FROM (Exp.table, constraints, constraints, constraints) // 2-ary
       WHERE ((e,t1,t2,t3) => subseq(e).length == 3
                          AND subseq(e)(0) == cid(t1)
                          AND subseq(e)(1) == cid(t2)
                          AND subseq(e)(2) == cid(t3)))

    )
  )

  val solver = Solve[Constraint](x => x)()
  var lastConstraints = Predef.Set[Constraint]()
  val rootTypeExtractor: ConstraintData => Either[Type, TError] = (x: ConstraintData) => {
    val (t, cons, reqs, free) = x
//    Predef.println(s"Solve $x")

    if (!reqs.isEmpty)
      scala.Right(s"Unresolved context requirements $reqs, type $t, constraints $cons, free $free")
    else {
      val addedCons = cons -- lastConstraints
      val removedCons = lastConstraints -- cons
      lastConstraints = cons

      for (c <- removedCons) solver.remove(c, scala.Seq())
      for (c <- addedCons) solver.add(c, scala.Seq())
      val (s, unres) = solver.get()

      if (unres.isEmpty)
        t.subst(s) match {
          case Root.TRoot(t) => scala.Left(t)
          case _ => scala.Right(s"Unexpected root type $t")
        }
      else
        scala.Right(s"Unresolved constraints $unres, type ${t.subst(s)}, free $free")
    }
  }

  val root = Root(constraints, staticData (rootTypeExtractor))
  def typecheck(e: Exp) = {
    val fire = root.set(e)
    () => {fire(); root.Type}
  }

  def typecheckIncremental(e: Exp): Either[Type, TError] = {
    root.update(e)
    root.Type
  }

  def reset() {root.reset()}

  def main(args: Array[String]): Unit = {
    Util.logTime("type check suite non-incrementally") {
      printTypecheck(Add(Num(17), Add(Num(10), Num(2))))
      printTypecheck(Add(Num(17), Add(Num(10), Num(5))))
      printTypecheck(Abs('x, Add(Num(10), Num(5))))
      printTypecheck(Abs('x, Add(Var('x), Var('x))))
      printTypecheck(Abs('x, Add(Var('err), Var('x))))
      printTypecheck(Abs('x, Abs('y, App(Var('x), Var('y)))))
      printTypecheck(Abs('x, Abs('y, Add(Var('x), Var('y)))))

      val fac = Fix(Abs('f, Abs('n, If0(Var('n), Num(1), Mul(Var('n), App(Var('f), Add(Var('n), Num(-1))))))))
      printTypecheck("factorial", fac)
      printTypecheck("eta-expanded factorial", Abs('x, App(fac, Var('x))))

      val fib = Fix(Abs('f, Abs('n,
        If0(Var('n), Num(1),
          If0(Add(Var('n), Num(-1)), Num(1),
            Add(App(Var('f), Add(Var('n), Num(-1))),
              App(Var('f), Add(Var('n), Num(-2)))))))))
      printTypecheck("fibonacci function", fib)
      printTypecheck("factorial+fibonacci", Abs('x, Add(App(fac, Var('x)), App(fib, Var('x)))))
      printTypecheck(Abs('y, Var('y)))
    }

    root.reset()
    Util.logTime("type check suite incrementally") {
      printTypecheck(Add(Num(17), Add(Num(10), Num(2))))
      printTypecheck(Add(Num(17), Add(Num(10), Num(5))))
      printTypecheck(Abs('x, Add(Num(10), Num(5))))
      printTypecheck(Abs('x, Add(Var('x), Var('x))))
      printTypecheck(Abs('x, Add(Var('err), Var('x))))
      printTypecheck(Abs('x, Abs('y, App(Var('x), Var('y)))))
      printTypecheck(Abs('x, Abs('y, Add(Var('x), Var('y)))))

      val fac = Fix(Abs('f, Abs('n, If0(Var('n), Num(1), Mul(Var('n), App(Var('f), Add(Var('n), Num(-1))))))))
      printTypecheck("factorial", fac)
      printTypecheck("eta-expanded factorial", Abs('x, App(fac, Var('x))))

      val fib = Fix(Abs('f, Abs('n,
        If0(Var('n), Num(1),
          If0(Add(Var('n), Num(-1)), Num(1),
            Add(App(Var('f), Add(Var('n), Num(-1))),
              App(Var('f), Add(Var('n), Num(-2)))))))))
      printTypecheck("fibonacci function", fib)
      printTypecheck("factorial+fibonacci", Abs('x, Add(App(fac, Var('x)), App(fib, Var('x)))))
      printTypecheck(Abs('y, Var('y)))
    }
  }

}
