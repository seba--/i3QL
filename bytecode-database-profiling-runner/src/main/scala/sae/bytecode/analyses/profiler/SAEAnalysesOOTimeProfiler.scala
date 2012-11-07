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


import sae.bytecode.profiler.{TimeMeasurement, AbstractPropertiesFileProfiler}
import sae.bytecode.profiler.util.MilliSeconds
import sae.bytecode.bat.BATDatabaseFactory
import sae.analyses.findbugs.AnalysesOO


/**
 *
 * @author Ralf Mitschke
 *
 */

object SAEAnalysesOOTimeProfiler
    extends AbstractPropertiesFileProfiler
    with TimeMeasurement
{

    val usage: String = """|Usage: java SAEAnalysesOOTimeProfiler propertiesFile
                          |(c) 2012 Ralf Mitschke (mitschke@st.informatik.tu-darmstadt.de)
                          | """.stripMargin


    def measure(iterations: Int, jars: List[String], queries: List[String]) {
        println ("Measure: " + iterations + " times : " + queries + " on " + jars)
        val statistic = measureTime (iterations)(() => applyAnalyses (jars, queries))
        println ("\tdone")
        println (statistic.summary (MilliSeconds))
    }


    def warmup(iterations: Int, jars: List[String], queries: List[String]) {
        println ("Warmup: " + iterations + " times : " + queries + " on " + jars)
        var i = 0
        while (i < iterations) {
            doWarmup (jars, queries)
            i += 1
        }

        println ("\tdone")
    }


    def doWarmup(jars: List[String], queries: List[String]) {
        applyAnalyses (jars, queries)
    }

    def applyAnalyses(jars: List[String], queries: List[String]): Long = {
        var taken: Long = 0
        val database = BATDatabaseFactory.create ()
        for (query <- queries) {
            AnalysesOO (query, database)
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
        }
        print (".")
        taken
    }


}
