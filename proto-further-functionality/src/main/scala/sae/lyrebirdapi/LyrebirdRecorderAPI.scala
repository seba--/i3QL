package sae.lyrebirdapi

import java.io.File
import de.tud.cs.st.lyrebird.replayframework.Replay
import sae.bytecode.Database

/**
 * A adapter to work with Lyrebird.Recorder.
 *
 * param location: folder of the default package.
 * param db : Database to process bytecode changes
 * author: Malte V
 */
class LyrebirdRecorderAPI(val location: File, val db: Database) {
  private val replay = new Replay(location)
  private val eventSets = replay.getAllEventSets
  private var idx = 0

  /**
   * Processes the next EventSet
   */
  def processNext() {
    replay.processEventSet(eventSets(idx), db.getAddClassFileFunction, db.getRemoveClassFileFunction)
    idx += 1
  }

  /**
   * Returns true if the location contain a further EventSet
   */
  def hasNext() = {
    idx < eventSets.size
  }

  /**
   * Processes the next x event sets from the current position onwards
   */
  def processNextX(x: Int) = {

    for (i <- 0 to x) {
      replay.processEventSet(eventSets(idx), db.getAddClassFileFunction, db.getRemoveClassFileFunction)
      idx += 1
    }
  }

  /**
   * Processes all eventsets with a timestamp <= time from the current position onwards
   */
  def processUntil(time: Long) {
    while (eventSets(idx).eventFiles.head.eventTime <= time) {
      replay.processEventSet(eventSets(idx), db.getAddClassFileFunction, db.getRemoveClassFileFunction)
      idx += 1
    }
  }


}
