package idb.observer

import scala.pickling._
import scala.pickling.Defaults._

final class CountingObserver extends Observer[Any] {
  type V = Any

  private var _msgCount = 0
  def msgCount = _msgCount

  private var _dataCount = 0
  def dataCount = _dataCount

  def pickler = implicitly[Pickler[CountingObserver]]

  override def updated(oldV: V, newV: V) = {
    _msgCount += 1
    _dataCount += 2
  }

  override def endTransaction() = {
    _msgCount += 1
  }

  override def removed(v: V) = {
    _msgCount += 1
    _dataCount += 1
  }

  override def added(v: V) = {
    _msgCount += 1
    _dataCount += 1
  }

  override def addedAll(vs: Seq[V]) = {
    _msgCount += 1
    _dataCount += vs.size
  }

  override def removedAll(vs: Seq[V]) = {
    _msgCount += 1
    _dataCount += vs.size
  }
}
