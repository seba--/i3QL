package sae.typecheck.downup

import sae.typecheck.TypeCheck

import sae.typecheck._
import sae.typecheck.TypeStuff._


/**
 * Created by seba on 05/11/14.
 */
object ConstraintTypeCheck extends TypeCheck {

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
        val (t, cons) = typecheck(e, Map())
        val (s, unres) = Constraint.solve(cons)
        if (unres.isEmpty)
          Left(t.subst(s))
        else
          Right(s"Unresolved constraints $unres, type ${t.subst(s)}")
      } catch {
        case e: UnboundVariable => Right(s"Unbound variable ${e.x} in context ${e.ctx}")
      }
  }

  def typecheck(e: Exp, ctx: TSubst): (Type, Set[Constraint]) = e.kind match {
    case Num => (TNum, Set())
    case k if k == Add || k == Mul =>
      val (t1, cons1) = typecheck(e.sub(0), ctx)
      val (t2, cons2) = typecheck(e.sub(1), ctx)
      val lcons = EqConstraint(TNum, t1)
      val rcons = EqConstraint(TNum, t2)
      (TNum, cons1 ++ cons2 + lcons + rcons)
    case Var =>
      val x = e.lits(0).asInstanceOf[Symbol]
      ctx.get(x) match {
        case None => throw UnboundVariable(x, ctx)
        case Some(t) => (t, Set())
      }
    case App =>
      val (t1, cons1) = typecheck(e.sub(0), ctx)
      val (t2, cons2) = typecheck(e.sub(1), ctx)

      val X = freshTVar()
      val fcons = EqConstraint(TFun(t2, X), t1)
      (X, cons1 ++ cons2 + fcons)
    case Abs =>
      val x = e.lits(0).asInstanceOf[Symbol]
      val X = freshTVar()

      val (t, cons) = typecheck(e.sub(0), ctx + (x -> X))
      (TFun(X, t), cons)
    case If0 =>
      val (t1, cons1) = typecheck(e.sub(0), ctx)
      val (t2, cons2) = typecheck(e.sub(1), ctx)
      val (t3, cons3) = typecheck(e.sub(2), ctx)

      val cond = EqConstraint(TNum, t1)
      val body = EqConstraint(t2, t3)

      (t2, cons1 ++ cons2 ++ cons3 + cond + body)
    case Fix =>
      val (t, cons) = typecheck(e.sub(0), ctx)
      val X = freshTVar()
      val fixCons = EqConstraint(t, TFun(X, X))
      (X, cons + fixCons)
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

case class UnboundVariable(x: Symbol, ctx: TSubst) extends RuntimeException
