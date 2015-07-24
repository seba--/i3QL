package idb.remote

import idb.SetTable
import idb.observer.{Observable, Observer}

import scala.collection.mutable
import scala.collection.generic.CanBuildFrom

import scala.pickling._
import scala.pickling.Defaults._
import scala.pickling.pickler.AllPicklers
import scala.pickling.static._

import scala.language.implicitConversions

trait DynPickling {
  type This
  def pickler: Pickler[This]
}
object DynPickling {
  implicit def picklerDispatch(o: DynPickling): Pickler[o.This] = o.pickler
}

class ObserverPicklerUnpickler[T] extends Pickler[Observer[T]] with Unpickler[Observer[T]] {
  def pickle(obs: Observer[T], builder: PBuilder): Unit = ???
  def unpickle(tag: String, reader: PReader): Any = ???
  def tag: FastTypeTag[Observer[T]] = FastTypeTag.materializeFastTypeTag[Observer[T]]
}

object Picklers extends App {

  import DynPickling._
  import scala.pickling.json._
  val v = SetTable.empty[Int]
  println(PickleOps(v).pickle)

  implicit val obsPickler: Pickler[Observer[Any]] = new ObserverPicklerUnpickler[Any]
  implicit val obsUnpickler: Unpickler[Observer[Any]] = new ObserverPicklerUnpickler[Any]
  implicit val obsTag = FastTypeTag.materializeFastTypeTag[Observer[Any]]

  implicit val setTag = FastTypeTag.materializeFastTypeTag[mutable.Set[Observer[Any]]]
  implicit val cbf    = implicitly[CanBuildFrom[mutable.Set[Observer[Any]], Observer[Any], mutable.Set[Observer[Any]]]]

  val setPickler: Pickler[mutable.Set[Observer[Any]]] = AllPicklers.mutableSetPickler(obsTag, obsPickler, obsUnpickler, setTag, cbf)

  implicit def observablePickler[T] = new Pickler[Observable[T]] {
    def pickle(obs: Observable[T], builder: PBuilder): Unit = {
      builder.beginEntry(obs)

      builder.putField("observers",
        b => {
          b.hintTag(setTag)
          setPickler.pickle(obs.observers, b)
        }
      )

      builder.endEntry()
    }

    def tag = FastTypeTag.materializeFastTypeTag[Observable[T]]
  }

}
