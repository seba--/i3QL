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

import sae.analyses.profiler.measure.{MilliSeconds, MeasurementUnit}
import sae.bytecode.profiler.statistics.EventStatistic
import sae.analyses.profiler.util.{ReplayEvent, ReplayReader}
import java.io.{FileInputStream, FileWriter, PrintWriter, File}
import sae.analyses.profiler.statistics.{DataStatistic, SampleStatistic}

/**
 *
 * @author Ralf Mitschke
 *
 */

trait AbstractPropertiesFileReplayProfiler extends PropertiesFileImporter
{

	def basePath = "analyses/properties/replay-time/"

	def execute(propertiesFile : String) {
		val properties = getProperties(propertiesFile).getOrElse(
		{
			println("could not find properties file or resource: " + propertiesFile)
			sys.exit(-1)
		}
		)

		val warmupIterations = properties.getProperty("sae.warmup.iterations").toInt
		val warmupLocation = properties.getProperty("sae.warmup.location")

		val measurementIterations = properties.getProperty("sae.benchmark.iterations").toInt
		val measurementLocation = properties.getProperty("sae.benchmark.location")

		val queries =
			if (properties.getProperty ("sae.benchmark.queries").isEmpty)
			{
				Nil
			}
			else
			{
				properties.getProperty ("sae.benchmark.queries").split (";").toList
			}

		val outputFile = properties.getProperty("sae.benchmark.out", System.getProperty("user.dir") + "/bench.txt")


		println("Warmup: " + warmupIterations + " times : " + queries + " on " + warmupLocation)



		val warmupEventReader = new ReplayReader(new File(warmupLocation))
		val warmupEvents = warmupEventReader.getAllEventSets
		val counts = warmup(warmupIterations, warmupEvents, queries)

		println("\tdone")
		println("Num. of Results: " + counts)

		val measurementEventReader = new ReplayReader(new File(measurementLocation))
		val measurementEvents = measurementEventReader.getAllEventSets


		val memoryMXBean = java.lang.management.ManagementFactory.getMemoryMXBean
		memoryMXBean.gc()

		println("Measure: " + measurementIterations + " times : " + queries + " on " + measurementLocation)
		val statistics : List[SampleStatistic] = measure(measurementIterations, measurementEvents, queries)
		println("\tdone")

		//statistics.foreach(x => println(x.summary (measurementUnit)))

		val dataStatisticList = dataStatistics(measurementEvents)

		if (counts.size != measurementEvents.size || dataStatisticList.size != measurementEvents.size || statistics
			.size != measurementEvents.size) {
			sys.error("different sizes for sampled data and list of event sets")
			sys.exit(-1)
		}


		//println (dataStatistics.summary)

		reportCSV(outputFile, warmupIterations, measurementIterations, measurementLocation, measurementEvents, dataStatisticList, queries, statistics, counts)

		val maxTime = MilliSeconds.fromBase(statistics.map(x => x.mean).fold(Double.NegativeInfinity)(math.max))
		val minTime = MilliSeconds.fromBase(statistics.map(x => x.mean).fold(Double.PositiveInfinity)(math.min))
		val meanTime = MilliSeconds.fromBase(statistics.map(x => x.mean).fold(0d)((x : Double,y : Double) => x + y)) / statistics.length

		Predef.println("File: " + propertiesFile)
		Predef.println("Max Time: %.3f - Min Time: %.3f - Mean Time : %.3f".format(maxTime, minTime, meanTime))

		sys.exit(0)
	}


    def reportCSV(outputFile: String, warmUpIterations: Int, measurementIterations: Int, measurementLocation: String,
                  eventSets: List[Seq[ReplayEvent]], dataStatistics: List[DataStatistic], queries: List[String],
                  statistics: List[SampleStatistic], resultCounts: List[Long]) {
        val file = new File(outputFile)
        val writeHeader = !file.exists()

        val out = new PrintWriter(new FileWriter(file, true))

        val separator = ";"

        val header = "bench type" + separator + "location" + separator + "timestamp" + separator + "change size" + separator +
                "num. classes" + separator + "num. methods" + separator + "num. fields" + separator + "num. instructions" + separator +
                "num. warmup iterations" + separator + "num. measure iterations" + separator +
                "re-read jars" + separator + "optimized" + separator + "transactional" + separator + "shared" + separator +
                "queries" + separator + "newResult count" + separator + "mean" + separator + "std. dev" + separator + "std err." + separator + "measured unit"

        if (writeHeader) {
            out.println(header)
        }

		var i = 0
        while (i < statistics.size) {
            val outputLine =
                benchmarkType + separator +
                        measurementLocation + separator +
                      //  eventSets(i)(0).eventTime + separator +
                      //  eventSets(i).size + separator +
                        dataStatistics(i).classCount + separator +
                        dataStatistics(i).methodCount + separator +
                        dataStatistics(i).fieldCount + separator +
                        dataStatistics(i).instructionCount + separator +
                        warmUpIterations + separator +
                        measurementIterations + separator +
                        (if(queries.isEmpty){"NONE" + separator} else {queries.reduce (_ + " | " + _) + separator}) +
                        resultCounts(i) + separator +
                        ("%.3f" formatLocal(java.util.Locale.UK, measurementUnit
                                .fromBase(statistics(i).mean))) + separator +
                        ("%.3f" formatLocal(java.util.Locale.UK, measurementUnit
                                .fromBase(statistics(i).standardDeviation))) + separator +
                        ("%.3f" formatLocal(java.util.Locale.UK, measurementUnit
                                .fromBase(statistics(i).standardError))) + separator +
                        measurementUnit.descriptor

		//	Predef.println(outputLine)
            out.println(outputLine)
            i += 1
        }
        out.close()
    }

    def usage: String

    def benchmarkType: String

    def dataStatistics(eventSets: List[Seq[ReplayEvent]]): List[DataStatistic]

    def measurementUnit: MeasurementUnit

    /**
     * Perform the actual measurement.
     */
    def measure(iterations: Int, eventSets: List[Seq[ReplayEvent]], queries: List[String]): List[SampleStatistic]

    /**
     * Perform the warmup by doing exactly the same operation as in the measurement.
     * The warmup is must return the number of results returned by the measured analyses.
     */
    def warmup(iterations: Int, eventSets: List[Seq[ReplayEvent]], queries: List[String]): List[Long]


}
