//package sae.typecheck
//
//import idb.SetTable
//import idb.syntax.iql._
//import idb.syntax.iql.IR._
//
///**
//  * Created by seba on 26/10/14.
//  */
//object ConstraintTypecheck {
//
//  type Lit = Any
//  abstract class ExpKind
//  type ExpKey = Int
//  type ExpTuple = (ExpKey, ExpKind, Seq[ExpKey])
//  val exps = SetTable.empty[ExpTuple]
//
//  type TypeRes = (Type, Seq[TConstraint], Seq[ContextReq])
//  abstract class Type
//  abstract class TConstraint
//  abstract class ContextReq
//  type TypeTuple = (ExpKey, TypeRes)
//
//  case object Num extends ExpKind
//  case object Add extends ExpKind
//  case object Var extends ExpKind
//  case object Abs extends ExpKind
//  case object App extends ExpKind
//
//  case object TNum extends Type
//  case class TVar(x: String) extends Type
//  case class TFun(t1: Type, t2: Type) extends Type
//
//  case class EqConstraint(exp: Type, act: Type, err: String) extends TConstraint
//  case class VarReq(x: String, t: Type) extends ContextReq
//
//  var _varid = 0
//  def nextTVar(): TVar = {
//    val x = "X$" + _varid
//    _varid += 1
//    TVar(x)
//  }
//
//  def typecheckStepRep: Rep[((ExpKind, Seq[TypeRes])) => TypeRes] = staticData (
//    (p: (ExpKind, Seq[TypeRes])) => typecheckStep(p._1, p._2)
//  )
//
//  def typecheckStep(e: ExpKind, sub: Seq[TypeRes]): TypeRes = e match {
//    case Num => (TNum, scala.Seq(), scala.Seq())
//    case Add =>
//      val lcons = EqConstraint(TNum, sub(0)._1, "left operand of Add")
//      val rcons = EqConstraint(TNum, sub(1)._1, "right operand of Add")
//      (TNum, scala.Seq(lcons,rcons), scala.Seq())
//    case Var =>
//      val tvar = nextTVar()
//      (tvar, scala.Seq(), scala.Seq(VarReq()))
//  }
//
//  val types = WITH.RECURSIVE[TypeTuple] (types =>
//      (SELECT ((e: Rep[ExpTuple]) => typecheckStepRep ((e._2, Seq())))
//       FROM exps // 0-ary
//       WHERE (e => e._3.length == 0))
//    UNION ALL
//      (SELECT ((e: Rep[ExpTuple], t1: Rep[TypeTuple]) => typecheckStepRep ((e._2, Seq(t1.left))))
//       FROM (exps, types) // 1-ary
//       WHERE ((e,t1) => e._3.length == 1
//                    AND e._3(0) == t1._1 && t1._1.isLeft))
//    UNION ALL
//      (SELECT ((e: Rep[ExpTuple], t1: Rep[TypeTuple], t2: Rep[TypeTuple]) => typecheckStepRep ((e._2, Seq(t1.left, t2.left))))
//       FROM (exps, types, types) // 2-ary
//       WHERE ((e,t1,t2) => e._3.length == 2
//                       AND e._3(0) == t1._1  && t1._1.isLeft
//                       AND e._3(1) == t2._1  && t2._1.isLeft))
//  )
//
//
//
// }
