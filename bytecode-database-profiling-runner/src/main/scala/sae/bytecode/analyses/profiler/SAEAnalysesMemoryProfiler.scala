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
import sae.bytecode.{MaterializedBytecodeDatabase, BytecodeDatabase}
import sae._
import bytecode.profiler.{AbstractPropertiesFileProfiler, MemoryUsage}
import bytecode.profiler.statistics.{SimpleDataStatistic, DataStatistic, SampleStatistic}
import bytecode.profiler.util.MegaByte


/**
 *
 * @author Ralf Mitschke
 *
 */

abstract class SAEAnalysesMemoryProfiler
    extends AbstractPropertiesFileProfiler
    with MemoryUsage
{
    sae.ENABLE_FORCE_TO_SET = true

    val optimized = true

    def getAnalysis(query: String, database: BytecodeDatabase)(implicit optimized: Boolean = false): Relation[_]

    val usage: String = """|Usage: java SAEAnalysesMemoryProfiler propertiesFile
                          |(c) 2012 Ralf Mitschke (mitschke@st.informatik.tu-darmstadt.de)
                          | """.stripMargin


    def measure(iterations: Int, jars: List[String], queries: List[String], reReadJars: Boolean): SampleStatistic = {
        if (reReadJars) {
            measureMemory (iterations)(() => applyAnalysesWithJarReading (jars, queries))._1
        }
        else
        {
            val database = createMaterializedDatabase (jars, queries)
            measureMemory (iterations)(() => applyAnalysesWithoutJarReading (database, queries))._1
        }
    }


    def createMaterializedDatabase(jars: List[String], queries: List[String]) = {
        val database = BATDatabaseFactory.create ()
        val materializedDatabase = new MaterializedBytecodeDatabase (database)

        // initialize the needed materializations at least once
        val relations = for (query <- queries) yield {
            getAnalysis (query, materializedDatabase)(optimized)
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
            sae.relationToResult (getAnalysis (query, database)(optimized))
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
            //sae.relationToResult (AnalysesOO (query, database))
            getAnalysis (query, database)(optimized)
        }

        results.map (_.size).sum
    }


    def applyAnalysesWithJarReading(jars: List[String], queries: List[String]): Long = {
        var taken: Long = 0
        var database = BATDatabaseFactory.create ()
        val relations = for (query <- queries) yield {
            //sae.relationToResult (AnalysesOO (query, database))
            getAnalysis (query, database)(optimized)
        }

        memory {
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
        memory {
            l => taken += l
        }
        {
            relations = for (query <- queries) yield {
                sae.relationToResult (getAnalysis (query, database)(optimized))
            }

        }
        relations.foreach (_.clearObserversForChildren (visitChild => true))
        relations = null
        val memoryMXBean = java.lang.management.ManagementFactory.getMemoryMXBean
        memoryMXBean.gc ()
        print (".")
        taken
    }

    def dataStatistic(jars: List[String]): DataStatistic = {
        var database = BATDatabaseFactory.create ()

        val classes = relationToResult (database.classDeclarations)

        val methods = relationToResult (database.methodDeclarations)

        val fields = relationToResult (database.fieldDeclarations)

        val instructions = relationToResult (database.instructions)

        jars.foreach (jar => {
            val stream = this.getClass.getClassLoader.getResourceAsStream (jar)
            database.addArchive (stream)
            stream.close ()
        })

        SimpleDataStatistic (classes.size, methods.size, fields.size, instructions.size)
    }

    def measurementUnit = MegaByte

}
