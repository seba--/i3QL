package sae.typecheck

import idb.{Relation, MaterializedView}
import idb.observer.Observer
import idb.syntax.iql.IR.Rep
import idb.syntax.iql.SELECT
import sae.typecheck.ConstraintSolutionTypecheck.EqConstraint

/**
 * Created by seba on 27/10/14.
 */
import Exp._
import Type._

object Constraint {
  type Unsolvable = Seq[Constraint]
  type Solution = (TSubst, Unsolvable)

  abstract class Constraint {
    def solve: Option[Map[Symbol, Type]]
    def rename(ren: Map[Symbol, Symbol]): Constraint
  }

  abstract class Requirement {
    def rename(ren: Map[Symbol, Symbol]): Requirement
    def merge(r: Requirement): Option[(Seq[Constraint], Seq[Requirement])]
  }
  def mergeReqs(reqs1: Seq[Requirement], reqs2: Seq[Requirement]) = {
    var mcons = Seq[Constraint]()
    var mreqs = reqs1
    for (r2 <- reqs2)
      mergeReq(mreqs, r2) match {
        case Some((newcons, newreqs)) => mcons = newcons; mreqs = newreqs
        case None => mreqs = r2 +: mreqs
      }
    (mcons, mreqs)
  }

  def mergeReq(reqs1: Seq[Requirement], r2: Requirement): Option[(Seq[Constraint], Seq[Requirement])] = {
    for (r1 <- reqs1)
      r1 merge r2 match {
        case Some((newcons, newreqs)) => return Some((newcons, newreqs ++ (reqs1 diff Seq(r1))))
        case _ => {}
      }
    None
  }

  def mergeFresh(free1: Seq[Symbol], free2: Seq[Symbol]) = {
    var allfree = free1 ++ free2
    var mfree = free1
    var ren = Map[Symbol, Symbol]()
    for (x <- free2)
      if (free1.contains(x)) {
        val newx = tick(x, allfree)
        allfree = newx +: allfree
        mfree = newx +: mfree
        ren += x -> newx
      }
    (mfree, ren)
  }

  def mergeSolution(sol1: Solution, sol2: Solution): Solution = {
    val s1 = sol1._1
    val s2 = sol2._1

    var s = s1 mapValues (_.subst(s2))
    var unres: Unsolvable = sol1._2 ++ sol2._2

    for ((x,_t2) <- s2) {
      val t2 = _t2.subst(s1)
      s.get(x) match {
        case None => s += x -> t2
        case Some(t1) => t1.unify(t2) match {
          case None => unres = EqConstraint(t1, t2) +: unres
          case Some(u) => s = s.mapValues(_.subst(u)) ++ u
        }
      }
    }

    (s, unres)
  }

  def extendSolution(sol: Solution, cs: Iterable[Constraint]): (TSubst, Seq[Constraint]) =
    cs.foldLeft(sol)(extendSolution)

  def extendSolution(sol: Solution, c: Constraint): (TSubst, Seq[Constraint]) = {
    c.solve match {
      case None => mergeSolution(sol, (Map(), Seq(c)))
      case Some(u) =>
//        println(s"Extend solution with $c: $u +: $sol")
        val res = mergeSolution(sol, (u, Seq()))
//        println(s"  => $res")
        res
    }
  }

  val name = """([^\d]+)_(\d+)""".r
  def tick(x: Symbol, avoid: Seq[Symbol]): Symbol = {
    val x2 = x.name match {
      case name(s, i) => Symbol(s + "_" + (i.toInt + 1))
      case s => Symbol(s + "_" + 0)
    }
    if (avoid.contains(x2))
      tick(x2, avoid)
    else
      x2
  }

  def rename(ren: Map[Symbol, Symbol])(p: (Type, Seq[Constraint], Seq[Requirement])) =
    (p._1.rename(ren), p._2.map(_.rename(ren)), p._3.map(_.rename(ren)))

  def renameSolution(ren: Map[Symbol, Symbol])(p: (Type, Solution, Seq[Requirement])): (Type, Solution, Seq[Requirement]) =
    (p._1.rename(ren), renameSolution(ren, p._2), p._3.map(_.rename(ren)))

  def renameSolution(ren: Map[Symbol, Symbol], sol: Solution): Solution =
    (sol._1.map(kv => ren.getOrElse(kv._1, kv._1) -> kv._2.rename(ren)), sol._2.map(_.rename(ren)))


  type ConstraintTuple = (ExpKey, ConstraintData)
  type ConstraintData = (Type, Seq[Constraint], Seq[Requirement], Seq[Symbol])

  type ConstraintIncTuple = (ExpKey, ConstraintIncData)
  type ConstraintIncData = (Type, Seq[Constraint], Seq[Requirement])

  type FreshTuple = (ExpKey, FreshData)
  type FreshData = (Seq[Symbol], // fresh variables requested in all of subtree
                    Seq[Map[Symbol, Symbol]]) // renaming for subtrees skipping first one (n-ary op => Seq.length == max(0, n - 1))

  type ConstraintSolutionTuple = (ExpKey, ConstraintSolutionData)
  type ConstraintSolutionData = (Type, Solution, Seq[Requirement], Seq[Symbol])

  def cid(c: Rep[ConstraintTuple]) = c._1
  def cdata(c: Rep[ConstraintTuple]) = c._2

  def ctype(c: Rep[ConstraintTuple]) = c._2._1
  def cons(c: Rep[ConstraintTuple]) = c._2._2
  def reqs(c: Rep[ConstraintTuple]) = c._2._3

  def ctype(c: ConstraintData) = c._1
  def cons(c: ConstraintData) = c._2
  def reqs(c: ConstraintData) = c._3
}
