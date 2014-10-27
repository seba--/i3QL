package sae.typecheck

import idb.MaterializedView
import idb.syntax.iql.IR.Rep

/**
 * Created by seba on 27/10/14.
 */
import Exp._
object Type {
  type TError = String
  abstract class Type
  type TypeTuple = (ExpKey, Either[Type, TError])
  def tid(t: Rep[TypeTuple]) = t._1
  def isType(t: Rep[TypeTuple]) = t._2.isLeft
  def getType(t: Rep[TypeTuple]) = t._2.leftGet
  def isError(t: Rep[TypeTuple]) = t._2.isRight
  def getError(t: Rep[TypeTuple]) = t._2.rightGet


  def printTyings(e: Exp, types: MaterializedView[TypeTuple]) {
    var typeMap = Map[ExpKey, Either[Type, TError]]()
    types foreach (kv => typeMap += kv)
    printTyings(e, typeMap)
  }

  def printTyings(e: Exp, types: Map[ExpKey, Either[Type, TError]]) {
    e.sub foreach (printTyings(_, types))
    val key = e.key
    types.get(key) match {
      case Some(Left(t)) => Predef.println(s"$key -> $t, $e")
      case Some(Right(msg)) => Predef.println(s"$key -> Error $msg, $e")
      case None => Predef.println(s"$key -> ERROR: not defined")
    }
  }

}