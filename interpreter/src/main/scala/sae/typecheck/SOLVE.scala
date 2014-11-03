package sae.typecheck

import idb.operators.{NotSelfMaintainableAggregateFunctionFactory, NotSelfMaintainableAggregateFunction}

import sae.typecheck.Constraint.Constraint
import sae.typecheck.ConstraintTypecheck.EqConstraint
import sae.typecheck.Type.Type

/**
 * Created by seba on 30/10/14.
 */
object SolveHelper {
  type TSubst = Map[Symbol, Type]
  type Unsolvable = Seq[Constraint]
  type Result = (TSubst, Unsolvable)
}
import SolveHelper._

trait Resetable { def reset() }

class SolveIntern[Domain <: AnyRef](val f: Domain => Constraint) extends NotSelfMaintainableAggregateFunction[Domain, ()=>Result] with Resetable {
  var substs: Map[Constraint, TSubst] = Map()
  var unres: Unsolvable = Seq()

  var result: Result = (Map(), Seq())

  var count = 0

  def reset() {
    substs = Map()
    unres = Seq()
    result = (Map(), Seq())
  }

  def get =
    if (result != null)
      ()=>result
    else {
      count += 1
      println(s"---Number of recomputation of solution to constraint system: $count")

      result = (Map(), this.unres)

      for ((c, s) <- substs)
        addSolution(c, s)

      ()=>result
    }

  def addSolution(c: Constraint, s: TSubst) = {
//    println(s"addSolution($c, $s)")
//    println(s"   on result $result")
    var subst = result._1
    var unres = result._2

    for ((k, v) <- s) {
      val t = v.subst(subst)
      subst.get(k) match {
        case None => subst = subst.mapValues(_.subst(Map(k -> t))) + (k -> t)
        case Some(t2) => t2.unify(t) match {
          case None => unres = c +: unres
          case Some(s2) => subst = subst.mapValues(_.subst(s2)) ++ s2
        }
      }
    }

    result = (subst, unres)
  }

  def add(d: Domain, data: Seq[Domain]) = {
    val c = f(d)
    println(s"add constraint $c")
    c.solve match {
      case None =>
        unres = c +: unres
        if (result != null)
          result = (result._1, c +: result._2)
      case Some(s) =>
        substs += c -> s
        if (result != null)
          addSolution(c, s)
    }
    ()=>get()
  }

  def remove(d: Domain, data: Seq[Domain]) = {
    val c = f(d)
    println(s"rem constraint $c")
    substs -= c
    unres = unres diff Seq(c)
    result = null
    ()=>get()
  }

  def update(oldV: Domain, newV: Domain, data: Seq[Domain]) = {
    remove(oldV, data)
    add(newV, data)
    ()=>get()
  }

}

object Solve
{
  def apply[Domain <: AnyRef](f: (Domain => Constraint)) = {
    new NotSelfMaintainableAggregateFunctionFactory[Domain, ()=>Result] {
      def apply(): NotSelfMaintainableAggregateFunction[Domain, ()=>Result] = {
        new SolveIntern[Domain](f)
      }
    }
  }
}