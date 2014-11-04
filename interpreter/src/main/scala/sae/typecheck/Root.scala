package sae.typecheck

import idb.observer.Observer
import idb.syntax.iql.IR._
import idb.syntax.iql.SELECT
import sae.typecheck.ConstraintTypecheck.TVar
import sae.typecheck.Exp.ExpKind
import sae.typecheck.Type._

/**
 * Created by seba on 28/10/14.
 *
 * Use root to ensure fixed rootkey for all programs
 */
class Root[V](private var rootNode: Exp) {
  def set(e: Exp): Unit = {
    val newroot = Root.Root(e)
    rootNode.replaceWith(newroot)
    rootNode = newroot
  }

  var Type: V = _
  def store(t: V) = Type = t
}

object Root {
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

  def apply[U: Manifest, V: Manifest](types: Relation[(Exp.ExpKey, U)], f: Rep[U => V]): Root[V] = {
    val rootNode = Exp(Root, scala.Seq(), scala.Seq())
    val root = new Root[V](rootNode)
    val rootKey: Rep[Int] = Exp.prefetchKey
    val rootQuery = SELECT ((t: Rep[(Exp.ExpKey, U)]) => f(t._2)) FROM types WHERE (t => t._1 == rootKey)
    rootQuery.addObserver(new Observer[V] {
      override def updated(oldV: V, newV: V): Unit = root.store(newV)
      override def removed(v: V): Unit = root.store(null.asInstanceOf[V])
      override def added(v: V): Unit = root.store(v)
      override def endTransaction(): Unit = {}
    })
    rootNode.insert
    root
  }
}