package sae.lyrebirdapi

import java.io.File
import de.tud.cs.st.lyrebird.replayframework.Replay
import sae.bytecode.{MaterializedDatabase, BytecodeDatabase}

/**
 * Date: 16.06.11
 * Time: 00:33
 * @author Malte V
 */

class ProcessLyrebirdResources(val location: File, val db: MaterializedDatabase) {
  private val replay = new Replay(location)
  private val eventSets = replay.getAllEventSets
  private var idx = 0

  def processNext() {
    replay.processEventSet(eventSets(idx), db.getAddClassFileFunction, db.getRemoveClassFileFunction)
    idx += 1
  }

  def hasNext() = {
    idx < eventSets.size
  }

  def processNextX(x: Int) = {

    for (i <- 0 to x) {
      replay.processEventSet(eventSets(idx), db.getAddClassFileFunction, db.getRemoveClassFileFunction)
      idx += 1
    }
  }

  def processUntil(time: Long) {
    while (eventSets(idx).eventFiles.head.eventTime <= time) {
      replay.processEventSet(eventSets(idx), db.getAddClassFileFunction, db.getRemoveClassFileFunction)
      idx += 1
    }
  }


}
