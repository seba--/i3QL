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
  abstract class Constraint

  abstract class Requirement {
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

  type ConstraintTuple = (ExpKey, ConstraintData)
  type ConstraintData = (Type, Seq[Constraint], Seq[Requirement])

  def cid(c: Rep[ConstraintTuple]) = c._1
  def cdata(c: Rep[ConstraintTuple]) = c._2

  def ctype(c: Rep[ConstraintTuple]) = c._2._1
  def cons(c: Rep[ConstraintTuple]) = c._2._2
  def reqs(c: Rep[ConstraintTuple]) = c._2._3

  def ctype(c: ConstraintData) = c._1
  def cons(c: ConstraintData) = c._2
  def reqs(c: ConstraintData) = c._3
}
