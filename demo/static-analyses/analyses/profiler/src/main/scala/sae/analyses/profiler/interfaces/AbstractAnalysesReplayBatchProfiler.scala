package sae.analyses.profiler.interfaces

import java.io.FileInputStream

import sae.analyses.profiler.util.{ReplayEvent, DatabaseReader}

/**
 * @author Mirko Köhler
 */
trait AbstractAnalysesReplayBatchProfiler extends AbstractAnalysesReplayProfiler {

	override def applyEvents(database: DatabaseReader, additions: Seq[ReplayEvent], deletions: Seq[ReplayEvent], updates: Seq[ReplayEvent]) {
		val addStreams = additions.map(event => new FileInputStream (event.eventFile))
		database.addClassFiles(addStreams)
		addStreams.foreach(_.close())

		val removeStreams = deletions.map(event => new FileInputStream (event.previousEvent.getOrElse(throw new IllegalStateException ("remove event without predecessor")).eventFile))
		database.removeClassFiles(removeStreams)
		removeStreams.foreach(_.close())

		val updateOldStreams = updates.map(event => new FileInputStream (event.previousEvent.getOrElse(throw new IllegalStateException ("change event without predecessor")).eventFile))
		val updateNewStreams = updates.map(event => new FileInputStream (event.eventFile) )
		database.updateClassFiles(updateOldStreams, updateNewStreams)
		updateOldStreams.foreach(_.close())
		updateNewStreams.foreach(_.close())
	}

}
