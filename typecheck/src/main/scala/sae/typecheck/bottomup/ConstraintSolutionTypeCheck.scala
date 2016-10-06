package sae.typecheck.bottomup;

import idb.algebra.print.RelationalAlgebraPrintPlan
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
object ConstraintSolutionTypeCheck extends TypeCheck {

  def typecheckStepRep: Rep[((ExpKey, ExpKind, Seq[Lit], Seq[ConstraintSolutionData])) => ConstraintSolutionData] = staticData (
    (p: (ExpKey, ExpKind, Seq[Lit], Seq[ConstraintSolutionData])) => {
//      Predef.println(p._1)
      val start = System.nanoTime()
//      Predef.println(s"Table change -> TypecheckStep = ${(start - Exp.change)/1000000.0}ms")

      val d = typecheckStep(p._2, p._3, p._4)
      val end = System.nanoTime()

      val usedVars = d._1.vars ++ solutionVars(d._2) ++ requirementVars(d._3)
      val freshVars = d._4

      if (!usedVars.forall(freshVars.contains(_))) {
        Predef.println(s"$usedVars not in $freshVars")
        Predef.println(s"${p._1}  -> type vars\t\t${d._1.vars}")
        Predef.println(s"   -> solution vars\t${solutionVars(d._2)}")
        Predef.println(s"   -> requires vars\t${requirementVars(d._3)}")
        Predef.println(s"   -> fresh vars\t${d._4.toSet}")
      }
//      Predef.println(s"TypecheckStep completed ${p._1}->$d in ${(end-start)/1000000.0}ms")
      d
    }
  )

