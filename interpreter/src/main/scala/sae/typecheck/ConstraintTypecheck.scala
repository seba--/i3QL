package sae.typecheck

import idb.syntax.iql._
import idb.syntax.iql.IR._

import sae.typecheck.Exp._
import sae.typecheck.Type._
import sae.typecheck.Constraint._

/**
* Created by seba on 26/10/14.
*/
object ConstraintTypecheck {

  case object Num extends ExpKind
  case object Add extends ExpKind
  case object Var extends ExpKind
  case object Abs extends ExpKind
  case object App extends ExpKind

  case object TNum extends Type {
    def rename(ren: Map[Symbol, Symbol]) = this
  }
  case class TVar(x: Symbol) extends Type {
    def rename(ren: Map[Symbol, Symbol]) = TVar(ren.getOrElse(x, x))
  }
  case class TFun(t1: Type, t2: Type) extends Type {
    def rename(ren: Map[Symbol, Symbol]) = TFun(t1.rename(ren), t2.rename(ren))
  }

  case class EqConstraint(expected: Type, actual: Type) extends Constraint {
    def rename(ren: Map[Symbol, Symbol]) = EqConstraint(expected.rename(ren), actual.rename(ren))
  }

  case class VarRequirement(x: Symbol, t: Type) extends Requirement {
    def merge(r: Requirement) = r match {
      case VarRequirement(`x`, t2) => scala.Some((scala.Seq(EqConstraint(t, t2)), scala.Seq(this)))
      case _ => None
    }

    def rename(ren: Map[Symbol, Symbol]) = VarRequirement(ren.getOrElse(x, x), t.rename(ren))
  }

  def typecheckStepRep: Rep[((ExpKind, Seq[Lit], Seq[ConstraintData])) => ConstraintData] = staticData (
    (p: (ExpKind, Seq[Lit], Seq[ConstraintData])) => typecheckStep(p._1, p._2, p._3)
  )

  var _tvarid = 0
  def nextTVar() = {
    val x = TVar(Symbol("X" + _tvarid))
    _tvarid += 1
    x
  }

  def typecheckStep(e: ExpKind, lits: Seq[Lit], sub: Seq[ConstraintData]): ConstraintData = {
    import scala.collection._
    import Predef.Set
    e match {
      case Num => (TNum, Seq(), Seq(), Set())
      case Add =>
        val (t1, cons1, reqs1, free1) = sub(0)
        val (_t2, _cons2, _reqs2, free2) = sub(1)
        val (mfree, ren) = mergeFree(free1, free2)
        val (t2, cons2, reqs2) = rename(ren)(_t2, _cons2, _reqs2)

        val lcons = EqConstraint(TNum, t1)
        val rcons = EqConstraint(TNum, t2)
        val (mcons, mreqs) = mergeReqs(reqs1, reqs2)
        (TNum, lcons +: rcons +: (cons1 ++ cons2 ++ mcons), mreqs, mfree)
      case Var =>
        val x = lits(0).asInstanceOf[Symbol]
        val X = TVar(Symbol("X$" + x.name))
        (X, Seq(), Seq(VarRequirement(x, X)), Set(X.x))
      case App =>
        val (t1, cons1, reqs1, free1) = sub(0)
        val (_t2, _cons2, _reqs2, free2) = sub(1)
        val (mfree, ren) = mergeFree(free1, free2)
        val (t2, cons2, reqs2) = rename(ren)(_t2, _cons2, _reqs2)

        val X = TVar(tick('X$App, mfree))
        val fcons = EqConstraint(TFun(t2, X), t1)
        val (mcons, mreqs) = mergeReqs(reqs1, reqs2)
        (X, fcons +: (cons1 ++ cons2 ++ mcons), mreqs, mfree + X.x)
      case Abs =>
        val x = lits(0).asInstanceOf[Symbol]
        val (t, cons, reqs, free) = sub(0)

        val X = TVar(tick('X$App, free))
        val (xreqs, otherReqs) = reqs.partition{case VarRequirement(`x`, _) => true; case _ => false}
        val xcons = xreqs map {case VarRequirement(_, t) => EqConstraint(X, t)}
        (TFun(X, t), cons ++ xcons, otherReqs, free + X.x)
      case Root.Root =>
        if (sub.isEmpty)
          (TVar('Uninitialized), Seq(EqConstraint(TNum, TFun(TNum, TNum))), Seq(), Set())
        else {
          val (t, cons, reqs, free) = sub(0)
          (Root.TRoot(t), cons, reqs, free)
        }
    }
  }

  val types = WITH.RECURSIVE[ConstraintTuple] (types =>
      (SELECT ((e: Rep[ExpTuple]) => id(e) -> typecheckStepRep ((kind(e), lits(e), Seq())))
       FROM Exp.table // 0-ary
       WHERE (e => subseq(e).length == 0))
    UNION ALL (
      (SELECT ((e: Rep[ExpTuple], t1: Rep[ConstraintTuple]) => id(e) -> typecheckStepRep ((kind(e), lits(e), Seq(cdata(t1)))))
       FROM (Exp.table, types) // 1-ary
       WHERE ((e,t1) => subseq(e).length == 1
                    AND subseq(e)(0) == cid(t1)))
    UNION ALL
      (SELECT ((e: Rep[ExpTuple], t1: Rep[ConstraintTuple], t2: Rep[ConstraintTuple]) => id(e) -> typecheckStepRep ((kind(e), lits(e), Seq(cdata(t1), cdata(t2)))))
       FROM (Exp.table, types, types) // 2-ary
       WHERE ((e,t1,t2) => subseq(e).length == 2
                       AND subseq(e)(0) == cid(t1)
                       AND subseq(e)(1) == cid(t2)))
    )
  )

  def main(args: Array[String]): Unit = {
    val resultTypes = types.asMaterialized
    val root = Root(types)

    val e = Add(Var('x), Var('x))
    root.set(e)
    Predef.println(s"Type of $e is ${root.Type}")

    val e2 = Add(Num(17), Add(Num(10), Num(2)))
    root.set(e2)
    Predef.println(s"Type of $e2 is ${root.Type}")

    val e3 = Add(Add(Num(17), Num(1)), Add(Num(10), Num(2)))
    root.set(e3)
    Predef.println(s"Type of $e3 is ${root.Type}")

    val e4 = Add(Add(Num(17), Num(1)), Add(Num(17), Num(1)))
    root.set(e4)
    Predef.println(s"Type of $e4 is ${root.Type}")

    val e5 = Num(30)
    root.set(e5)
    Predef.println(s"Type of $e5 is ${root.Type}")

    val e6 = Var('x)
    root.set(e6)
    Predef.println(s"Type of $e6 is ${root.Type}")

    val e7 = Abs(scala.Seq('y), scala.Seq(Var('y)))
    root.set(e7)
    Predef.println(s"Type of $e7 is ${root.Type}")
  }

}
