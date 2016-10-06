package sae.typecheck.bottomup;

import idb.syntax.iql._
import idb.syntax.iql.IR.{String=>_,_}

import sae.typecheck._
import sae.typecheck.Exp
import sae.typecheck.Exp._
import sae.typecheck.TypeStuff._
import sae.typecheck.Constraint._
import sae.typecheck.TypeCheck

/**
* Created by seba on 26/10/14.
*/
object DirectTypeCheck extends TypeCheck {

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

  val root = Root(types, staticData (rootTypeExtractor))
  def typecheck(e: Exp) = {
    val fire = root.set(e)
    () => {fire(); root.Type}
  }

  def typecheckIncremental(e: Exp) = {
    root.update(e)
    root.Type
  }

  def reset() {root.reset()}

  def main(args: Array[String]): Unit = {
    printTypecheck(Add(Num(17), Num(18)))
    printTypecheck(Add(String("ab"), String("b")))
    printTypecheck(Add(Add(Num(17), Num(1)), Add(Num(10), Num(2))))
    printTypecheck(Add(Add(Num(17), Num(1)), Add(Num(17), Num(1))))
    printTypecheck(Num(30))
    printTypecheck(Add(Num(17), Num(12)))
    printTypecheck(Add(Num(17), String("abcdef")))
    printTypecheck(Add(Num(17), Num(13)))
  }

}
