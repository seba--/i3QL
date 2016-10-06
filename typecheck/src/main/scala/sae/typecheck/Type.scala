package sae.typecheck

import idb.{Relation, MaterializedView}
import idb.observer.Observer
import idb.syntax.iql.IR.Rep
import idb.syntax.iql.SELECT

/**
 * Created by seba on 27/10/14.
 */
import Exp._
object TypeStuff {
  type TError = String
  type TSubst = Map[Symbol, Type]

  type TypeTuple = (ExpKey, Either[Type, TError])
  def tid(t: Rep[TypeTuple]) = t._1
  def isType(t: Rep[TypeTuple]) = t._2.isLeft
  def getType(t: Rep[TypeTuple]) = t._2.leftGet
  def isError(t: Rep[TypeTuple]) = t._2.isRight
  def getError(t: Rep[TypeTuple]) = t._2.rightGet


  def printTyings(e: Exp, types: MaterializedView[TypeTuple]) {
    var typeMap = Map[ExpKey, Either[Type, TError]]()
    types foreach (kv => typeMap += kv)
    printTyings(e, typeMap)
  }

  def printTyings(e: Exp, types: Map[ExpKey, Either[Type, TError]]) {
    e.sub foreach (printTyings(_, types))
    val key = e.key
    types.get(key) match {
      case Some(Left(t)) => Predef.println(s"$key -> $t, $e")
      case Some(Right(msg)) => Predef.println(s"$key -> Error $msg, $e")
      case None => Predef.println(s"$key -> ERROR: not defined")
    }
  }
}
import TypeStuff._

abstract class Type {
  def rename(ren: Map[Symbol, Symbol]): Type
  def subst(s: TSubst): Type
  def unify(other: Type): Option[TSubst]
  def vars: Set[Symbol]
}

case object TNum extends Type {
  def rename(ren: Map[Symbol, Symbol]) = this
  def subst(s: TSubst) = this
  def unify(other: Type) = other match {
    case TNum => scala.Some(Map())
    case TVar(x) => scala.Some(Map(x -> this))
    case _ => None
  }
  def vars = Predef.Set()
}

case object TString extends Type {
  def rename(ren: Map[Symbol, Symbol]) = this
  def subst(s: TSubst) = this
  def unify(other: Type) = other match {
    case TString => scala.Some(Map())
    case TVar(x) => scala.Some(Map(x -> this))
    case _ => None
  }
  def vars = Predef.Set()
}

case class TVar(x: Symbol) extends Type {
  def rename(ren: Map[Symbol, Symbol]) = TVar(ren.getOrElse(x, x))
  def subst(s: Map[Symbol, Type]) = s.getOrElse(x, this)
  def unify(other: Type): Option[TSubst] = if (other == this) scala.Some(Map()) else scala.Some(Map(x -> other))
  def vars = Predef.Set(x)
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
  def vars = t1.vars ++ t2.vars
}
