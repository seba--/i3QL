package sae.typecheck

import idb.syntax.iql.IR.Rep
import idb.{BagTable, SetTable}

/**
 * Created by seba on 27/10/14.
 */
object ExpShared {
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
  private[this] var _expMap = Map[ExpShared, (ExpKey, Int)]()
  private def bindExp(e: ExpShared, k: ExpKey): Unit = {
    _expMap.get(e) match {
      case None => _expMap += e -> (k, 1)
      case Some((`k`, count)) => _expMap += e -> (k, count+1)
      case Some((k2, count)) => throw new IllegalStateException(s"conflicting key bindings $k and $k2 for expression $e")
    }
  }
  private def unbindExp(e: ExpShared): Unit = {
    _expMap.get(e) match {
      case None => {}
      case Some((k, 1)) => _expMap -= e
      case Some((k, count)) => _expMap += e -> (k, count-1)
    }
  }
  private def lookupExp(e: ExpShared) = _expMap.get(e).map(_._1)
  private def lookupExpCount(e: ExpShared) = _expMap.get(e).map(_._2).getOrElse(0)
  private def lookupExpWithCount(e: ExpShared) = _expMap.get(e)

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
    def apply(): ExpShared = ExpShared(k, scala.Seq(), scala.Seq())
    def apply(l: Lit, sub: ExpShared*): ExpShared = ExpShared(k, scala.Seq(l), scala.Seq(sub:_*))
    def apply(e: ExpShared, sub: ExpShared*): ExpShared = ExpShared(k, scala.Seq(), e +: scala.Seq(sub:_*))
    def apply(lits: Seq[Lit], sub: Seq[ExpShared]): ExpShared = ExpShared(k, lits, sub)
  }
}

import ExpShared._
case class ExpShared(kind: ExpKind, lits: Seq[Lit], sub: Seq[ExpShared]) {
  def key = lookupExp(this).get
  def getkey = lookupExp(this)

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

  def updateExp(old: ExpShared, e: ExpShared, oldsubkeys: Seq[ExpKey], newsubkeys: Seq[ExpKey]): ExpKey = {
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

  def replaceWith(e: ExpShared): ExpKey = {
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