package sae.test

import java.io.File
import sae.lyrebirdapi._
import java.lang.Long
import sae.LazyView
import sae.bytecode.{MaterializedDatabase, BytecodeDatabase}

/**
 * Date: 16.06.11
 * Time: 00:52
 * @author Malte V
 */

class EventSetTestHelper {
  var db: MaterializedDatabase = null
  var lyrebird: LyrebirdRecorderAPI = null

  def init(location: File) {
    db = new MaterializedDatabase()
    lyrebird = new LyrebirdRecorderAPI(location, db)
  }


  def registerQuery[T <: AnyRef](queries: MaterializedDatabase => LazyView[T]): LazyView[T] = {
    queries(db)
  }
  def processNextAndTest(test: => Unit) {
    if (lyrebird.hasNext) {
      lyrebird.processNext()
    } else {
      throw new Error()
    }
    test
  }

  def print(f : MaterializedDatabase => Unit){
    f(db)
  }

  def processXAndTest(x: Int, test: Unit => _) {
    lyrebird.processNextX(x)
    test
  }

  def processRestAndTest(test:  => Unit) {
    while (lyrebird.hasNext()) {
      lyrebird.processNext()
    }
    test
  }

  def processToAndTest(time: Long, test:  => Unit) {
    lyrebird.processUntil(time)
    test
  }

}

object EventSetHalper extends EventSetTestHelper