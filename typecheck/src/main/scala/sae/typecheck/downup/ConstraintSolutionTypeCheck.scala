package sae.typecheck.downup

import sae.typecheck.Constraint._
import sae.typecheck.TypeCheck

import sae.typecheck._
import sae.typecheck.TypeStuff._


/**
* Created by seba on 05/11/14.
*/
object ConstraintSolutionTypeCheck extends TypeCheck {

  private var _nextId = 0
  def freshTVar(): TVar = {
    val v = TVar(Symbol("x$" + _nextId))
    _nextId += 1
    v
  }

  def reset() {}

  def typecheckIncremental(e: Exp) = typecheck(e)()

  def typecheck(e: Exp) = {
    _nextId = 0
    () =>
      try {
        val (t, s, unres) = typecheck(e, Map())
        if (unres.isEmpty)
          Left(t.subst(s))
        else
          Right(s"Unresolved constraints $unres, type ${t.subst(s)}")
      } catch {
        case e: UnboundVariable => Right(s"Unbound variable ${e.x} in context ${e.ctx}")
      }
  }

  def typecheck(e: Exp, ctx: TSubst): (Type, TSubst, Unsolvable) = e.kind match {
    case (Num, 0) => (TNum, Map(), Set())
    case (k, 2) if k == Add || k == Mul =>
      val (t1, s1, unres1) = typecheck(e.sub(0), ctx)
      val (t2, s2, unres2) = typecheck(e.sub(1), ctx)
      val subsol = mergeSolutionC((s1, unres1), (s2, unres2))

      val lcons = EqConstraint(TNum, t1)
      val rcons = EqConstraint(TNum, t2)
      val (s, unres) = extendSolution(subsol, Seq(lcons, rcons))

      (TNum, s, unres)
    case (Var, 0) =>
      val x = e.lits(0).asInstanceOf[Symbol]
      ctx.get(x) match {
        case None => throw UnboundVariable(x, ctx)
        case Some(t) => (t, Map(), Set())
      }
    case (App, 2) =>
      val (t1, s1, unres1) = typecheck(e.sub(0), ctx)
      val (t2, s2, unres2) = typecheck(e.sub(1), ctx)
      val subsol = mergeSolutionC((s1, unres1), (s2, unres2))

      val X = freshTVar()
      val fcons = EqConstraint(TFun(t2, X), t1)
      val (s, unres) = extendSolution(subsol, fcons)

      (X.subst(s), s, unres)
    case (Abs, 1) =>
      if (e.lits(0).isInstanceOf[Symbol]) {
        val x = e.lits(0).asInstanceOf[Symbol]
        val X = freshTVar()

        val (t, s, unres) = typecheck(e.sub(0), ctx + (x -> X))
        (TFun(X.subst(s), t), s, unres)
      }
      else if (e.lits(0).isInstanceOf[Seq[Symbol]]) {
        val xs = e.lits(0).asInstanceOf[Seq[Symbol]]
        val Xs = xs map (_ => freshTVar())

        val (t, s, unres) = typecheck(e.sub(0), ctx ++ (xs zip Xs))

        var tfun = t
        for (i <- xs.size-1 to 0 by -1) {
          val X = Xs(i)
          tfun = TFun(X.subst(s), tfun)
        }

        (tfun, s, unres)
      }
      else
        throw new RuntimeException(s"Cannot handle Abs variables ${e.lits(0)}")
    case (If0, 3) =>
      val (t1, s1, unres1) = typecheck(e.sub(0), ctx)
      val (t2, s2, unres2) = typecheck(e.sub(1), ctx)
      val (t3, s3, unres3) = typecheck(e.sub(2), ctx)
      val subsol = mergeSolutionC((s1, unres1), mergeSolutionC((s2, unres2), (s3, unres3)))

      val cond = EqConstraint(TNum, t1)
      val body = EqConstraint(t2, t3)
      val (s, unres) = extendSolution(subsol, Seq(cond, body))

      (t2.subst(s), s, unres)
    case (Fix, 1) =>
      val (t, s1, unres1) = typecheck(e.sub(0), ctx)

      val X = freshTVar()
      val fixCons = EqConstraint(t, TFun(X, X))
      val (s, unres) = extendSolution((s1, unres1), fixCons)

      (X.subst(s), s, unres)
  }

  def main(args: Array[String]): Unit = {
    printTypecheck(Add(Num(17), Add(Num(10), Num(2))))
    printTypecheck(Add(Num(17), Add(Num(10), Num(5))))
    printTypecheck(Abs('x, Add(Num(10), Num(2))))
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
