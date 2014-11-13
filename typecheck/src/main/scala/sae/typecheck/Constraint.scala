package sae.typecheck

import idb.syntax.iql.IR.Rep
import Exp._
import TypeStuff._

/**
 * Created by seba on 27/10/14.
 */

object Constraint {
  type Unsolvable = Set[Constraint]
  type Solution = (TSubst, Unsolvable)
  val emptySolution: Solution = (Map(), Set())

  def solutionVars(s: Solution) = substVars(s._1) ++ s._2.foldLeft(Set[Symbol]())((vs,c) => vs++c.vars)
  def substVars(s: TSubst) = s.foldLeft(Set[Symbol]())((vs,kv) => (vs + kv._1) ++ kv._2.vars)
  def requirementVars(reqs: Set[Requirement]) = reqs.foldLeft(Set[Symbol]())((vs,r) => vs ++ r.vars)

  def subst(reqs: Map[Symbol, Type], s: TSubst) = reqs.mapValues(_.subst(s))

  def mergeReqMaps(reqs1: Map[Symbol, Type], reqs2: Map[Symbol, Type]) = {
    val (res, time) = Util.timed(_mergeReqMaps(reqs1, reqs2))
    mergeReqsTime += time
    res
  }

  def _mergeReqMaps(reqs1: Map[Symbol, Type], reqs2: Map[Symbol, Type]) = {
    var mcons = Set[Constraint]()
    var mreqs = reqs1
    for ((x, r2) <- reqs2)
      reqs1.get(x) match {
        case None => mreqs += x -> r2
        case Some(r1) =>
          mcons = mcons + EqConstraint(r1, r2)
      }

    (mcons, mreqs)
  }

  var mergeReqsTime = 0.0
  def mergeReqs(reqs1: Set[Requirement], reqs2: Set[Requirement]) = {
    val (res, time) = Util.timed(_mergeReqs(reqs1, reqs2))
    mergeReqsTime += time
    res
  }

  def _mergeReqs(reqs1: Set[Requirement], reqs2: Set[Requirement]) = {
    var mcons = Set[Constraint]()
    var mreqs = reqs1
    for (r2 <- reqs2)
      mergeReq(mreqs, r2) match {
        case Some((newcons, newreqs)) => mcons = newcons; mreqs = newreqs
        case None => mreqs += r2
      }
    (mcons, mreqs)
  }

  def mergeReq(reqs1: Set[Requirement], r2: Requirement): Option[(Set[Constraint], Set[Requirement])] = {
    for (r1 <- reqs1)
      r1 merge r2 match {
        case (mr, newcons) => return Some((newcons, (reqs1 diff Set(r1)) + mr.asInstanceOf[Requirement]))
      }
    None
  }

  var mergeFreshTime = 0.0
  def mergeFresh(fresh1: Set[Symbol], fresh2: Set[Symbol]) = {
    val (merged, time) = Util.timed(_mergeFresh(fresh1, fresh2))
    mergeFreshTime += time
    merged
  }

  def _mergeFresh(fresh1: Set[Symbol], fresh2: Set[Symbol]) = {
    var allfree = fresh1 ++ fresh2
    var mfresh = fresh1
    var ren = Map[Symbol, Symbol]()
    for (x <- fresh2)
      if (fresh1.contains(x)) {
        val newx = tick(x, allfree)
        allfree += newx
        mfresh += newx
        ren += x -> newx
      }
      else
        mfresh += x
    
//    if (!ren.isEmpty)
//      println(s"Merge fresh vars $fresh1 and $fresh2")
    
    (mfresh, ren)
  }

  var mergeSolutionTime = 0.0

  def mergeSolution(sol1: Solution, sol2: Solution): Solution = {
    val (res, time) = Util.timed(mergeSolutionConflictfree(sol1, sol2))
    mergeSolutionTime += time
    res
  }

  private def mergeSolutionConflictfree(sol1: Solution, sol2: Solution): Solution = {
    val s1 = sol1._1
    val s2 = sol2._1
    var unres: Unsolvable = sol1._2 ++ sol2._2

    val s1s = s1 // s1.mapValues(_.subst(s2))
    val res = s1s ++ s2
//    assert(res.size == s1.size + s2.size)
//
//    if (s1s != s1)
//      println(s"*** difference found")

    (res, unres)
  }

  def mergeSolutionC(sol1: Solution, sol2: Solution): Solution = {
    val (res, time) = Util.timed(_mergeSolution(sol1, sol2))
    mergeSolutionTime += time
    res
  }

  private def _mergeSolution(sol1: Solution, sol2: Solution): Solution = {
    val s1 = sol1._1
    val s2 = sol2._1
    var unres: Unsolvable = sol1._2 ++ sol2._2

    var s = s1 mapValues (_.subst(s2))

//    val overlap = s1.keySet.intersect(s2.keySet)
//    if (!overlap.isEmpty)
//      println(s"Overlap in merge solution: $overlap")

    for ((x, t2) <- s2) {
      s.get(x) match {
        case None => s += x -> t2.subst(s)
        case Some(t1) => t1.unify(t2, s) match {
          case None => unres += EqConstraint(t1, t2)
          case Some(u) => s = s.mapValues(_.subst(u)) ++ u
        }
      }
    }

    (s, unres)
  }

  var computeReqsTime = 0.0
  var allVailableCheckTime = 0.0

