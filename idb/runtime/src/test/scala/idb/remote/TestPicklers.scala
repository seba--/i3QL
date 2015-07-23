package idb.remote

import org.junit.Test

import scala.pickling._
import scala.pickling.json._
import scala.pickling.Defaults._

import idb.observer._

class TestPicklers {

  @Test
  def pickleCountingObserver() {
    val compiledClientCounts = new CountingObserver

    compiledClientCounts.added(5)
    implicit val pickler = compiledClientCounts.pickler
    val p = compiledClientCounts.pickle

    val expected = """JSONPickle({
      |  "$type": "idb.observer.CountingObserver",
      |  "_msgCount": 1,
      |  "_dataCount": 1
      |})""".stripMargin

    assert(p.toString == expected)

    val up = p.unpickle[CountingObserver]
    assert(up.msgCount == 1)
  }

}
