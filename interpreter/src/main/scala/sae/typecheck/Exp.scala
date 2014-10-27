package sae.typecheck

import idb.syntax.iql.IR.Rep
import idb.SetTable

/**
 * Created by seba on 27/10/14.
 */
object Exp {
  type Lit = Any
  abstract class ExpKind
  type ExpKey = Int
  type ExpTuple = (ExpKey, ExpKind, Seq[Lit], Seq[ExpKey])
  def id(e: Rep[ExpTuple]) = e._1
  def kind(e: Rep[ExpTuple]) = e._2
  def lits(e: Rep[ExpTuple]) = e._3
  def subseq(e: Rep[ExpTuple]) = e._4

  private var _nextKey = 0
  private var _expMap = Map[Exp, ExpKey]()
  val table = SetTable.empty[ExpTuple]


  def nextKey() = {
    val k = _nextKey
    _nextKey += 1
    k
  }

  import scala.language.implicitConversions
  implicit def constructable(k: ExpKind) = new Constructable(k)
  class Constructable(k: ExpKind) {
    def apply(): Exp = Exp(k, scala.Seq(), scala.Seq())
    def apply(l: Lit, lits: Lit*): Exp = Exp(k, l +: scala.Seq(lits:_*), scala.Seq())
    def apply(e: Exp, sub: Exp*): Exp = Exp(k, scala.Seq(), e +: scala.Seq(sub:_*))
  }
}

import Exp._
case class Exp(kind: ExpKind, lits: Seq[Lit], sub: Seq[Exp]) {
  def key = _expMap(this)

  def insert: ExpKey = {
    val subkeys = sub map (_.insert)
    _expMap.get(this) match {
      case scala.Some(key) => key
      case scala.None =>
        val key = nextKey()
        table += ((key, kind, lits, subkeys))
        _expMap += this -> key
        key
    }
  }

  def replace(e: Exp): Unit = {
    if (kind == e.kind && lits == e.lits && sub.length == e.sub.length)
      for (i <- 0 until sub.length) sub(i).replace(e.sub(i))
    else {
      val thiskey = key
      val oldsubkeys = e.sub map (_.key)
      val newsubkeys = e.sub map (_.insert) // will be shared with previous subtrees due to _expMap
      table ~= (thiskey, kind, lits, oldsubkeys) -> (thiskey, e.kind, e.lits, newsubkeys)
      _expMap += e -> thiskey
    }
  }

  override def toString = {
    val subs = lits.map(_.toString) ++ sub.map(_.toString)
    val subssep = subs.flatMap(s => Seq(", ", s)).tail
    val substring = subssep.foldLeft("")(_+_)
    val keyString = _expMap.get(this) match {case None => ""; case Some(k) => s"$k@"}
    s"$keyString$kind($substring)"
  }
}
