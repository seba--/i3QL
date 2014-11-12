package sae.typecheck

import idb.observer.Observer
import idb.syntax.iql.SELECT
import sae.typecheck.Exp.{ExpKindTag, ExpKind}
import TypeStuff._



/**
 * Created by seba on 28/10/14.
 *
 * Use root to ensure fixed rootkey for all programs
 */
class Root() {
  def insert {
    val e = Exp((Root.Root, 1), Seq(), Seq())
    e.insert(-1, -1)
    assert (rootKey == e.key, s"Expected prefetched rootKey $rootKey to equal actual root key ${e.key}")
  }
  val rootKey = Exp.prefetchKey
  var topexp: Option[Exp] = None

  def reset(): Unit = {
    if (topexp.isDefined)
      topexp.get.remove(rootKey, 0)
  }

  def set(e: Exp) = {
    if (topexp.isDefined)
      topexp.get.remove(rootKey, 0)

    val ts = e.insertCollect(rootKey, 0)
    topexp = Some(e)

    () => Exp.fireAdd(ts)
  }

  def update(e: Exp): Unit = {
    if (topexp.isEmpty)
      set(e)
    else {
      val old = topexp.get
      old.replaceWith(e)
      topexp = Some(e)
    }
  }

  var Types: Seq[Either[Type, TError]] = scala.Seq()
  def Type = Types.size match {
    case 0 => scala.Right("Unitialized root type")
    case 1 => Types.head
    case _ => scala.Right(s"Ambiguous root type: $Types")
  }
  def store(t: Either[Type, TError]) = Types = t+:Types
  def unstore(t: Either[Type, TError]) = Types = Types diff scala.Seq(t)
}

object Root {
  import idb.syntax.iql.IR._

  case object Root extends ExpKindTag
  case class TRoot(t: Type) extends Type {
    def rename(ren: Map[Symbol, Symbol]) = TRoot(t.rename(ren))
    def subst(s: TSubst) = TRoot(t.subst(s))
    def unify(other: Type) = other match {
      case TRoot(t2) => t.unify(t2)
      case TVar(x) => scala.Some(Map(x -> this))
      case _ => None
    }
    def vars = t.vars
  }

  def apply[U : Manifest](types: Relation[(Exp.Parent, Exp.Position, U)], f: Rep[U => Either[Type, TError]]): Root = {
    val root = new Root()
    val rootKey: Rep[Int] = root.rootKey
    val rootQuery = SELECT ((t: Rep[(Exp.Parent, Exp.Position, U)]) => f(t._3)) FROM types WHERE (t => t._1 == rootKey)
    rootQuery.addObserver(new Observer[Either[Type, TError]] {
      override def updated(oldV: Either[Type, TError], newV: Either[Type, TError]): Unit = {root.unstore(oldV); root.store(newV)}
      override def removed(v: Either[Type, TError]): Unit = {root.unstore(v)}
      override def removedAll(vs: Seq[Either[Type, TError]]) = for (v <- vs) removed(v)
      override def added(v: Either[Type, TError]): Unit = root.store(v)
      override def addedAll(vs: Seq[Either[Type, TError]]) = for (v <- vs) added(v)
      override def endTransaction(): Unit = {}
    })
    root.insert
    root
  }
}