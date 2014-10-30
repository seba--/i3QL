package sae.typecheck

import idb.syntax.iql._
import idb.syntax.iql.IR._

import sae.typecheck.Exp._
import sae.typecheck.Type._

/**
* Created by seba on 26/10/14.
*/
object DirectTypecheck  {

  case object Num extends ExpKind
  case object Add extends ExpKind
  case object String extends ExpKind

  case object TNum extends Type {
    def rename(ren: Map[Symbol, Symbol]) = this
    def subst(s: TSubst) = this
    def unify(other: Type) = other match {
      case TNum => scala.Some(Map())
      case _ => None
    }
  }
  case object TString extends Type {
    def rename(ren: Map[Symbol, Symbol]) = this
    def subst(s: TSubst) = this
    def unify(other: Type) = other match {
      case TString => scala.Some(Map())
      case _ => None
    }
  }

  def typecheckStepRep: Rep[((ExpKind, Seq[Lit], Seq[Type])) => Either[Type, TError]] = staticData (
    (p: (ExpKind, Seq[Lit], Seq[Type])) => typecheckStep(p._1, p._2, p._3)
  )

  def typecheckStep(e: ExpKind, lits: Seq[Lit], sub: Seq[Type]): Either[Type, TError] = e match {
    case Num => scala.Left(TNum)
    case String => scala.Left(TString)
    case Add =>
      val ltype = sub(0)
      val rtype = sub(1)
      if (ltype != rtype)
        scala.Right(s"Left and right child of Add must have same type, but was $ltype and $rtype")
      else if (ltype != TNum && ltype != TString)
        scala.Right(s"Left and right child must have type TNum or TString, but was $ltype")
      else
        scala.Left(ltype)
    case Root.Root => if (sub.isEmpty) scala.Right("Uninitialized root") else scala.Left(Root.TRoot(sub(0)))
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

  val rootTypeExtractor = (x: scala.Either[Type, TError]) => x match {
    case scala.Left(Root.TRoot(t)) => scala.Left(t)
    case scala.Right(msg) => scala.Right(msg)
    case scala.Left(t) => throw new RuntimeException(s"Unexpected root type $t")
  } 
  
  def main(args: Array[String]): Unit = {
    val expressions = Exp.table.asMaterialized
    val resultTypes = types.asMaterialized
    val root = Root(types, staticData (rootTypeExtractor))

    val e = Add(Num(17), Num(18))
    root.set(e)
    expressions foreach (Predef.println(_))
    Predef.println(s"Type of $e is ${root.Type}")

    val e2 = Add(String("ab"), String("b"))
    root.set(e2)
    expressions foreach (Predef.println(_))
    Predef.println(s"Type of $e2 is ${root.Type}")

    val e3 = Add(Add(Num(17), Num(1)), Add(Num(10), Num(2)))
    root.set(e3)
    Predef.println(s"Type of $e3 is ${root.Type}")

    val e4 = Add(Add(Num(17), Num(1)), Add(Num(17), Num(1)))
    root.set(e4)
    Predef.println(s"Type of $e4 is ${root.Type}")

    val e5 = Num(30)
    root.set(e5)
    Predef.println(s"Type of $e5 is ${root.Type}")

    val e6 = Add(Num(17), Num(12))
    root.set(e6)
    Predef.println(s"Type of $e6 is ${root.Type}")

    val e7 = Add(Num(17), String("abcdef"))
    root.set(e7)
    Predef.println(s"Type of $e7 is ${root.Type}")

    val e8 = Add(Num(17), Num(13))
    root.set(e8)
    Predef.println(s"Type of $e8 is ${root.Type}")
  }

}
