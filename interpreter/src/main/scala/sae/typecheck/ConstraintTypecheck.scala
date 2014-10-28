package sae.typecheck

import idb.observer.Observer
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

  case object TNum extends Type
  case class TVar(x: Symbol) extends Type
  case class TFun(t1: Type, t2: Type) extends Type

  case class EqConstraint(expected: Type, actual: Type) extends Constraint
  case class VarRequirement(x: Symbol) extends Requirement

  def typecheckStepRep: Rep[((ExpKind, Seq[Lit], Seq[ConstraintData])) => ConstraintData] = staticData (
    (p: (ExpKind, Seq[Lit], Seq[ConstraintData])) => typecheckStep(p._1, p._2, p._3)
  )

  var _tvarid = 0
  def nextTVar() = {
    val x = TVar(Symbol("X" + _tvarid))
    _tvarid += 1
    x
  }

  def typecheckStep(e: ExpKind, lits: Seq[Lit], sub: Seq[ConstraintData]): ConstraintData = e match {
    case Num => (TNum, scala.Seq(), scala.Seq())
    case Add =>
      val (t1, cons1, reqs1) = sub(0)
      val (t2, cons2, reqs2) = sub(1)
      val lcons = EqConstraint(TNum, t1)
      val rcons = EqConstraint(TNum, t2)
      (TNum, lcons +: rcons +: (cons1 ++ cons2), reqs1 ++ reqs2)
    case Var =>
      val x = lits(0).asInstanceOf[Symbol]
      val X = nextTVar()
      (X, scala.Seq(), scala.Seq(VarRequirement(x)))
    case App =>
      val (t1, cons1, reqs1) = sub(0)
      val (t2, cons2, reqs2) = sub(1)
      val X = nextTVar()
      val fcons = EqConstraint(TFun(t2, X), t1)
      (X, fcons +: (cons1 ++ cons2), reqs1 ++ reqs2)
    case Abs =>
      val x = lits(0).asInstanceOf[Symbol]
      val X = nextTVar()
      val (t, cons, reqs) = sub(0)
      (TFun(X, t), cons, reqs.filter(_!=x))
    case Root.Root =>
      val (t, cons, reqs) = sub(0)
      (Root.TRoot(t), cons, reqs)
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

    val e = Add(Num(17), Num(12))
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
  }

}
