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

import sae.bytecode.bat.BATDatabaseFactory
import sae._
import bytecode.BytecodeDatabase
import bytecode.profiler.{TimeMeasurement, AbstractPropertiesFileReplayProfiler}
import bytecode.profiler.statistics.{SimpleDataStatistic, DataStatistic}
import de.tud.cs.st.lyrebird.replayframework.{EventType, Event}


/**
 *
 * @author Ralf Mitschke
 *
 */

class SAEAnalysesReplayTimeProfiler
    extends AbstractPropertiesFileReplayProfiler
    with TimeMeasurement
{
    /*
        def getAnalysis(query: String, database: BytecodeDatabase)(implicit optimized: Boolean = false): Relation[_]

        val usage: String = """|Usage: java SAEAnalysesTimeProfiler propertiesFile
                              |(c) 2012 Ralf Mitschke (mitschke@st.informatik.tu-darmstadt.de)
                              | """.stripMargin


        def measure(iterations: Int, jars: List[String], queries: List[String], reReadJars: Boolean): SampleStatistic = {
            if (reReadJars) {
                measureTime (iterations)(() => applyAnalysesWithJarReading (jars, queries))
            }
            else
            {
                val database = createMaterializedDatabase (jars, queries)
                measureTime (iterations)(() => applyAnalysesWithoutJarReading (database, queries))
            }
        }


        def createMaterializedDatabase(jars: List[String], queries: List[String]) = {
            val database = BATDatabaseFactory.create ()
            val materializedDatabase = new MaterializedBytecodeDatabase (database)

            // initialize the needed materializations at least once
            val relations = for (query <- queries) yield {
                getAnalysis (query, materializedDatabase)
            }


            jars.foreach (jar => {
                val stream = this.getClass.getClassLoader.getResourceAsStream (jar)
                database.addArchive (stream)
                stream.close ()
            })

            relations.foreach (_.clearObserversForChildren (visitChild => true))

            materializedDatabase
        }

        def warmup(iterations: Int, jars: List[String], queries: List[String], reReadJars: Boolean): Long = {
            val materializedDatabase =
                if (reReadJars) {
                    None
                }
                else
                {
                    Some (createMaterializedDatabase (jars, queries))
                }

            var i = 0
            while (i < iterations) {
                if (reReadJars) {
                    applyAnalysesWithJarReading (jars, queries)
                }
                else
                {
                    applyAnalysesWithoutJarReading (materializedDatabase.get, queries)
                }
                i += 1
            }

            if (reReadJars) {
                getResultsWithReadingJars (jars, queries)
            }
            else
            {
                getResultsWithoutReadingJars (jars, queries)
            }
        }

        def getResultsWithReadingJars(jars: List[String], queries: List[String]): Long = {
            var database = BATDatabaseFactory.create ()
            val results = for (query <- queries) yield {
                sae.relationToResult (getAnalysis (query, database))
            }
            jars.foreach (jar => {
                val stream = this.getClass.getClassLoader.getResourceAsStream (jar)
                database.addArchive (stream)
                stream.close ()
            })

            results.map (_.size).sum
        }

        def getResultsWithoutReadingJars(jars: List[String], queries: List[String]): Long = {
            val database = createMaterializedDatabase (jars, queries)
            val results = for (query <- queries) yield {
                val relation = getAnalysis (query, database)
                sae.relationToResult (relation)
            }

            results.map (_.size).sum
        }


        def applyAnalysesWithJarReading(jars: List[String], queries: List[String]): Long = {
            var taken: Long = 0
            var database = BATDatabaseFactory.create ()
            for (query <- queries) yield {
                sae.relationToResult (getAnalysis (query, database))
            }
            time {
                l => taken += l
            }
            {
                jars.foreach (jar => {
                    val stream = this.getClass.getClassLoader.getResourceAsStream (jar)
                    database.addArchive (stream)
                    stream.close ()
                })
            }
            database = null
            val memoryMXBean = java.lang.management.ManagementFactory.getMemoryMXBean
            memoryMXBean.gc ()
            print (".")
            taken
        }


        def applyAnalysesWithoutJarReading(database: BytecodeDatabase, queries: List[String]): Long = {
            var taken: Long = 0
            var relations: Iterable[Relation[_]] = null
            time {
                l => taken += l
            }
            {
                relations = for (query <- queries) yield {
                    sae.relationToResult (getAnalysis (query, database))
                }

            }
            relations.foreach (_.clearObserversForChildren (visitChild => true))
            relations = null
            val memoryMXBean = java.lang.management.ManagementFactory.getMemoryMXBean
            memoryMXBean.gc ()
            print (".")
            taken
        }


        def measurementUnit = MilliSeconds
    */
    def usage = null

    def benchmarkType = null

    def sortEventsByType(events: Seq[Event]): (Seq[Event], Seq[Event], Seq[Event]) = {
        var additions: List[Event] = Nil
        var deletions: List[Event] = Nil
        var updates: List[Event] = Nil
        for (event <- events) event.eventType match {
            case EventType.ADDED => additions = event :: additions
            case EventType.REMOVED => additions = event :: deletions
            case EventType.CHANGED => additions = event :: updates
        }
        (additions, deletions, updates)
    }

    def applyEvents(database: BytecodeDatabase ,additions: Seq[Event], deletions: Seq[Event], updates: Seq[Event]) {
        additions.foreach (event => {
            val stream = new java.io.FileInputStream (event.eventFile)
            database.addClassFile (stream)
            stream.close ()
        }
        )
        deletions.foreach (event => {
            val stream = new java.io.FileInputStream (event.eventFile)
            database.removeClassFile (stream)
            stream.close ()
        }
        )
        updates.foreach (event => {
            val oldStream = new java.io.FileInputStream (event.eventFile)
            val newStream = new java.io.FileInputStream (event.previousEvent.getOrElse (throw new IllegalStateException ("change event without predecessor")).eventFile)
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
            applyEvents(database, additions, deletions, updates)
            result = result ::: List (SimpleDataStatistic (classes.size, methods.size, fields.size, instructions.size))
        }
        )
        result
    }


    /**
     * Perform the actual measurement.
     */
    def measure(iterations: Int, eventSets: List[Seq[Event]], queries: List[String], includeReadTime: Boolean) = null

    /**
     * Perform the warmup by doing exactly the same operation as in the measurement.
     * The warmup is must return the number of results returned by the measured analyses.
     */
    def warmup(iterations: Int, eventSets: List[Seq[Event]], queries: List[String], includeReadTime: Boolean) = null


    def applyStepWithJarReadTime(set: Seq[Event], database: BytecodeDatabase): Long = {
        var taken: Long = 0
        val (additions, deletions, updates) = sortEventsByType (set)
        time {
            l => taken += l
        }
        {
            applyEvents(additions, deletions, updates)
        }
        val memoryMXBean = java.lang.management.ManagementFactory.getMemoryMXBean
        memoryMXBean.gc ()
        print (".")
        taken
    }

}
