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
  abstract class Requirement
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
