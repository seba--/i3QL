package sae.typecheck

import idb.syntax.iql.IR.Rep
import idb.{BagTable, SetTable}

/**
 * Created by seba on 27/10/14.
 */
object Exp {
  var change: Long = System.nanoTime
  val LOG_TABLE_OPS = true
  def log(s: String) = {
    val newchange = System.nanoTime
    if(LOG_TABLE_OPS) {
      println(s"Time since last table op ${(newchange - change)/1000000.0}ms")
      println(s)
    }
    change = newchange
  }

  type Lit = Any
  abstract class ExpKind
  type ExpKey = Int
  type ExpTuple = (ExpKey, ExpKind, Seq[Lit], Seq[ExpKey])
  def id(e: Rep[ExpTuple]) = e._1
  def kind(e: Rep[ExpTuple]) = e._2
  def lits(e: Rep[ExpTuple]) = e._3
  def subseq(e: Rep[ExpTuple]) = e._4

  private var _nextKey = 0

  val table = BagTable.empty[ExpTuple]


  def nextKey() = {
    val k = _nextKey
    _nextKey += 1
    k
  }
  def prefetchKey = _nextKey

  import scala.language.implicitConversions
  implicit def constructable(k: ExpKind) = new Constructable(k)
  class Constructable(k: ExpKind) {
    def apply(): Exp = Exp(k, scala.Seq(), scala.Seq())
    def apply(l: Lit, sub: Exp*): Exp = Exp(k, scala.Seq(l), scala.Seq(sub:_*))
    def apply(e: Exp, sub: Exp*): Exp = Exp(k, scala.Seq(), e +: scala.Seq(sub:_*))
    def apply(lits: Seq[Lit], sub: Seq[Exp]): Exp = Exp(k, lits, sub)
  }
}

import Exp._
case class Exp(kind: ExpKind, lits: Seq[Lit], sub: Seq[Exp]) {
  var key = -1

  def insert: ExpKey = {
    if (key != -1)
      throw new RuntimeException(s"Attempted double insert of exp $key->$this")
    val subkeys = sub map (_.insert)
    key = nextKey()
    log(s"insert ${(key, kind, lits, subkeys)}")
    table += ((key, kind, lits, subkeys))
    key
  }

  def remove: ExpKey = {
    val subkeys = sub map (_.remove)
    log(s"remove ${(key, kind, lits, subkeys)}")
    table -= ((key, kind, lits, subkeys))
    val k = key
    key = -1
    k
  }

  def updateExp(old: Exp, e: Exp, oldsubkeys: Seq[ExpKey], newsubkeys: Seq[ExpKey]): ExpKey = {
    val oldkey = old.key
    if (e.key != -1)
      throw new RuntimeException(s"Attempted double insert of exp $key->$this")
    val newkey = old.key
    e.key = newkey
    log(s"update  ($oldkey, $kind, $lits, $oldsubkeys) -> ($newkey, ${e.kind}, ${e.lits}, $newsubkeys)")
    table ~= (oldkey, old.kind, old.lits, oldsubkeys) -> (newkey, e.kind, e.lits, newsubkeys)
    newkey
  }

  def replaceWith(e: Exp): ExpKey = {
    val oldsubkeys = sub map (_.key)
    val newsubkeys = sub.zip(e.sub).map(p => p._1.replaceWith(p._2))
    if (sub.size > e.sub.size) sub.drop(e.sub.size).map(_.remove)
    val moreNewsubkeys = if (e.sub.size > sub.size) e.sub.drop(sub.size).map(_.insert) else Seq()

    updateExp(this, e, oldsubkeys, newsubkeys ++ moreNewsubkeys)
  }

  override def toString = {
    val subs = lits.map(_.toString) ++ sub.map(_.toString)
    val subssep = if (subs.isEmpty) subs else subs.flatMap(s => Seq(", ", s)).tail
    val substring = subssep.foldLeft("")(_+_)
    val keyString = "" //getkey match {case None => ""; case Some(k) => s"$k@"}
    s"$keyString$kind($substring)"
  }
}


case object Num extends ExpKind
case object String extends ExpKind
case object Add extends ExpKind
case object Mul extends ExpKind
case object Var extends ExpKind
case object Abs extends ExpKind
case object App extends ExpKind
case object If0 extends ExpKind
case object Fix extends ExpKind
