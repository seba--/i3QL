package sae.analyses.profiler

import sae.analyses.profiler.measure.TimeMeasurement
import sae.analyses.profiler.util.{ReplayEventType, ReplayEvent}
import sae.analyses.profiler.statistics._
import sae.bytecode.profiler.statistics.EventStatistic
import idb.Relation
import sae.bytecode.BytecodeDatabase
import java.io.FileInputStream
import sae.analyses.profiler.util.ReplayEvent
import sae.analyses.profiler.statistics.SimpleDataStatistic
import sae.analyses.profiler.measure.units.{MilliSeconds, MeasurementUnit}

/**
 * @author Mirko Köhler
 */
trait AbstractAnalysesReplayTimeProfiler
	extends AbstractPropertiesFileReplayProfiler
	with AbstractAnalysesProfiler
	with TimeMeasurement {


	private def sortEventsByType(events: Seq[ReplayEvent]): (Seq[ReplayEvent], Seq[ReplayEvent], Seq[ReplayEvent]) = {
		var additions: List[ReplayEvent] = Nil
		var deletions: List[ReplayEvent] = Nil
		var updates: List[ReplayEvent] = Nil
		for (event <- events) event.eventType match {
			case ReplayEventType.ADDED => additions = event :: additions
			case ReplayEventType.REMOVED => deletions = event :: deletions
			case ReplayEventType.CHANGED if event.previousEvent.isDefined => updates = event :: updates
			case ReplayEventType.CHANGED if !event.previousEvent.isDefined => additions = event :: additions
		}
		(additions, deletions, updates)
	}

	private def applyEvents(database: BytecodeDatabase, additions: Seq[ReplayEvent], deletions: Seq[ReplayEvent], updates: Seq[ReplayEvent]) {
		additions.foreach (event => {
			val stream = new FileInputStream (event.eventFile)
			database.addClassFile (stream)
			stream.close ()
		}
		)
		deletions.foreach (event => {
			val stream = new java.io.FileInputStream (event.previousEvent.getOrElse (throw new IllegalStateException ("remove event without predecessor")).eventFile)
			database.removeClassFile (stream)
			stream.close ()
		}
		)
		updates.foreach (event => {
			val oldStream = new java.io.FileInputStream (event.previousEvent.getOrElse (throw new IllegalStateException ("change event without predecessor")).eventFile)
			val newStream = new java.io.FileInputStream (event.eventFile)
			database.updateClassFile (oldStream, newStream)
			oldStream.close ()
			newStream.close ()
		}
		)
	}

	def dataStatistics(eventSets: List[Seq[ReplayEvent]]): List[DataStatistic] = {

		val classes = database.classDeclarations.asMaterialized

		val methods = database.methodDeclarations.asMaterialized

		val fields = database.fieldDeclarations.asMaterialized

		val instructions = database.instructions.asMaterialized

		var result: List[DataStatistic] = Nil

		eventSets.foreach (set =>
		{
			val (additions, deletions, updates) = sortEventsByType (set)
			applyEvents (database, additions, deletions, updates)
			result = result ::: List (SimpleDataStatistic (classes.size, methods.size, fields.size, instructions.size))
		}
		)
		result
	}

	def measurementUnit: MeasurementUnit = MilliSeconds

	private def applyAnalysesWithJarReading(eventSets: List[Seq[ReplayEvent]], queries: List[String]): List[Long] = {
		val results = queries.foreach (getAnalysis)
		eventSets map {
			set => applyStepWithJarReadTime (set)
		}
	}

	private def applyStepWithJarReadTime(set: Seq[ReplayEvent]): Long = {
		var taken: Long = 0
		val (additions, deletions, updates) = sortEventsByType (set)
		//print (set.head.eventTime)
		time {
			l => taken += l
		}
		{
			//TODO What does this refer to in the new database?
			//database.beginTransaction ()
			applyEvents (database, additions, deletions, updates)

			//if (optimized) {


			//	database.computeTransactionUpdates ()
				/*
				val batDb = database.asInstanceOf[BATBytecodeDatabase]
				print ("; ")
				val addedMethods =
					batDb.transaction.codeAdditions.size + batDb.transaction.codeUpdates.size
				val deletedMethods =
					batDb.transaction.codeDeletions.size + batDb.transaction.codeUpdates.size
				val addedInstr =
					batDb.transaction.codeAdditions.map (u => u.code.instructions.size).sum +
					batDb.transaction.codeUpdates.map (u => u.newV.code.instructions.size).sum
				val deletedInstr =
					batDb.transaction.codeDeletions.map (u => u.code.instructions.size).sum +
						batDb.transaction.codeUpdates.map (u => u.oldV.code.instructions.size).sum
				print(addedMethods)
				print ("; ")
				print(deletedMethods)
				print ("; ")
				print(addedInstr)
				print ("; ")
				print(deletedInstr)
				print ("; ")
				println
				*/
			}
			/*
		else
		{
			val batDb = database.asInstanceOf[BATBytecodeDatabase]
			print ("; ")
			val addedMethods =
				batDb.transaction.codeAdditions.size
			val deletedMethods =
				batDb.transaction.codeDeletions.size
			val addedInstr =
				batDb.transaction.codeAdditions.map (u => u.code.instructions.size).sum
			val deletedInstr =
				batDb.transaction.codeDeletions.map (u => u.code.instructions.size).sum
			print(addedMethods)
			print ("; ")
			print(deletedMethods)
			print ("; ")
			print(addedInstr)
			print ("; ")
			print(deletedInstr)
			print ("; ")
			println
		}
		*/
		//	database.doEndtrans ()

		val memoryMXBean = java.lang.management.ManagementFactory.getMemoryMXBean
		memoryMXBean.gc ()
		print(".")
		taken
	}

	/**
	 * Perform the actual measurement.
	 */
	def measure(iterations: Int, eventSets: List[Seq[ReplayEvent]], queries: List[String]): List[ReplayStatistic] = {
    	measureTime (iterations, eventSets.size)(() => applyAnalysesWithJarReading (eventSets, queries))

	}

	def measureTime(iterations: Int, sampleSize: Int)(f: () => List[Long]): List[ReplayStatistic] = {
		val statistics = Array.fill (sampleSize)(new SimpleReplayStatistic(iterations))

		for (i <- 1 to iterations)
		{
			val results = f ()
			var j = 0
			while (j < sampleSize) {
				statistics (j).add (i, results (j))
				j += 1
			}
			println ()
		}
		statistics.toList
	}


	/**
	 * Perform the warmup by doing exactly the same operation as in the measurement.
	 * The warmup is must return the number of results returned by the measured analyses.
	 */
	def warmup(iterations: Int, eventSets: List[Seq[ReplayEvent]], queries: List[String]): List[Long] = {
		for (i <- 0 until iterations) {
			applyAnalysesWithJarReading (eventSets, queries)
			println ()
		}
		getResultsWithReadingJars (eventSets, queries)
	}

	private def getResultsWithReadingJars(eventSets: List[Seq[ReplayEvent]], queries: List[String]): List[Long] = {
		val queryResults = for (query <- queries) yield {
			getAnalysis (query).asMaterialized
		}
		eventSets map {
			set => {
				applyStepWithJarReadTime (set)
				queryResults.map (_.size).sum.toLong
			}
		}
	}
}
