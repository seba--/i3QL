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
package sae.bytecode.analyses.profiler


import sae._
import bytecode.bat.{BATBytecodeDatabase, BATDatabaseFactory}
import bytecode.BytecodeDatabase
import bytecode.profiler.util.MilliSeconds
import bytecode.profiler.{TimeMeasurement, AbstractPropertiesFileReplayProfiler}
import bytecode.profiler.statistics.{Statistic, SampleStatistic, SimpleDataStatistic, DataStatistic}
import de.tud.cs.st.lyrebird.replayframework.{EventType, Event}


/**
 *
 * @author Ralf Mitschke
 *
 */

abstract class SAEAnalysesReplayTimeProfiler
    extends AbstractPropertiesFileReplayProfiler
    with TimeMeasurement
{
    def measurementUnit = MilliSeconds

    val usage: String = """|Usage: java SAEAnalysesReplayTimeProfiler propertiesFile
                          |(c) 2012 Ralf Mitschke (mitschke@st.informatik.tu-darmstadt.de)
                          | """.stripMargin

    def getAnalysis(query: String, database: BytecodeDatabase)(optimized: Boolean, sharedSubQueries: Boolean): Relation[_]

    private def sortEventsByType(events: Seq[Event]): (Seq[Event], Seq[Event], Seq[Event]) = {
        var additions: List[Event] = Nil
        var deletions: List[Event] = Nil
        var updates: List[Event] = Nil
        for (event <- events) event.eventType match {
            case EventType.ADDED => additions = event :: additions
            case EventType.REMOVED => deletions = event :: deletions
            case EventType.CHANGED if event.previousEvent.isDefined => updates = event :: updates
            case EventType.CHANGED if !event.previousEvent.isDefined => additions = event :: additions
        }
        (additions, deletions, updates)
    }

    private def applyEvents(database: BytecodeDatabase, additions: Seq[Event], deletions: Seq[Event], updates: Seq[Event]) {
        additions.foreach (event => {
            val stream = new java.io.FileInputStream (event.eventFile)
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


    def dataStatistics(eventSets: List[Seq[Event]]): List[DataStatistic] = {
        var database = BATDatabaseFactory.create ()

        val classes = relationToResult (database.classDeclarations)

        val methods = relationToResult (database.methodDeclarations)

        val fields = relationToResult (database.fieldDeclarations)

        val instructions = relationToResult (database.instructions)

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


    /**
     * Perform the actual measurement.
     */
    def measure(iterations: Int, eventSets: List[Seq[Event]], queries: List[String], includeReadTime: Boolean): List[SampleStatistic] = {

        if (includeReadTime) {
            measureTime (iterations, eventSets.size)(() => applyAnalysesWithJarReading (eventSets, queries))
        }
        else
        {
            //applyAnalysesWithoutJarReading (materializedDatabase.get, queries)
            throw new UnsupportedOperationException ("replay without class file time not supported")
        }
    }

    /**
     * Perform the warmup by doing exactly the same operation as in the measurement.
     * The warmup is must return the number of results returned by the measured analyses.
     */
    def warmup(iterations: Int, eventSets: List[Seq[Event]], queries: List[String], includeReadTime: Boolean): List[Long] = {

        var i = 0
        while (i < iterations) {
            if (includeReadTime) {
                applyAnalysesWithJarReading (eventSets, queries)
            }
            else
            {
                //applyAnalysesWithoutJarReading (materializedDatabase.get, queries)
                throw new UnsupportedOperationException ("replay without class file time not supported")
            }
            println ()
            i += 1
        }

        if (includeReadTime) {
            getResultsWithReadingJars (eventSets, queries)
        }
        else
        {
            throw new UnsupportedOperationException ("replay without class file time not supported")
        }
    }


    /**
     * performs the measurement of function f, iterations times.
     * f should return the time taken to perform the required computation.
     * A statistic is returned for the time consumed when applying f
     */
    def measureTime(iterations: Int, sampleSize: Int)(f: () => List[Long]): List[SampleStatistic] = {
        val statistics = Array.fill (sampleSize)(Statistic (iterations))

        for (i <- 1 to iterations)
        {
            val results = f ()
            var j = 0
            while (j < sampleSize) {
                statistics (j).add (results (j))
                j += 1
            }
            println ()
        }
        statistics.toList
    }

    private def applyAnalysesWithJarReading(eventSets: List[Seq[Event]], queries: List[String]): List[Long] = {
        val database = BATDatabaseFactory.create ()
        val results = queries.foreach (q => getAnalysis (q, database)(optimized, sharedSubQueries))
        eventSets map {
            set => applyStepWithJarReadTime (set, database)
        }
    }

    private def applyStepWithJarReadTime(set: Seq[Event], database: BytecodeDatabase): Long = {
        var taken: Long = 0
        val (additions, deletions, updates) = sortEventsByType (set)
        print (set.head.eventTime)
        time {
            l => taken += l
        }
        {
            database.beginTransaction ()
            applyEvents (database, additions, deletions, updates)

            if (optimized) {
                database.computeTransactionUpdates ()
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
            database.commitTransaction ()
        }
        val memoryMXBean = java.lang.management.ManagementFactory.getMemoryMXBean
        memoryMXBean.gc ()
        print(", ")
        taken
    }


    private def getResultsWithReadingJars(eventSets: List[Seq[Event]], queries: List[String]): List[Long] = {
        var database = BATDatabaseFactory.create ()
        val queryResults = for (query <- queries) yield {
            sae.relationToResult (getAnalysis (query, database)(optimized, sharedSubQueries))
        }
        eventSets map {
            set => {
                applyStepWithJarReadTime (set, database)
                queryResults.map (_.size).sum.toLong
            }
        }
    }


    def eventStatistics(eventSets: List[Seq[Event]]) = Nil
}
