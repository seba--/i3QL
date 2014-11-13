package sae.typecheck.bottomup

import sae.typecheck.Constraint._
import sae.typecheck.Exp.ExpKind
import sae.typecheck.TypeStuff._
import sae.typecheck._

/**
 * Created by seba on 12/11/14.
 */
object Nonrelational extends TypeCheck {

  private var _nextId = 0
  def freshTVar(): TVar = {
    val v = TVar(Symbol("x$" + _nextId))
    _nextId += 1
    v
  }
  def freshTVarForVar(): TVar = {
    val v = TVar(Symbol("var$" + _nextId))
    _nextId += 1
    v
  }

  def reset() {}
  def typecheckIncremental(e: Exp): Either[Type, TError] = typecheck(e)()


  def typecheck(e: Exp): () => Either[Type, TError] = {
    _nextId = 0
    val (root, leaves) = BUExp.from[ConstraintNonrelationalSolutionData](e)

    () => {
      leaves foreach (typecheckSpine(_))

      val (t, reqs, unres) = root.Type
      if (!reqs.isEmpty)
        scala.Right(s"Unresolved context requirements $reqs, type $t, unres $unres")
      else if (!unres.isEmpty)
        scala.Right(s"Unresolved constraints $unres, type $t")
      else
        scala.Left(t)
    }
  }

  def typecheckSpine(e2: BUExp[ConstraintNonrelationalSolutionData]): Unit ={

    var current = e2
    while (current != null && current.allKidTypesAvailable) {
      val isFirstTime = current.Type == null
      val isRoot = current.parent == null

      val t = typecheckStep(current)
//      println(s"$current -> ")
//      println(s" * ${t._1}")
//      println(s" * ${t._2}")
//      println(s" * ${t._3}")
      if (current.Type != t) {
        current.Type = t
        if (!isRoot && isFirstTime)
          current.parent.markKidTypeAvailable(current.pos)
        current = current.parent
      }
    }
  }

  def typecheckStep(e: BUExp[ConstraintNonrelationalSolutionData]): ConstraintNonrelationalSolutionData = e.kind match {
    case (Num, 0) => (TNum, Map(), Set())
    case (op, 2) if op == Add || op == Mul =>
      val (t1, reqs1, unres1) = e.kids(0).Type
      val (t2, reqs2, unres2) = e.kids(1).Type

      val lcons = EqConstraint(TNum, t1)
      val rcons = EqConstraint(TNum, t2)

      val (mcons, mreqs) = mergeReqMaps(reqs1, reqs2)

      val (s, newunres) = solve(mcons + lcons + rcons)
      (TNum, subst(mreqs, s), unres1 ++ unres2 ++ newunres)
    case (Var, 0) =>
      val x = e.lits(0).asInstanceOf[Symbol]
      val X = freshTVarForVar()
      (X, Map(x -> X), Set())
    case (App, 2) =>
      val (t1, reqs1, unres1) = e.kids(0).Type
      val (t2, reqs2, unres2) = e.kids(1).Type

      val X = freshTVar()
      val fcons = EqConstraint(TFun(t2, X), t1)
      val (mcons, mreqs) = mergeReqMaps(reqs1, reqs2)

      val (s, newunres) = solve(mcons + fcons)

      (X.subst(s), subst(mreqs, s), unres1 ++ unres2 ++ newunres)
    case (Abs, 1) =>
      if (e.lits(0).isInstanceOf[Symbol]) {
        val x = e.lits(0).asInstanceOf[Symbol]
        val (t, reqs, unres) = e.kids(0).Type

        val X = freshTVar()
        reqs.get(x) match {
          case None =>
            (TFun(X, t), reqs, unres)
          case Some(treq) =>
            val otherReqs = reqs - x
            val xcons = EqConstraint(X, treq)
            val (s, newunres) = solve(xcons)
            (TFun(X, t).subst(s), subst(otherReqs, s), unres ++ newunres)
        }
      }
      else if (e.lits(0).isInstanceOf[Seq[Symbol]]) {
        val xs = e.lits(0).asInstanceOf[Seq[Symbol]]
        val (t, reqs, unres) = e.kids(0).Type

        val Xs = xs map (_ => freshTVar())

        var restReqs = reqs
        var xscons = Seq[Constraint]()
        for (i <- 0 until xs.size) {
          val x = xs(i)
          val X = Xs(i)
          restReqs.get(x) match {
            case None => {}
            case Some(treq) =>
              restReqs = restReqs - x
              xscons = EqConstraint(X, treq) +: xscons
          }
        }

        val (s, newunres) = solve(xscons)
        val tfun = Xs.foldRight(t.subst(s))((X, t) => TFun(X.subst(s), t))
        (tfun, subst(restReqs, s), unres ++ newunres)
      }
      else {
        println(s"cannot handle ${e.lits(0)}")
        throw new RuntimeException()
      }
    case (If0, 3) =>
      val (t1, reqs1, unres1) = e.kids(0).Type
      val (t2, reqs2, unres2) = e.kids(1).Type
      val (t3, reqs3, unres3) = e.kids(2).Type

      val (mcons12, mreqs12) = mergeReqMaps(reqs1, reqs2)
      val (mcons23, mreqs123) = mergeReqMaps(mreqs12, reqs3)

      val cond = EqConstraint(TNum, t1)
      val body = EqConstraint(t2, t3)

      val (s, newunres) = solve(mcons12 ++ mcons23 + cond + body)

      (t2.subst(s), subst(mreqs123, s), unres1 ++ unres2 ++ unres3 ++ newunres)

    case (Fix, 1) =>
      val (t, reqs, unres) = e.kids(0).Type
      val X = freshTVar()
      val fixCons = EqConstraint(t, TFun(X, X))
      val (s, newunres) = solve(fixCons)
      (X.subst(s), subst(reqs, s), unres ++ newunres)
  }


