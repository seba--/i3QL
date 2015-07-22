package idb.remote

import idb.observer.Observable

import scala.collection.mutable
import scala.pickling._
import scala.pickling.Defaults._

object Picklers {

  val hashSetTag = FastTypeTag[mutable.Set[Observable[Any]]]
  val hashSetPickler = implicitly[Pickler[mutable.Set[Observable[Any]]]]

  implicit def observablePickler[T] = new Pickler[Observable[T]] {
    def pickle(obs: Observable[T], builder: PBuilder): Unit = {
      builder.beginEntry(obs)

      builder.putField("observers",
        b => {
          b.hintTag(hashSetTag)
          hashSetPickler.pickle(obs.observers.asInstanceOf[mutable.Set[Observable[Any]]], b)
        }
      )

      builder.endEntry()
    }

    def tag = FastTypeTag.materializeFastTypeTag[Observable[T]]
  }

}
