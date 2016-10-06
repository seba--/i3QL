package sae.typecheck

import idb.observer.Observer
import idb.syntax.iql.SELECT
import Exp.ExpKind
import TypeStuff._



/**
 * Created by seba on 28/10/14.
 *
 * Use root to ensure fixed rootkey for all programs
 */
class Root() {
  var rootNode: Exp = Exp(Root.Root, Seq(), Seq())
  val key = Exp.prefetchKey
  def insert = rootNode.insert

  def reset(): Unit = {
    val oldSubkeys = rootNode.subkeys
    if (!rootNode.sub.isEmpty)
      rootNode.sub(0).remove

    val newRootNode = Exp(Root.Root, Seq(), Seq())
    Exp.updateExp(rootNode, newRootNode, oldSubkeys, Seq())
    rootNode = newRootNode
  }

  def set(e: Exp) = {
    val oldSubkeys = rootNode.subkeys
    if (!rootNode.sub.isEmpty)
      rootNode.sub(0).remove
    val (_,ts) = e.insertCollect

    val newRootNode = Exp(Root.Root, Seq(), Seq(e))
    Exp.updateExp(rootNode, newRootNode, oldSubkeys, Seq(e.key))

    rootNode = newRootNode
    () => Exp.fireAdd(ts)
  }

  def update(e: Exp): Unit = {
    val newRootNode = Exp(Root.Root, Seq(), Seq(e))
    rootNode.replaceWith(newRootNode)
    rootNode = newRootNode
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

  case object Root extends ExpKind
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

  def apply[U: Manifest](types: Relation[(Exp.ExpKey, U)], f: Rep[U => Either[Type, TError]]): Root = {
    val root = new Root()
    val rootKey: Rep[Int] = root.key
    val rootQuery = SELECT ((t: Rep[(Exp.ExpKey, U)]) => f(t._2)) FROM types WHERE (t => t._1 == rootKey)
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