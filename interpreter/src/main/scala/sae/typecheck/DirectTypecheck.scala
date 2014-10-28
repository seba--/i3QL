package sae.typecheck

import idb.observer.Observer
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
    case root.Root => if (sub.isEmpty) scala.Right("Uninitialized root") else scala.Left(root.TRoot(sub(0)))
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


  // use root to ensure fixed rootkey for all programs
  object root {
    case object Root extends ExpKind
    case class TRoot(t: Type) extends Type

    val rootNode = Root()
    def replaceWith(e: Exp) = rootNode.replaceWith(Root(e))

    val rootKey = Exp.prefetchKey
    val rootQuery = SELECT ((t: Rep[TypeTuple]) => t._2) FROM types WHERE (t => tid(t) == rootKey)

    var Type: Either[Type, TError] = scala.Right("Uninitialized root")
    def store(t: Either[Type, TError]) = t match {
      case scala.Left(TRoot(t)) => Type = scala.Left(t)
      case scala.Right(msg) => Type = scala.Right(msg)
      case _ => throw new RuntimeException(s"Unexpected root type $t")
    }
    rootQuery.addObserver(new Observer[Either[Type, TError]] {
      override def updated(oldV: Either[Type, TError], newV: Either[Type, TError]): Unit = store(newV)
      override def endTransaction(): Unit = {}
      override def removed(v: Either[Type, TError]): Unit = Type = scala.Right("Uninitialized root")
      override def added(v: Either[Type, TError]): Unit = store(v)
    })
    rootNode.insert
  }

  def main(args: Array[String]): Unit = {
    val resultTypes = types.asMaterialized

    val e = Add(Num(17), Num(12))
    root.replaceWith(e)
    Predef.println(s"Type of $e is ${root.Type}")

    val e2 = Add(Num(17), Add(Num(10), Num(2)))
    e.replaceWith(e2)
    Predef.println(s"Type of $e2 is ${root.Type}")

    val e3 = Add(Add(Num(17), Num(1)), Add(Num(10), Num(2)))
    e2.replaceWith(e3)
    Predef.println(s"Type of $e3 is ${root.Type}")

    val e4 = Add(Add(Num(17), Num(1)), Add(Num(17), Num(1)))
    e3.replaceWith(e4)
    Predef.println(s"Type of $e4 is ${root.Type}")

    val e5 = Num(30)
    e4.replaceWith(e5)
    Predef.println(s"Type of $e5 is ${root.Type}")
  }

}
