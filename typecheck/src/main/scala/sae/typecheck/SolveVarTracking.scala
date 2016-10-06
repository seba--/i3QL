//package sae.typecheck
//
//import idb.operators.{NotSelfMaintainableAggregateFunctionFactory, NotSelfMaintainableAggregateFunction}
//import sae.typecheck.Constraint.{Unsolvable, Constraint}
//
//import SolveHelper._
//import sae.typecheck.Type.TSubst
//
///**
// * Created by seba on 03/11/14.
// */
//class SolveVarTrackingIntern[Domain <: AnyRef](val f: Domain => Constraint) extends NotSelfMaintainableAggregateFunction[Domain, ()=>Result] with Resetable {
//  var substs: Map[Constraint, TSubst] = Map()
//  var unres: Unsolvable = Seq()
//
//  var result: TSubst = Map()
//
//  var track: Map[Symbol, Set[Constraint]] = Map()
//
//  var count = 0
//
//  def reset() {
//    substs = Map()
//    unres = Seq()
//    track = Map()
//    result = Map()
//  }
//
//  def addTrack(k: Symbol, c: Constraint) = {
//    println(s"add track $k -> $c")
//    track.get(k) match {
//      case None => track += k -> Set(c)
//      case Some(s) => track += k -> (s + c)
//    }
//  }
//
//  def get = ()=>(result, unres)
//
//  def addSolution(c: Constraint, s: TSubst) = {
//    //    println(s"addSolution($c, $s)")
//    //    println(s"   on result $result")
//    for ((k, v) <- s) {
//      addTrack(k, c)
//      val t = v.subst(result)
//      result.get(k) match {
//        case None => result = result.mapValues(_.subst(Map(k -> t))) + (k -> t)
//        case Some(t2) => t2.unify(t) match {
//          case None => unres = c +: unres
//          case Some(s2) =>
//            println(s"  sub-unifier on $k->$t is $s2")
//            for (x <- s2.keys)
//              addTrack(x, c)
//            for (cother <- track(k) if cother != c;
//                 x <- s2.keys)
//              addTrack(x, cother)
//
//            result = result.mapValues(_.subst(s2)) ++ s2
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
//      case Some(s) =>
//        substs += c -> s
//        addSolution(c, s)
//    }
//    ()=>get()
//  }
//
//  def remove(d: Domain, data: Seq[Domain]) = {
//    val c = f(d)
//    println(s"rem constraint $c")
//    substs -= c
//    unres = unres diff Seq(c)
//
////    println(s"var tracking $track")
//
//    var vars = Set[Symbol]()
//    var interdeps = Set[Constraint]()
//    for ((x,s) <- track)
//      if (s.contains(c)) {
//        vars += x
//        val rest = s - c
//        interdeps ++= rest
//        track += x -> rest
//      }
//    println(s"  vars $vars, interdeps $interdeps")
//
//    vars foreach {x =>
//      result -= x
//    }
//    println(s"Cleaned-up result $result")
//
//    interdeps foreach {c =>
//      substs.get(c) match {
//        case Some(s) =>
//          unres = unres diff Seq(c)
//          println(s"restore solution $c -> s")
//          addSolution(c, s)
//        case None => {}
//      }
//    }
//
////    result = null
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
//object SolveVarTracking
//{
//  def apply[Domain <: AnyRef](f: (Domain => Constraint)) = {
//    new NotSelfMaintainableAggregateFunctionFactory[Domain, ()=>Result] {
//      def apply(): NotSelfMaintainableAggregateFunction[Domain, ()=>Result] = {
//        new SolveIntern[Domain](f)
//      }
//    }
//  }
//}