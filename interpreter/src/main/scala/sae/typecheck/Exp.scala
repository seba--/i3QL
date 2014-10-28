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
  private[this] var _expMap = Map[Exp, (ExpKey, Int)]()
  private def bindExp(e: Exp, k: ExpKey): Unit = {
    _expMap.get(e) match {
      case None => _expMap += e -> (k, 1)
      case Some((`k`, count)) => _expMap += e -> (k, count+1)
      case Some((k2, count)) => throw new IllegalStateException(s"conflicting key bindings $k and $k2 for expression $e")
    }
  }
  private def unbindExp(e: Exp): Unit = {
    _expMap.get(e) match {
      case None => {}
      case Some((k, 1)) => _expMap -= e
      case Some((k, count)) => _expMap += e -> (k, count-1)
    }
  }
  private def lookupExp(e: Exp) = _expMap.get(e).map(_._1)
  private def lookupExpCount(e: Exp) = _expMap.get(e).map(_._2).getOrElse(0)
  private def lookupExpWithCount(e: Exp) = _expMap.get(e)

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
  def key = lookupExp(this).get
  def getkey = lookupExp(this)

  def insert: ExpKey = {
    val subkeys = sub map (_.insert)
    getkey match {
      case None =>
        val key = nextKey()
        table += ((key, kind, lits, subkeys))
        bindExp(this, key)
        key
      case Some(key) =>
        bindExp(this, key)
        key
    }
  }

  def remove: ExpKey = {
    val subkeys = sub map (_.remove)
    val Some((key, count)) = lookupExpWithCount(this)
    unbindExp(this)
    if (count == 1)
      table -= ((key, kind, lits, subkeys))
    key
  }

  def updateExp(old: Exp, e: Exp, oldsubkeys: Seq[ExpKey], newsubkeys: Seq[ExpKey]): ExpKey = {
    val Some((oldkey, oldcount)) = lookupExpWithCount(old)
    val newkey = if (oldcount == 1) oldkey else nextKey()
    unbindExp(old)
    bindExp(e, newkey)
    if (!(oldkey == newkey && oldsubkeys == newsubkeys))
      table ~= (oldkey, old.kind, old.lits, oldsubkeys) -> (newkey, e.kind, e.lits, newsubkeys)
    newkey
  }

  def replaceWith(e: Exp): ExpKey = {
    if (kind == e.kind && lits == e.lits && sub.length == e.sub.length) {
      if (sub == e.sub)
        // equal to old expression, no change required
        key
      else {
        // same structure, just replace subexpressions
        val oldsubkeys = sub map (_.key)
        val newsubkeys = sub.zip(e.sub).map(p => p._1.replaceWith(p._2))
        updateExp(this, e, oldsubkeys, newsubkeys)
      }
    }
    else {
      // different structure, remove old subexpressions and insert new subexpressions
      val oldsubkeys = sub map (_.remove)
      val newsubkeys = e.sub map (_.insert) // will be shared with previous subtrees due to _expMap
      updateExp(this, e, oldsubkeys, newsubkeys)
    }
  }

  override def toString = {
    val subs = lits.map(_.toString) ++ sub.map(_.toString)
    val subssep = subs.flatMap(s => Seq(", ", s)).tail
    val substring = subssep.foldLeft("")(_+_)
    val keyString = getkey match {case None => ""; case Some(k) => s"$k@"}
    s"$keyString$kind($substring)"
  }
}
