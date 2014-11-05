package sae.typecheck

import idb.observer.Observer
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
  private[this] var _expMap = Map[Exp, (ExpKey, Int)]()
  private def bindExp(e: Exp, k: ExpKey): Unit = {
    e.key = k
    _expMap.get(e) match {
      case None => _expMap += e -> (k, 1)
      case Some((`k`, count)) => _expMap += e -> (k, count+1)
      case Some((k2, count)) => throw new IllegalStateException(s"conflicting key bindings $k and $k2 for expression $e")
    }
  }
  private def unbindExp(e: Exp): Unit = {
    e.key = -1
    _expMap.get(e) match {
      case None => {}
      case Some((k, 1)) => _expMap -= e
      case Some((k, count)) => _expMap += e -> (k, count-1)
    }
  }
  private def lookupExp(e: Exp) = _expMap.get(e).map(_._1)
  private def lookupExpCount(e: Exp) = _expMap.get(e).map(_._2).getOrElse(0)
  private def lookupExpWithCount(e: Exp) = _expMap.get(e)

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
  def getkey = lookupExp(this)

  override def equals(a: Any) = a.isInstanceOf[Exp] && {
    val e = a.asInstanceOf[Exp]
    if (key >= 0 && e.key >= 0)
      key == e.key
    else
      kind == e.kind && lits == e.lits && sub == e.sub
  }

  def insert: ExpKey = {
    val subkeys = sub map (_.insert)
    getkey match {
      case Some(key) =>
        bindExp(this, key)
        key
      case None =>
        val key = nextKey()
        log(s"insert ${(key, kind, lits, subkeys)}")
        table += ((key, kind, lits, subkeys))
        bindExp(this, key)
        key
    }
  }

  def remove: ExpKey = {
    val subkeys = sub map (_.remove)
    val Some((key, count)) = lookupExpWithCount(this)
    unbindExp(this)
    if (count == 1) {
      log(s"remove ${(key, kind, lits, subkeys)}")
      table -= ((key, kind, lits, subkeys))
    }
    key
  }

  def updateExp(old: Exp, e: Exp, oldsubkeys: Seq[ExpKey], newsubkeys: Seq[ExpKey]): ExpKey = {
    val Some((oldkey, oldcount)) = lookupExpWithCount(old)
    val newkey = e.getkey.getOrElse(if (oldcount == 1) oldkey else nextKey())
    val newcount = lookupExpCount(e)
    unbindExp(old)
    bindExp(e, newkey)
    if (old.kind == e.kind && old.lits == e.lits && oldsubkeys == newsubkeys) {
      // terms are flat-equal, do nothing
    }
    else if (oldcount == 1 && newcount == 0) {
      log(s"update  ($oldkey, $kind, $lits, $oldsubkeys)*${lookupExpCount(old)} -> ($newkey, ${e.kind}, ${e.lits}, $newsubkeys)*${lookupExpCount(e)}")
      table ~= (oldkey, old.kind, old.lits, oldsubkeys) -> (newkey, e.kind, e.lits, newsubkeys)
//      table -= (oldkey, old.kind, old.lits, oldsubkeys)
//      table += (newkey, e.kind, e.lits, newsubkeys)
    }
    else if (oldcount == 1 && newcount >= 1) {
      log(s"remove ($oldkey, $kind, $lits, $oldsubkeys)*${lookupExpCount(old)}")
      table -= (oldkey, old.kind, old.lits, oldsubkeys)
    }
    else if (oldcount > 1 && newcount == 0) {
      log(s"insert ($newkey, ${e.kind}, ${e.lits}, $newsubkeys)")
      table += (newkey, e.kind, e.lits, newsubkeys)
    }
    else if (oldcount > 1 && newcount >= 1) {
      // do nothing
    }
//    println(s"updated ($oldkey, $kind, $lits, $oldsubkeys)*${lookupExpCount(old)} -> ($newkey, ${e.kind}, ${e.lits}, $newsubkeys)*${lookupExpCount(e)}")
    newkey
  }

  def replaceWith(e: Exp): ExpKey = {
    if (kind == e.kind && lits == e.lits && sub.length == e.sub.length) {
      // same structure, just replace subexpressions
      val oldsubkeys = sub map (_.key)
      val newsubkeys = sub.zip(e.sub).map(p => p._1.replaceWith(p._2))
      updateExp(this, e, oldsubkeys, newsubkeys)
//      else
//        getkey match {
//          case None => throw new RuntimeException(s"$this does not have key")
//          case Some(k) => k
//        }
    }
    else {
      // different structure, remove old subexpressions and insert new subexpressions
      val newsubkeys = e.sub map (_.insert) // will be shared with previous subtrees if already present
      val oldsubkeys = sub map (_.remove)
      updateExp(this, e, oldsubkeys, newsubkeys)
    }
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
