package sae.profiler.util

import de.tud.cs.st.lyrebird.replayframework._
import sae.bytecode.{MaterializedDatabase, Database, BytecodeDatabase}
import java.io.{OutputStream, PrintWriter, File}
import sae.profiler._

class EventProfileHelper(val location: File, val registerFunction: Database => Unit) {
  private var db = BATDatabaseFactory.create()
  private var replayData = new MaterializedDatabase()
  private val replay = new Replay(location)
  private val allEventSets = replay.getAllEventSets
  private var idx = 0
  private var buffer = new DatabaseBuffer(db)
  // private val writer = new PrintWriter(, true)

  def init {
    System.gc()
    db = new BytecodeDatabase
    buffer = new DatabaseBuffer(db)
    registerFunction(buffer)

  }

  def size = {
    allEventSets.size
  }

  def beforeMeasurement(i: Int): DatabaseBuffer = {
    buffer.reset()
    replay.processEventSet(allEventSets(i), db.getAddClassFileFunction, db.getRemoveClassFileFunction)

    buffer
  }

  def getInfo(i: Int): String = {
    val res : String = "t:" + allEventSets(i).eventFiles.size +
      "\ta:" + allEventSets(i).eventFiles./:(0)((x: Int, y: Event) => {
      if (y.eventType == EventType.ADDED) x + 1 else x
    }) +
      "\tc:" + allEventSets(i).eventFiles./:(0)((x: Int, y: Event) => {
      if (y.eventType == EventType.CHANGED) x + 1 else x
    }) +
      "\tr:" + allEventSets(i).eventFiles./:(0)((x: Int, y: Event) => {
      if (y.eventType == EventType.REMOVED) x + 1 else x})

    res
  }


  def applyNext() {
    buffer.reset()
    if (idx < allEventSets.size) {
      replay.processEventSet(allEventSets(idx), db.getAddClassFileFunction, db.getRemoveClassFileFunction)
      idx += 1
    } else {
      throw new Error()
    }
  }

  def hasNext: Boolean = {
    idx < allEventSets.size
  }

  def applyAll(text: List[String]) {
//    var i = 0
//    implicit val times = 1
//    while (this.hasNext) {
//      if (i < text.size) {
//        applyNext()
//        Write(text(i), Profiler.profile(buffer.replay()))
//      } else {
//        applyNext()
//        Write("Profiling EventSet Number: " + i, Profiler.profile(buffer.replay()))
//      }
//      i += 1
//    }
  }

  def applyAll() {
    applyAll(List[String]())
  }

  def getBuffer: DatabaseBuffer = {
    buffer
  }
}