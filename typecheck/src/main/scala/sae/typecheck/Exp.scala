package sae.typecheck

import idb.syntax.iql.IR.Rep
import idb.{BagTable, SetTable}

/**
 * Created by seba on 27/10/14.
 */
object Exp {
  var change: Long = System.nanoTime

  def log(s: String) = {
    val newchange = System.nanoTime
    if(Util.LOG_TABLE_OPS) {
      println(s"Time since last table op ${(newchange - change)/1000000.0}ms")
      println(s)
    }
    change = newchange
  }

  type Lit = Any
  abstract class ExpKindTag
  type ExpKind = (ExpKindTag, Int)
  type ExpKey = Int
  type Parent = ExpKey
  type Position = Int
  type ExpTuple = (ExpKey, ExpKind, Seq[Lit], Parent, Position)
  def id(e: Rep[ExpTuple]) = e._1
  def kind(e: Rep[ExpTuple]) = e._2
  def arity(e: Rep[ExpTuple]) = e._2._2
  def lits(e: Rep[ExpTuple]) = e._3
  def parent(e: Rep[ExpTuple]) = e._4
  def pos(e: Rep[ExpTuple]) = e._5

  private var _nextKey = 0

  val table = BagTable.empty[ExpTuple]

//  def updateExp(old: Exp, e: Exp, oldsubkeys: Seq[ExpKey], newsubkeys: Seq[ExpKey]): ExpKey = {
//    val oldkey = old.key
//    val oldcount = old.count
//    val newcount = e.count
//
//
//    if (oldcount == 1 && newcount == 0) {
//      val newkey = oldkey
//      val same = old.kind == e.kind && old.lits == e.lits && oldsubkeys == newsubkeys
//      if (!same) {
//        log(s"update ($oldkey, ${old.kind}, ${old.lits}, $oldsubkeys)*${oldcount} -> ($newkey, ${e.kind}, ${e.lits}, $newsubkeys)*${newcount}")
//        table ~= (oldkey, old.kind, old.lits, oldsubkeys) ->(newkey, e.kind, e.lits, newsubkeys)
//      }
//      e.key = newkey
//    }
//    else if (oldcount == 1 && newcount >= 1) {
//      log(s"remove ($oldkey, ${old.kind}, ${old.lits}, $oldsubkeys)*${oldcount}")
//      table -= (oldkey, old.kind, old.lits, oldsubkeys)
//    }
//    else if (oldcount > 1 && newcount == 0) {
//      val newkey = nextKey()
//      log(s"insert ($newkey, ${e.kind}, ${e.lits}, $newsubkeys)")
//      table += (newkey, e.kind, e.lits, newsubkeys)
//      e.key = newkey
//    }
//    else if (oldcount > 1 && newcount >= 1) {
//      // do nothing
//    }
//    old.decCount
//    e.incCount
//    //    println(s"updated ($oldkey, ${old.kind}, ${old.lits}, $oldsubkeys)*${old.count} -> (${e.key}, ${e.kind}, ${e.lits}, $newsubkeys)*${e.count}")
//    e.key
//  }

  def fireAdd(ts: Seq[ExpTuple]): Unit = {
    println(s"fire batch insertion, size ${ts.size}")
    table ++= ts
  }

  def nextKey() = {
    val k = _nextKey
    _nextKey += 1
    k
  }
  def prefetchKey = _nextKey

  import scala.language.implicitConversions
  implicit def constructable(k: ExpKindTag) = new Constructable(k)
  class Constructable(k: ExpKindTag) {
    def apply(): Exp = Exp((k,0), scala.Seq(), scala.Seq())
    def apply(l: Lit, sub: Exp*): Exp = Exp((k, sub.size), scala.Seq(l), scala.Seq(sub:_*))
    def apply(e: Exp, sub: Exp*): Exp = Exp((k, sub.size + 1), scala.Seq(), e +: scala.Seq(sub:_*))
    def apply(lits: Seq[Lit], sub: Seq[Exp]): Exp = Exp((k, sub.size), lits, sub)
  }
}

