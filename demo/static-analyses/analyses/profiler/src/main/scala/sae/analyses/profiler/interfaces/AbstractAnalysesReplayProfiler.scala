/* License (BSD Style License):
 *  Copyright (c) 2009, 2011
 *  Software Technology Group
 *  Department of Computer Science
 *  Technische Universität Darmstadt
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  - Neither the name of the Software Technology Group or Technische
 *    Universität Darmstadt nor the names of its contributors may be used to
 *    endorse or promote products derived from this software without specific
 *    prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 */
package sae.analyses.profiler.interfaces

import sae.bytecode.{BytecodeDatabaseFactory, BytecodeDatabase}
import idb.Relation
import sae.analyses.profiler.statistics.{SimpleReplayStatistic, ReplayStatistic, SimpleDataStatistic, DataStatistic}
import sae.analyses.profiler.util.{DatabaseReader, ReplayEventType, ReplayEvent}
import java.io.FileInputStream


/**
 *
 * @author Ralf Mitschke, Mirko Köhler
 *
 */

trait AbstractAnalysesReplayProfiler extends AbstractAnalysesProfiler {

	def measurement[T](mu: (Long) => Unit)(f: => T)

	private var	resultsList : List[DataStatistic] = Nil

	override def getResults = resultsList

	def applyStepWithJarRead(set: Seq[ReplayEvent]): Long = {
		var taken: Long = 0
		val (additions, deletions, updates) = sortEventsByType (set)

		measurement (l => taken += l)(applyEvents (database, additions, deletions, updates))

		if (storeResults) {
			resultsList = resultsList ::: List (new SimpleDataStatistic(classCount,0,0,0))
		}

		val memoryMXBean = java.lang.management.ManagementFactory.getMemoryMXBean
		memoryMXBean.gc ()
		print(".")
		taken
	}

    def sortEventsByType(events: Seq[ReplayEvent]): (Seq[ReplayEvent], Seq[ReplayEvent], Seq[ReplayEvent]) = {
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

    def applyEvents(database: DatabaseReader, additions: Seq[ReplayEvent], deletions: Seq[ReplayEvent], updates: Seq[ReplayEvent]) {
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

        var result: List[DataStatistic] = Nil

        eventSets.foreach (set =>
        {
            val (additions, deletions, updates) = sortEventsByType (set)
            applyEvents (database, additions, deletions, updates)
            result = result ::: List (SimpleDataStatistic (database.classCount, database.methodCount, database.fieldCount, database.instructionCount))
        }
        )
        result
    }



 /*   def applyAnalysesWithJarReading(eventSets: List[Seq[ReplayEvent]], queries: List[String]): List[Long] = {
        val results = queries.foreach (getAnalysis)
        eventSets map {
            set => applyStepWithJarRead (set)
        }
    }  */




    def getResultsWithReadingJars(eventSets: List[Seq[ReplayEvent]], queries: List[String]): List[Long] = {

		val queryResults : List[Relation[_]] = queries map getAnalysis
		val result = eventSets map (s => { applyStepWithJarRead(s)})

		result
    }

    def measureSample(iterations: Int, sampleSize: Int)(f: () => List[Long]): List[ReplayStatistic] = {
        val statistics = Array.fill (sampleSize)(new SimpleReplayStatistic(iterations))
        for (i <- 1 to iterations)
        {
			initializeDatabase()
            val results : List[Long] = f ()
            var j = 0
            while (j < sampleSize) {
                statistics (j).add (i, results (j))
                j += 1
            }
			disposeDatabase()

			val memoryMXBean = java.lang.management.ManagementFactory.getMemoryMXBean
			memoryMXBean.gc ()

			println ()
        }
        statistics.toList
    }

    def measure(iterations: Int, eventSets: List[Seq[ReplayEvent]], queries: List[String]): List[ReplayStatistic] = {
        measureSample (iterations, eventSets.size)(() => getResultsWithReadingJars (eventSets, queries))

    }

    /**
     * Perform the warmup by doing exactly the same operation as in the measurement.
     * The warmup is must return the number of results returned by the measured analyses.
     */
    def warmup(iterations: Int, eventSets: List[Seq[ReplayEvent]], queries: List[String]) {
		if (iterations < 1)
			return

		storeResults = true
		measureSample (1, eventSets.size)(() => getResultsWithReadingJars (eventSets, queries))
		storeResults = false

		if (iterations > 1)
			measureSample (iterations - 1, eventSets.size)(() => getResultsWithReadingJars (eventSets, queries))
    }


}
