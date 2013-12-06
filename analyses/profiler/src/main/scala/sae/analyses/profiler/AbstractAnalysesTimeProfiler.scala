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
package sae.analyses.profiler

import sae.analyses.profiler.measure.{MilliSeconds, TimeMeasurement}
import sae.bytecode.BytecodeDatabase
import sae.analyses.profiler.statistics.SampleStatistic
import idb.Relation


/**
 *
 * @author Ralf Mitschke
 *
 */

trait AbstractAnalysesTimeProfiler
  extends AbstractAnalysesProfiler
  with TimeMeasurement
{

  val usage: String = """|Usage: java SAEAnalysesTimeProfiler propertiesFile
                        |(c) 2012 Ralf Mitschke (mitschke@st.informatik.tu-darmstadt.de)
                        | """.stripMargin


  def measure(iterations: Int, jars: List[String], queries: List[String]): SampleStatistic = {
      measureTime (iterations)(() => applyAnalysesWithJarReading (jars, queries))
  }

  def warmup(iterations: Int, jars: List[String], queries: List[String]): Long = {
    var i = 0
    while (i < iterations) {
        applyAnalysesWithJarReading (jars, queries)

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


  def applyAnalysesWithJarReading(jars: List[String], queries: List[String]): Long = {
    var taken: Long = 0
    var database = databaseFactory.create ()
    val results = for (query <- queries) yield {
      getAnalysis (query, database).asMaterialized
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
        getAnalysis (query, database).asMaterialized
      }

    }
    relations.foreach (_.clearObserversForChildren (visitChild => true))
    relations = null
    val memoryMXBean = java.lang.management.ManagementFactory.getMemoryMXBean
    memoryMXBean.gc ()
    print (".")
    taken
  }

 /* def applyAnalysesWithTransactionalJarReading(jars: List[String], queries: List[String]): Long = {
    var taken: Long = 0
    var database = databaseFactory.create ()
    val results = for (query <- queries) yield {
      getAnalysis (query, database)(optimized, transactional, sharedSubQueries).asMaterialized
    }
    jars.foreach (jar => {

      database.beginTransaction ()
      val stream = this.getClass.getClassLoader.getResourceAsStream (jar)
      database.addArchive (stream)
      stream.close ()
      time {
        l => taken += l
      }
      {
        database.commitTransaction ()
      }
    })
    database = null
    val memoryMXBean = java.lang.management.ManagementFactory.getMemoryMXBean
    memoryMXBean.gc ()
    print (".")
    taken
  }   */


  def measurementUnit = MilliSeconds
}