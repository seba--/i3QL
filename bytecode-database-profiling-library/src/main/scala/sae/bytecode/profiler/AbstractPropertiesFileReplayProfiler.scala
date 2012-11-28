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
package sae.bytecode.profiler

import java.io._
import java.util.Properties
import statistics.{DataStatistic, MeasurementUnit, SampleStatistic}
import scala.Some
import de.tud.cs.st.lyrebird.replayframework.Event
import de.tud.cs.st.lyrebird.replayframework.file.Reader
import collection.Seq

/**
 *
 * @author Ralf Mitschke
 *
 */

trait AbstractPropertiesFileReplayProfiler
{

    def main(args: Array[String]) {
        if (args.length == 0 || !args (0).endsWith (".properties")) {
            println (usage)
            sys.exit (1)
        }

        val propertiesFile = args (0)

        val reReadJars =
            if (args.length > 1)
            {
                java.lang.Boolean.parseBoolean (args (1))
            }
            else
            {
                false
            }

        val properties = getProperties (propertiesFile).getOrElse (
        {
            println ("could not find properties file or resource: " + propertiesFile)
            sys.exit (-1)
        }
        )

        val warmupIterations = properties.getProperty ("sae.warmup.iterations").toInt
        val warmupLocation = properties.getProperty ("sae.warmup.location")

        val measurementIterations = properties.getProperty ("sae.benchmark.iterations").toInt
        val measurementLocation = properties.getProperty ("sae.benchmark.location")
        val queries = properties.getProperty ("sae.benchmark.queries").split (";").toList

        val outputFile = properties.getProperty ("sae.benchmark.out", System.getProperty ("user.dir") + "/bench.txt")


        println ("Warmup: " + warmupIterations + " times : " + queries + " on " + warmupLocation + " re-read = " + reReadJars)

        val warmupEventReader = new Reader (new File (warmupLocation))
        val warmupEvents = warmupEventReader.getAllEventSets
        val counts = warmup (warmupIterations, warmupEvents, queries, reReadJars)

        println ("\tdone")
        println ("Num. of Results: " + counts)

        val measurementEventReader = new Reader (new File (measurementLocation))
        val measurementEvents = measurementEventReader.getAllEventSets


        val memoryMXBean = java.lang.management.ManagementFactory.getMemoryMXBean
        memoryMXBean.gc ()

        println ("Measure: " + measurementIterations + " times : " + queries + " on " + measurementLocation + " re-read = " + reReadJars)
        val statistics = measure (measurementIterations, measurementEvents, queries, reReadJars)
        println ("\tdone")

        //println (statistics.summary (measurementUnit))

        val dataStatisticList = dataStatistics (measurementEvents)

        if (counts.size != measurementEvents.size ||dataStatisticList.size != measurementEvents.size || statistics.size != measurementEvents.size)
        {
            sys.error ("different sizes for sampled data and list of event sets")
            sys.exit (-1)
        }


        //println (dataStatistics.summary)

        reportCSV (outputFile, reReadJars, warmupIterations, measurementIterations, measurementLocation, measurementEvents, dataStatisticList, queries, statistics, counts)

        sys.exit (0)
    }

    def reportCSV(outputFile: String, reReadJars: Boolean, warmUpIterations: Int, measurementIterations: Int, measurementLocation : String, eventSets: List[Seq[Event]], dataStatistics: List[DataStatistic], queries: List[String], statistics: List[SampleStatistic], resultCounts: List[Long]) {
        val file = new File (outputFile)
        val writeHeader = !file.exists ()

        val out = new PrintWriter (new FileWriter (file, true))

        val separator = ";"

        val header = "bench type" + separator + "location" + separator +  "timestamp" + separator +
            "num. classes" + separator + "num. methods" + separator + "num. fields" + separator + "num. instructions" + separator +
            "num. warmup iterations" + separator + "num. measure iterations" + separator + "re-read jars" + separator + "queries" + separator +
            "result count" + separator + "mean" + separator + "std. dev" + separator + "std err." + separator + "measured unit"




        if (writeHeader) {
            out.println (header)
        }

        var i = 0
        while (i < statistics.size) {
            val outputLine =
                benchmarkType + separator +
                    measurementLocation + separator +
                    eventSets(i)(0).eventTime + separator +
                    dataStatistics(i).classCount + separator +
                    dataStatistics(i).methodCount + separator +
                    dataStatistics(i).fieldCount + separator +
                    dataStatistics(i).instructionCount + separator +
                    warmUpIterations + separator +
                    measurementIterations + separator +
                    reReadJars.toString + separator +
                    queries.reduce (_ + " | " + _) + separator +
                    resultCounts(i) + separator +
                    ("%.3f" formatLocal (java.util.Locale.UK, measurementUnit.fromBase (statistics(i).mean))) + separator +
                    ("%.3f" formatLocal (java.util.Locale.UK, measurementUnit.fromBase (statistics(i).standardDeviation))) + separator +
                    ("%.3f" formatLocal (java.util.Locale.UK, measurementUnit.fromBase (statistics(i).standardError))) + separator +
                    measurementUnit.descriptor

            out.println (outputLine)
            i += 1
        }
        out.close ()
    }

    def usage: String

    def benchmarkType: String

    def dataStatistics(eventSets: List[Seq[Event]]): List[DataStatistic]

    def measurementUnit: MeasurementUnit

    /**
     * Perform the actual measurement.
     */
    def measure(iterations: Int, eventSets: List[Seq[Event]], queries: List[String], includeReadTime: Boolean): List[SampleStatistic]

    /**
     * Perform the warmup by doing exactly the same operation as in the measurement.
     * The warmup is must return the number of results returned by the measured analyses.
     */
    def warmup(iterations: Int, eventSets: List[Seq[Event]], queries: List[String], includeReadTime: Boolean): List[Long]


    def isFile(propertiesFile: String): Boolean = {
        val file = new java.io.File (propertiesFile)
        file.exists () && file.canRead && !file.isDirectory

    }

    def isResource(propertiesFile: String): Boolean = {
        this.getClass.getClassLoader.getResource (propertiesFile) != null
    }

    def getProperties(propertiesFile: String): Option[Properties] = {
        if (isFile (propertiesFile)) {
            val file = new java.io.File (propertiesFile)
            val properties = new Properties ()
            properties.load (new FileInputStream (file))
            return Some (properties)
        }
        if (isResource (propertiesFile))
        {
            val properties = new Properties ()
            properties.load (this.getClass.getClassLoader.getResource (propertiesFile).openStream ())
            return Some (properties)
        }
        None
    }

}