  var extendSolutionTime = 0.0
  def extendSolution(sol: Solution, cs: Iterable[Constraint]): (TSubst, Set[Constraint]) = {
    constraintCount += cs.size
    val (res, time) = Util.timed(_extendSolution(sol, cs))
    extendSolutionTime += time
    res
  }

  private def _extendSolution(sol: Solution, cs: Iterable[Constraint]): (TSubst, Set[Constraint]) = {
    val esol = cs.foldLeft[Solution]((Map(), Set()))(_extendSolution)
    _mergeSolution(sol, esol)
  }


  var constraintCount = 0
  def solve(cs: Iterable[Constraint]): Solution = {
    cs.foldLeft(emptySolution)(extendSolution)
  }
  def solve(c: Constraint): Solution = {
    extendSolution(emptySolution, c)
  }

  def extendSolution(sol: Solution, c: Constraint): (TSubst, Set[Constraint]) = {
    constraintCount += 1
    val (res, time) = Util.timed(_extendSolution(sol, c))
    extendSolutionTime += time
    res
  }

  private def _extendSolution(sol: Solution, c: Constraint): (TSubst, Set[Constraint]) = {
    c.solve(sol._1) match {
      case None => (sol._1, sol._2 + c)
      case Some(u) =>
//        println(s"Extend solution with $c: $u + $sol")
        val res = _mergeSolution(sol, (u, Set()))
//        println(s"  => $res")
        res
    }
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

  def rename(ren: Map[Symbol, Symbol])(p: (Type, Set[Constraint], Set[Requirement])) =
    (p._1.rename(ren), p._2.map(_.rename(ren)), p._3.map(_.rename(ren)))

  var renameSolutionTime = 0.0

  def renameSolution(ren: Map[Symbol, Symbol])(p: (Type, Solution, Set[Requirement])): (Type, Solution, Set[Requirement]) = {
    val (res, time) = Util.timed((p._1.rename(ren), renameSolution(ren, p._2), p._3.map(_.rename(ren))))
    renameSolutionTime += time
    res
  }

  def renameSolution(ren: Map[Symbol, Symbol], sol: Solution): Solution =
    (sol._1.map(kv => ren.getOrElse(kv._1, kv._1) -> kv._2.rename(ren)), sol._2.map(_.rename(ren)))


  type ConstraintTuple = (ExpKey, ConstraintData)
  type ConstraintData = (Type, Set[Constraint], Set[Requirement], Set[Symbol])

  type ConstraintIncTuple = (ExpKey, ConstraintIncData)
  type ConstraintIncData = (Type, Set[Constraint], Set[Requirement])

  type FreshTuple = (ExpKey, FreshData)
  type FreshData = (Set[Symbol], // fresh variables requested in all of subtree
                    Set[Map[Symbol, Symbol]]) // renaming for subtrees skipping first one (n-ary op => Set.length == max(0, n - 1))

  type ConstraintSolutionTuple = (Parent, Position, ConstraintSolutionData)
  type ConstraintSolutionData = (Type, Solution, Set[Requirement], Set[Symbol])

  type ConstraintNonrelationalSolutionTuple = (Parent, Position, ConstraintSolutionData)
  type ConstraintNonrelationalSolutionData = (Type, Map[Symbol, Type], Unsolvable)

  def cid(c: Rep[ConstraintTuple]) = c._1
  def cdata(c: Rep[ConstraintTuple]) = c._2

  def ctype(c: Rep[ConstraintTuple]) = c._2._1
  def cons(c: Rep[ConstraintTuple]) = c._2._2
  def reqs(c: Rep[ConstraintTuple]) = c._2._3

  def ctype(c: ConstraintData) = c._1
  def cons(c: ConstraintData) = c._2
  def reqs(c: ConstraintData) = c._3

  def csparent(c: Rep[ConstraintSolutionTuple]) = c._1
  def cspos(c: Rep[ConstraintSolutionTuple]) = c._2
  def csdata(c: Rep[ConstraintSolutionTuple]) = c._3
}

abstract class Constraint {
  def solve(s: TSubst): Option[Map[Symbol, Type]]
  def rename(ren: Map[Symbol, Symbol]): Constraint
  def vars: Set[Symbol]
}

trait SimpleRequirement {
  def merge(r: SimpleRequirement): (SimpleRequirement, Set[Constraint])
}
abstract class Requirement extends SimpleRequirement {
  def rename(ren: Map[Symbol, Symbol]): Requirement
  def vars: Set[Symbol]
}

case class EqConstraint(expected: Type, actual: Type) extends Constraint {
  def rename(ren: Map[Symbol, Symbol]) = EqConstraint(expected.rename(ren), actual.rename(ren))
  def solve(s: TSubst) = expected.unify(actual, s)
  def vars = expected.vars ++ actual.vars
}

case class VarRequirement(x: Symbol, t: Type) extends Requirement {
  def merge(r: SimpleRequirement) = r match {
    case VarRequirement(`x`, t2) => (this, Set(EqConstraint(t, t2)))
  }
  def rename(ren: Map[Symbol, Symbol]) = VarRequirement(ren.getOrElse(x, x), t.rename(ren))
  def vars = t.vars
}

case class SimpleVarRequirement(t: Type) extends SimpleRequirement {
  def merge(r: SimpleRequirement) = r match {
    case SimpleVarRequirement(t2) => (this, Set(EqConstraint(t, t2)))
  }
}