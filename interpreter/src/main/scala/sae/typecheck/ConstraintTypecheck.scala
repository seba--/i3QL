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
    def subst(s: TSubst) = this
    def unify(other: Type): Option[TSubst] = other match {
      case TNum => scala.Some(Map())
      case TVar(x) => scala.Some(Map(x -> this))
      case _ => None
    }
  }
  case class TVar(x: Symbol) extends Type {
    def rename(ren: Map[Symbol, Symbol]) = TVar(ren.getOrElse(x, x))
    def subst(s: Map[Symbol, Type]) = s.getOrElse(x, this)
    def unify(other: Type): Option[TSubst] = scala.Some(Map(x -> other))
  }
  case class TFun(t1: Type, t2: Type) extends Type {
    def rename(ren: Map[Symbol, Symbol]) = TFun(t1.rename(ren), t2.rename(ren))
    def subst(s: Map[Symbol, Type]) = TFun(t1.subst(s), t2.subst(s))
    def unify(other: Type): Option[TSubst] = other match {
      case TFun(t1_, t2_) =>
        t1.unify(t1_) match {
          case scala.None => None
          case scala.Some(s1) => t2.subst(s1).unify(t2_.subst(s1)) match {
            case scala.None => None
            case scala.Some(s2) => scala.Some(s1.mapValues(_.subst(s2)) ++ s2)
          }
        }
      case TVar(x) => scala.Some(Map(x -> this))
      case _ => None
    }
  }

  case class EqConstraint(expected: Type, actual: Type) extends Constraint {
    def rename(ren: Map[Symbol, Symbol]) = EqConstraint(expected.rename(ren), actual.rename(ren))
    def solve = expected.unify(actual)
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

  def typecheckStep(e: ExpKind, lits: Seq[Lit], sub: Seq[ConstraintData]): ConstraintData = {
    import scala.collection._
    e match {
      case Num => (TNum, Seq(), Seq(), Seq())
      case Add =>
        val (t1, cons1, reqs1, free1) = sub(0)
        val (_t2, _cons2, _reqs2, free2) = sub(1)
        val (mfree, ren) = mergeFresh(free1, free2)
        val (t2, cons2, reqs2) = rename(ren)(_t2, _cons2, _reqs2)

        val lcons = EqConstraint(TNum, t1)
        val rcons = EqConstraint(TNum, t2)
        val (mcons, mreqs) = mergeReqs(reqs1, reqs2)
        (TNum, lcons +: rcons +: (cons1 ++ cons2 ++ mcons), mreqs, mfree)
      case Var =>
        val x = lits(0).asInstanceOf[Symbol]
        val X = TVar(tick(Symbol("X_" + 0), Seq()))
        (X, Seq(), Seq(VarRequirement(x, X)), Seq(X.x))
      case App =>
        val (t1, cons1, reqs1, fresh1) = sub(0)
        val (_t2, _cons2, _reqs2, fresh2) = sub(1)
        val (mfree, ren) = mergeFresh(fresh1, fresh2)
        val (t2, cons2, reqs2) = rename(ren)(_t2, _cons2, _reqs2)

        val X = TVar(tick('X$App, mfree))
        val fcons = EqConstraint(TFun(t2, X), t1)
        val (mcons, mreqs) = mergeReqs(reqs1, reqs2)
        (X, fcons +: (cons1 ++ cons2 ++ mcons), mreqs, X.x +: mfree)
      case Abs =>
        val x = lits(0).asInstanceOf[Symbol]
        val (t, cons, reqs, free) = sub(0)

        val X = TVar(tick('X$Abs, free))
        val (xreqs, otherReqs) = reqs.partition{case VarRequirement(`x`, _) => true; case _ => false}
        val xcons = xreqs map {case VarRequirement(_, t) => EqConstraint(X, t)}
        (TFun(X, t), cons ++ xcons, otherReqs, X.x +: free)
      case Root.Root =>
        if (sub.isEmpty)
          (TVar('Uninitialized), Seq(EqConstraint(TNum, TFun(TNum, TNum))), Seq(), Seq())
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
    )
  )

  var oldConstraints = scala.Seq[Constraint]()
  val solver = Solve[Constraint](x => x)()
  def solveConstraints(cons: Seq[Constraint]): (TSubst, Seq[Constraint]) = {
    for (c <- oldConstraints)
      solver.remove(c, scala.Seq())
    oldConstraints = cons

    for (c <- cons)
      solver.add(c, scala.Seq())
    solver.get
//    var unres = scala.Seq[Constraint]()
//    var res = Map[Symbol, Type]()
//    for (c <- cons) c match {
//      case EqConstraint(t1, t2) => t1.subst(res).unify(t2.subst(res)) match {
//        case scala.Some(s) => res = res.mapValues(_.subst(s)) ++ s
//        case scala.None => unres = cons +: unres
//      }
//    }
//    (res, unres)
  }


  val rootTypeExtractor: ConstraintData => Either[Type, TError] = (x: ConstraintData) => {
    val (t, cons, reqs, free) = x
    if (!reqs.isEmpty)
      scala.Right(s"Unresolved context requirements $reqs, type $t, constraints $cons, free $free")
    else {
      val (s, unres) = solveConstraints(cons)
      if (unres.isEmpty)
        t.subst(s) match {
          case Root.TRoot(t) => scala.Left(t)
          case _ => throw new RuntimeException(s"Unexpected root type $t")
        }
      else
        scala.Right(s"Unresolved constraints $unres, type ${t.subst(s)}, free $free")
    }
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

//    val e6 = Var('x)
//    root.set(e6)
//    Predef.println(s"Type of $e6 is ${root.Type}")
//
//    val e7 = Abs(scala.Seq('y), scala.Seq(Var('y)))
//    root.set(e7)
//    Predef.println(s"Type of $e7 is ${root.Type}")
  }

}