  def main(args: Array[String]): Unit = {
    printTypecheck(Add(Num(17), Add(Num(10), Num(2))))
    printTypecheck(Add(Num(17), Add(Num(10), Num(5))))
    printTypecheck(Abs('x, Add(Num(10), Num(5))))
    printTypecheck(Abs('x, Add(Var('x), Var('x))))
    printTypecheck(Abs('x, Add(Var('err), Var('x))))
    printTypecheck(Abs('x, Abs('y, App(Var('x), Var('y)))))
    printTypecheck(Abs('x, Abs('y, Add(Var('x), Var('y)))))
    printTypecheck(If0(Num(17), Num(0), Num(1)))

    val fac = Fix(Abs('f, Abs('n, If0(Var('n), Num(1), Mul(Var('n), App(Var('f), Add(Var('n), Num(-1))))))))
    printTypecheck("factorial", fac)
    printTypecheck("eta-expanded factorial", Abs('x, App(fac, Var('x))))

    val fib = Fix(Abs('f, Abs('n,
      If0(Var('n), Num(1),
        If0(Add(Var('n), Num(-1)), Num(1),
          Add(App(Var('f), Add(Var('n), Num(-1))),
            App(Var('f), Add(Var('n), Num(-2)))))))))
    printTypecheck("fibonacci function", fib)
    printTypecheck("factorial+fibonacci", Abs('x, Add(App(fac, Var('x)), App(fib, Var('x)))))
    printTypecheck(Abs('y, Var('y)))
  }
}


object BUExp {
  def apply[T](kind: ExpKind, lits: Seq[Any], parent: BUExp[T], pos: Int) = new BUExp(kind, lits, parent, pos)

  def from[T](e: Exp): (BUExp[T], Seq[BUExp[T]]) = {
    val leaves = collection.mutable.ArrayBuffer[BUExp[T]]()
    val bue = convert[T](e, null, -1, leaves)
    (bue, leaves.toSeq)
  }

  private def convert[T](e: Exp, parent: BUExp[T], pos: Int, leaves: collection.mutable.ArrayBuffer[BUExp[T]]): BUExp[T] = {
    var input = Seq[(Exp, BUExp[T], Int)]()
    var pending = Seq[BUExp[T]]()
    var output = Seq[BUExp[T]]()

    input = (e, null, -1) +: input

    while (!input.isEmpty || !pending.isEmpty) {
      if (!pending.isEmpty && (input.isEmpty || input.head._2 != pending.head)) {
        val next = pending.head
        pending = pending.tail

        var kids = Seq[BUExp[T]]()
        while (!output.isEmpty && output.head.parent == next) {
          kids = output.head +: kids
          output = output.tail
        }

        next.kids = kids.reverse
        output = next +: output
      }
      else {
        val (next, parent, pos) = input.head
        input = input.tail

        val bue = BUExp[T](next.kind, next.lits, parent, pos)
        if (next.sub.isEmpty) {
          bue.kids = Seq()
          output = bue +: output
          leaves += bue
        }
        else {
          pending = bue +: pending
          for (i <- 0 until next.sub.size)
            input = (next.sub(i), bue, i) +: input
        }
      }
    }

    output.head

//    val bue = BUExp[T](e.kind, e.lits,parent, pos)
//    if (e.sub.isEmpty) {
//      bue.kids = Seq()
//      leaves += bue
//    }
//    else {
//      val kids = for (i <- 0 until e.sub.size)
//        yield convert[T](e.sub(i), bue, i, leaves)
//      bue.kids = kids
//    }
//    bue
  }
}
class BUExp[T](val kind: ExpKind, val lits: Seq[Any], val parent: BUExp[T], val pos: Int) {
  private var _kids: Seq[BUExp[T]] = _
  var availableKidTypes: Seq[Boolean] = _

  var Type: T = _

  def kids = _kids
  def kids_=(es: Seq[BUExp[T]]) = {
    _kids = es
    availableKidTypes = es map (_.Type != null)
  }

  def markKidTypeAvailable(pos: Int) =
    availableKidTypes = availableKidTypes.updated(pos, true)

  def allKidTypesAvailable = {
    val (b, time) = Util.timed(availableKidTypes.foldLeft(true)(_&&_))
    Constraint.allVailableCheckTime += time
    b
  }

  override def toString = {
    val subs = lits.map(_.toString) ++ _kids.map(_.toString)
    val subssep = if (subs.isEmpty) subs else subs.flatMap(s => Seq(", ", s)).tail
    val substring = subssep.foldLeft("")(_+_)
    s"${kind._1}($substring)"

  }
}
