//package sae.typecheck
//
//import idb.operators.{NotSelfMaintainableAggregateFunctionFactory, NotSelfMaintainableAggregateFunction}
//
//import sae.typecheck.Constraint.Constraint
//import sae.typecheck.ConstraintTypecheck.EqConstraint
//import sae.typecheck.Type.Type
//
///**
//* Created by seba on 30/10/14.
//*/
//import SolveHelper._
//
//private class SolveRollbackIntern[Domain <: AnyRef](val f: Domain => Constraint)extends NotSelfMaintainableAggregateFunction[Domain, ()=>Result]
//{
//  var substs: List[(Constraint, Option[TSubst])] = List()
//
//  var count = 0
//
//  def get = () => {
//    count += 1
//    println(s"---Number of recomputation of solution to constraint system: $count")
//
//    var subst: TSubst = Map()
//    var unres: Unsolvable = Seq()
//
//    for ((c, ms) <- substs)
//      ms match {
//        case None => unres = c +: unres
//        case Some(s) => subst = subst.mapValues(_.subst(s)) ++ s
//      }
//
//    (subst, unres)
//  }
//
//  def addSolution(c: Constraint, s: TSubst) = {
//    //    println(s"addSolution($c, $s)")
//    //    println(s"   on result $result")
//    for ((k, v) <- s) {
//      val t = v.subst(subst)
//      subst.get(k) match {
//        case None => subst = subst.mapValues(_.subst(Map(k -> t))) + (k -> t)
//        case Some(t2) => t2.unify(t) match {
//          case None => unres = c +: unres
//          case Some(s2) => subst = subst.mapValues(_.subst(s2))
//        }
//      }
//    }
//  }
//
//  def add(d: Domain, data: Seq[Domain]) = {
//    val c = f(d)
//    println(s"add constraint $c")
//    c.solve match {
//      case None =>
//        unres = c +: unres
//        if (result != null)
//          result = (result._1, c +: result._2)
//      case Some(s) =>
//        substs += c -> s
//        if (result != null)
//          addSolution(c, s)
//    }
//    get
//  }
//
//  def remove(d: Domain, data: Seq[Domain]) = {
//    val c = f(d)
//    println(s"rem constraint $c")
//    substs -= c
//    unres = unres diff Seq(c)
//    result = null
//    ()=>get()
//  }
//
//  def update(oldV: Domain, newV: Domain, data: Seq[Domain]) = {
//    remove(oldV, data)
//    add(newV, data)
//    ()=>get()
//  }
//
//}
//
//object SolveRollback
//{
//  def apply[Domain <: AnyRef](f: (Domain => Constraint)) = {
//    new NotSelfMaintainableAggregateFunctionFactory[Domain, ()=>Result] {
//      def apply(): NotSelfMaintainableAggregateFunction[Domain, ()=>Result] = {
//        new SolveRollbackIntern[Domain](f)
//      }
//    }
//  }
//}