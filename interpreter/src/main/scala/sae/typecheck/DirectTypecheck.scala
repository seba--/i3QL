package sae.typecheck

import idb.syntax.iql._
import idb.syntax.iql.IR._

import sae.typecheck.Exp._
import sae.typecheck.Type._

/**
* Created by seba on 26/10/14.
*/
object DirectTypecheck {

  case object Num extends ExpKind
  case object Add extends ExpKind

  case object TNum extends Type

  def typecheckStepRep: Rep[((ExpKind, Seq[Lit], Seq[Type])) => Either[Type, TError]] = staticData (
    (p: (ExpKind, Seq[Lit], Seq[Type])) => typecheckStep(p._1, p._2, p._3)
  )

  def typecheckStep(e: ExpKind, lits: Seq[Lit], sub: Seq[Type]): Either[Type, TError] = e match {
    case Num => scala.Left(TNum)
    case Add =>
      if (sub(0) != TNum)
        scala.Right(s"Left child of Add should be TNum but was ${sub(0)}")
      else if (sub(1) != TNum)
        scala.Right(s"Right child of Add should be TNum but was ${sub(1)}")
      else
        scala.Left(TNum)
  }

  val types = WITH.RECURSIVE[TypeTuple] (types =>
      (SELECT ((e: Rep[ExpTuple]) => id(e) -> typecheckStepRep ((kind(e), lits(e), Seq())))
       FROM Exp.table // 0-ary
       WHERE (e => subseq(e).length == 0))
    UNION ALL (
      (SELECT ((e: Rep[ExpTuple], t1: Rep[TypeTuple]) => id(e) -> typecheckStepRep ((kind(e), lits(e), Seq(getType(t1)))))
       FROM (Exp.table, types) // 1-ary
       WHERE ((e,t1) => subseq(e).length == 1
                    AND subseq(e)(0) == tid(t1) AND isType(t1)))
    UNION ALL
      (SELECT ((e: Rep[ExpTuple], t1: Rep[TypeTuple], t2: Rep[TypeTuple]) => id(e) -> typecheckStepRep ((kind(e), lits(e), Seq(getType(t1), getType(t2)))))
       FROM (Exp.table, types, types) // 2-ary
       WHERE ((e,t1,t2) => subseq(e).length == 2
                       AND subseq(e)(0) == tid(t1) AND isType(t1)
                       AND subseq(e)(1) == tid(t2) AND isType(t2)))
    )
  )


  def main(args: Array[String]): Unit = {
    val expressions = Exp.table.asMaterialized
    val resultTypes = types.asMaterialized

    val e = Add(Num(17), Num(12))
    e.insert
    printTyings(e, resultTypes)
    expressions foreach (Predef.println(_))
    resultTypes foreach (Predef.println(_))
    Predef.println()

    e.insert
    printTyings(e, resultTypes)
    expressions foreach (Predef.println(_))
    resultTypes foreach (Predef.println(_))
    Predef.println()

    e.remove
    printTyings(e, resultTypes)
    expressions foreach (Predef.println(_))
    resultTypes foreach (Predef.println(_))
    Predef.println()

    //    val e2 = Add(Num(17), Add(Num(10), Num(2)))
//    e.replaceWith(e2)
//    printTyings(e2, resultTypes)
//    expressions foreach (Predef.println(_))
//    resultTypes foreach (Predef.println(_))
//    Predef.println()
//
//    val e3 = Add(Add(Num(17), Num(1)), Add(Num(10), Num(2)))
//    e2.replaceWith(e3)
//    printTyings(e3, resultTypes)
//    expressions foreach (Predef.println(_))
//    resultTypes foreach (Predef.println(_))
//    Predef.println()
//
//    val e4 = Num(30)
//    e3.replaceWith(e4)
//    printTyings(e4, resultTypes)
//    expressions foreach (Predef.println(_))
//    resultTypes foreach (Predef.println(_))
//    Predef.println()
  }

}