import Exp._
case class Exp(kind: ExpKind, lits: Seq[Lit], sub: Seq[Exp]) {
  var _key = -1
  var _count = 0
  var _delegate: Exp = null

  var parent = -1
  var pos = -1

  def key: ExpKey = if (_delegate != null) _delegate.key else _key
  def key_=(k: ExpKey): Unit = if (_delegate != null) _delegate.key = k else _key = k
  def count: Int = if (_delegate != null) _delegate.count else _count
  def decCount(): Unit = if (_delegate != null) _delegate.decCount() else _count -= 1
  def incCount(): Unit = if (_delegate != null) _delegate.incCount() else _count += 1

  override def equals(a: Any) = a.isInstanceOf[Exp] && {
    val e = a.asInstanceOf[Exp]
    if (count > 0 && e.count > 0)
      key == e.key
    else
      kind == e.kind && lits == e.lits && sub == e.sub
  }

  def subkeys = sub map (_.key)

  def subexps = {
    val set = collection.mutable.ArrayBuffer[Exp]()
    addSubexps(set)
    set
  }
  def addSubexps(set: collection.mutable.ArrayBuffer[Exp]): Unit = {
    sub foreach (_.addSubexps(set))
    set += this
  }


  def insert(parent: ExpKey, pos: Int): Unit = {
    val ts = insertCollect(parent, pos)
    println(s"batch insertion, size ${ts.size}")
    table ++= ts
  }
  def insertCollect(parent: ExpKey, pos: Int) = {
    this.parent = parent
    this.pos = pos
    _insertCollect
  }
  private def _insertCollect: Seq[ExpTuple] = {
    var added = Seq[ExpTuple]()

    val k = flatInsertCollect match {
      case None => key
      case Some(tuple) =>
        added = tuple +: added
        tuple._1
    }

    for (i <- 0 until sub.size) {
      val s = sub(i)
      s.parent = k
      s.pos = i
      added = added ++ s._insertCollect
    }

    added
  }

  private def flatInsertCollect: Option[ExpTuple] =
    if (count > 0) {
      //      throw new RuntimeException(s"Attempted double insert of exp $key->$this")
      incCount()
      None
    }
    else {
      key = nextKey()
      incCount()
      log(s"insert ${(key, kind, lits, parent, pos)}")
      Some((key, kind, lits, parent, pos))
    }

  private def flatInsert: Unit = {
    flatInsertCollect match {
      case None => {}
      case Some(t) => table += t
    }
  }

  def remove(parent: ExpKey, pos: Int): Unit = {
    val ts = _removeCollect
    println(s"batch removal, size ${ts.size}")
    table --= ts
  }
  def removeCollect(parent: ExpKey, pos: Int) = {
    this.parent = parent
    this.pos = pos
    _removeCollect
  }
  private def _removeCollect: Seq[ExpTuple] = {
    var removed = Seq[ExpTuple]()

    val k = flatRemoveCollect match {
      case None => key
      case Some(tuple) =>
        removed = tuple +: removed
        tuple._1
    }

    for (i <- 0 until sub.size) {
      val s = sub(i)
      s.parent = k
      s.pos = i
      removed = removed ++ s._removeCollect
    }

    removed
  }

  def flatRemoveCollect: Option[ExpTuple] = {
    decCount()
    if (count == 0) {
      log(s"remove ${(key, kind, lits, parent, pos)}")
      Some((key, kind, lits, parent, pos))
    }
    else
      None
  }

  private def flatRemove: Unit = {
    flatRemoveCollect match {
      case None => {}
      case Some(t) => table -= t
    }
  }


  def replaceWith(e: Exp): Unit = {
    val removed = this._removeCollect
    val added = e.insertCollect(this.parent, this.pos)

    // TODO more incremental update
    table --= removed
    table ++= added
  }

  override def toString = {
    val subs = lits.map(_.toString) ++ sub.map(_.toString)
    val subssep = if (subs.isEmpty) subs else subs.flatMap(s => Seq(", ", s)).tail
    val substring = subssep.foldLeft("")(_+_)
    val keyString = "" //getkey match {case None => ""; case Some(k) => s"$k@"}
    s"$keyString${kind._1}($substring)"
  }
}


case object Num extends ExpKindTag
case object String extends ExpKindTag
case object Add extends ExpKindTag
case object Mul extends ExpKindTag
case object Var extends ExpKindTag
case object Abs extends ExpKindTag
case object App extends ExpKindTag
case object If0 extends ExpKindTag
case object Fix extends ExpKindTag


class ExtHashSet[A] extends collection.mutable.HashSet[A] {
  override def findEntry(elem: A) = super.findEntry(elem)
}