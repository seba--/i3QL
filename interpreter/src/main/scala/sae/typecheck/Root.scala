package sae.typecheck

import idb.observer.Observer
import idb.syntax.iql.IR._
import idb.syntax.iql.SELECT
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

  var Type: V = scala.Right("Uninitialized root")
  def store(t: Either[Type, TError]) = t match {
    case scala.Left(Root.TRoot(t)) => Type = scala.Left(t)
    case scala.Right(msg) => Type = scala.Right(msg)
    case _ => throw new RuntimeException(s"Unexpected root type $t")
  }
}

object Root {
  case object Root extends ExpKind
  case class TRoot(t: Type) extends Type

  def apply[V](types: Relation[V]): Root[V] = {
    val rootNode = Exp(Root, scala.Seq(), scala.Seq())
    val root = new Root[V](rootNode)
    val rootKey: Rep[Int] = Exp.prefetchKey
    val rootQuery = SELECT ((t: Rep[TypeTuple]) => t._2) FROM types WHERE (t => tid(t) == rootKey)
    rootQuery.addObserver(new Observer[Either[Type, TError]] {
      override def updated(oldV: Either[Type, TError], newV: Either[Type, TError]): Unit = root.store(newV)
      override def endTransaction(): Unit = {}
      override def removed(v: Either[Type, TError]): Unit = root.store(scala.Right("Uninitialized root"))
      override def added(v: Either[Type, TError]): Unit = root.store(v)
    })
    rootNode.insert
    root
  }
}