  def typecheckStep(e: ExpKind, lits: Seq[Lit], sub: Seq[ConstraintSolutionData]): ConstraintSolutionData = {
    import scala.collection.immutable._
    e match {
      case Num => (TNum, (Map(), Set()), Set(), Set())
      case k if k == Add || k == Mul =>
        val (t1, sol1, reqs1, free1) = sub(0)
        val (_t2, _sol2, _reqs2, free2) = sub(1)
        val (mfresh, ren) = mergeFresh(free1, free2)
        val (t2, sol2, reqs2) = renameSolution(ren)(_t2, _sol2, _reqs2)

        val lcons = EqConstraint(TNum, t1)
        val rcons = EqConstraint(TNum, t2)
        val (mcons, mreqs) = mergeReqs(reqs1, reqs2)

        val sol12 = mergeSolution(sol1, sol2)
        val sol = extendSolution(sol12, mcons + lcons + rcons)
        (TNum, sol, mreqs, mfresh)
      case Var =>
        val x = lits(0).asInstanceOf[Symbol]
        val X = TVar(Symbol("X$" + x.name))
        (X, (Map(), Set()), Set(VarRequirement(x, X)), Set(X.x))
      case App =>
        val (t1, sol1, reqs1, fresh1) = sub(0)
        val (_t2, _sol2, _reqs2, fresh2) = sub(1)
        val (mfresh, ren) = mergeFresh(fresh1, fresh2)
        val (t2, sol2, reqs2) = renameSolution(ren)(_t2, _sol2, _reqs2)

        val X = TVar(tick('X$App, mfresh))
        val fcons = EqConstraint(TFun(t2, X), t1)
        val (mcons, mreqs) = mergeReqs(reqs1, reqs2)

        val sol12 = mergeSolution(sol1, sol2)
        val sol = extendSolution(sol12, mcons + fcons)

        (X.subst(sol._1), sol, mreqs, mfresh + X.x)
      case Abs =>
        val x = lits(0).asInstanceOf[Symbol]
        val (t, sol, reqs, fresh) = sub(0)

        val X = TVar(tick('X$Abs, fresh))
        val ((otherReqs, xcons), time) = Util.timed {
          val (xreqs, otherReqs) = reqs.partition { case VarRequirement(`x`, _) => true; case _ => false}
          val xcons = xreqs map { case VarRequirement(_, t) => EqConstraint(X, t.subst(sol._1))}
          (otherReqs, xcons)
        }
        Constraint.computeReqsTime += time
        val fsol = extendSolution(sol, xcons)
        (TFun(X, t).subst(fsol._1), fsol, otherReqs, fresh + X.x)
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
        val sol = extendSolution(sol123, mcons12 ++ mcons23 + cond + body)

        (t2.subst(sol._1), sol, mreqs123, mfresh123)

      case Fix =>
        val (t, sol, reqs, fresh) = sub(0)
        val X = TVar(tick('X$Fix, fresh))
        val fixCons = EqConstraint(t, TFun(X, X))
        val fsol = extendSolution(sol, Set(fixCons))
        (X.subst(fsol._1), fsol, reqs, fresh + X.x)

      case Root.Root =>
        if (sub.isEmpty)
          (TVar('Uninitialized), (Map(), Set(EqConstraint(TNum, TFun(TNum, TNum)))), Set(), Set('Uninitialized))
        else {
          val (t, sol, reqs, free) = sub(0)
          (Root.TRoot(t), sol, reqs, free)
        }
    }
  }


//  val constraints = WITH.RECURSIVE[ConstraintSolutionTuple] (constraints =>
//      (SELECT ((e: Rep[ExpTuple]) => id(e) -> typecheckStepRep ((id(e), kind(e), lits(e), Seq())))
//       FROM Exp.table // 0-ary
//       WHERE (e => subseq(e).length == 0))
//    UNION ALL (
//      (SELECT ((e: Rep[ExpTuple], t1: Rep[ConstraintSolutionTuple]) => id(e) -> typecheckStepRep ((id(e), kind(e), lits(e), Seq(t1._2))))
//       FROM (Exp.table, constraints) // 1-ary
//       WHERE ((e,t1) => subseq(e).length == 1
//                    AND subseq(e)(0) == t1._1))
//    UNION ALL
//      (SELECT ((et1: Rep[(ExpTuple, ConstraintSolutionTuple)], t2: Rep[ConstraintSolutionTuple]) => {
//                 val e = et1._1
//                 val t1 = et1._2
//                 id(e) -> typecheckStepRep ((id(e), kind(e), lits(e), Seq(t1._2, t2._2)))
//               })
//          FROM (SELECT ((e: Rep[ExpTuple], t1: Rep[ConstraintSolutionTuple]) => (e, t1))
//                FROM (Exp.table, constraints)
//                WHERE ((e, t1) => subseq(e).length == 2 AND subseq(e)(0) == t1._1),
//                constraints) // 2-ary
//          WHERE ((et1,t2) => subseq(et1._1)(1) == t2._1))
//          UNION ALL
//            (SELECT ((et1t2: Rep[(ExpTuple,ConstraintSolutionTuple,ConstraintSolutionTuple)], t3: Rep[ConstraintSolutionTuple]) => {
//                       val e = et1t2._1
//                       val t1 = et1t2._2
//                       val t2 = et1t2._3
//                       id(e) -> typecheckStepRep ((id(e), kind(e), lits(e), Seq(t1._2, t2._2, t3._2)))
//                     })
//            FROM (SELECT ((et1: Rep[(ExpTuple, ConstraintSolutionTuple)], t2: Rep[ConstraintSolutionTuple]) => (et1._1, et1._2, t2))
//                  FROM(SELECT ((e: Rep[ExpTuple], t1: Rep[ConstraintSolutionTuple]) => (e, t1))
//                       FROM (Exp.table, constraints)
//                       WHERE ((e, t1) => subseq(e).length == 3 AND subseq(e)(0) == t1._1),
//                       constraints)
//                  WHERE  ((et1, t2) => subseq(et1._1)(1) == t2._1),
//                  constraints) // 3-ary
//             WHERE ((et1t2,t3) => subseq(et1t2._1)(2) == t3._1))
//
//    )
//  )


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

  val root = Root(constraints, staticData (rootTypeExtractor))
  def typecheck(e: Exp) = {
    val fire = root.set(e)
    () => {fire(); root.Type}
  }

  def typecheckIncremental(e: Exp) = {
    root.update(e)
    root.Type
  }

  def reset() {root.reset()}

  def printQuery(file: String): Unit = {
    val printer = new RelationalAlgebraPrintPlan {
      override val IR = idb.syntax.iql.IR
    }

    val s = printer.quoteRelation(constraints)
    scala.tools.nsc.io.File(file).writeAll(s)
  }

  def main(args: Array[String]): Unit = {
    printQuery("constraints.query")

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
