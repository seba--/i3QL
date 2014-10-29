package sae.typecheck

import idb.{Relation, MaterializedView}
import idb.observer.Observer
import idb.syntax.iql.IR.Rep
import idb.syntax.iql.SELECT

/**
 * Created by seba on 27/10/14.
 */
import Exp._
import Type._

object Constraint {
  abstract class Constraint {
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

  def mergeFree(free1: Set[Symbol], free2: Set[Symbol]) = {
    var allfree = free1 ++ free2
    var mfree = free1
    var ren = Map[Symbol, Symbol]()
    for (x <- free2)
      if (free1.contains(x)) {
        val newx = tick(x, allfree)
        allfree += newx
        mfree += newx
        ren += x -> newx
      }
    (mfree, ren)
  }

  val name = """([^\d]+)_(\d+)""".r
  def tick(x: Symbol, avoid: Set[Symbol]): Symbol = {
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

  type ConstraintTuple = (ExpKey, ConstraintData)
  type ConstraintData = (Type, Seq[Constraint], Seq[Requirement], Set[Symbol])

  type FreshTuple = (ExpKey, FreshData)
  type FreshData = (Set[Symbol], Map[Symbol, Symbol])

  def cid(c: Rep[ConstraintTuple]) = c._1
  def cdata(c: Rep[ConstraintTuple]) = c._2

  def ctype(c: Rep[ConstraintTuple]) = c._2._1
  def cons(c: Rep[ConstraintTuple]) = c._2._2
  def reqs(c: Rep[ConstraintTuple]) = c._2._3

  def ctype(c: ConstraintData) = c._1
  def cons(c: ConstraintData) = c._2
  def reqs(c: ConstraintData) = c._3
}
