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

import sae.bytecode.profiler.statistics.EventStatistic
import sae.analyses.profiler.util.{PropertiesFileImporter, ReplayEvent, ReplayReader}
import java.io.{FileInputStream, FileWriter, PrintWriter, File}
import sae.analyses.profiler.statistics.{ReplayStatistic, DataStatistic, SampleStatistic}
import sae.analyses.profiler.measure.units.MeasurementUnit

/**
 *
 * @author Ralf Mitschke
 *
 */

trait AbstractPropertiesFileReplayProfiler extends PropertiesFileImporter
{

	def getResults : List[DataStatistic]

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

		val outputPath = System.getProperty("user.dir") + "/benchmarks/" + benchmarkType + "/"+ queryType
		val outputFile = "/" + propertiesFile + ".csv"

		val path = new File(outputPath)
		path.mkdirs()

		println("Warmup: " + warmupIterations + " times : " + queries + " on " + warmupLocation)

		val warmupEventReader = new ReplayReader(new File(warmupLocation))
		val warmupEvents = warmupEventReader.getAllEventSets

        try {
            warmup(warmupIterations, warmupEvents, queries)
        } catch {
            case e : Exception => {
                println("Executing " + propertiesFile + " returned an exception.")
                e.printStackTrace()
                return
            }
        }

		val measurementEventReader = new ReplayReader(new File(measurementLocation))
		val measurementEvents = measurementEventReader.getAllEventSets

        val dataStatisticList = getResults

		val memoryMXBean = java.lang.management.ManagementFactory.getMemoryMXBean
		memoryMXBean.gc()

		println("Measure: " + measurementIterations + " times : " + queries + " on " + measurementLocation)
		val statistics : List[ReplayStatistic] = measure(measurementIterations, measurementEvents, queries)
		println("\tdone")



		if (dataStatisticList.size != measurementEvents.size || statistics
			.size != measurementEvents.size) {
			println(dataStatisticList.size)
			println(measurementEvents.size)
			println(statistics.size)


			sys.error("different sizes for sampled data and list of event sets")




			return
        }

  		reportCSV(outputPath + outputFile, warmupIterations, measurementIterations, measurementLocation, measurementEvents, dataStatisticList, queries, statistics)
	}


    def reportCSV(outputFile: String, warmUpIterations: Int, measurementIterations: Int, measurementLocation: String,
                  eventSets: List[Seq[ReplayEvent]], dataStatistics: List[DataStatistic], queries: List[String],
                  statistics: List[ReplayStatistic]) {


		val file = new File(outputFile)

		if(file.exists())
			file.delete()


		file.createNewFile()

        val out = new PrintWriter(new FileWriter(file, true))

        val separator = ";"

        val header =
            "benchmark" + separator +
            "queries" + separator +
            "location" + separator +
            "timestamp" + separator +
            "change size" + separator +
            "classes" + separator +
            "methods" + separator +
            "fields" + separator +
            "instructions" + separator +
            "warmup iterations" + separator +
            "measure iterations" + separator +
            //"results#" + separator +
            "times(" + measurementUnit.descriptor +")"


        out.println(header)



        for (i <- 0 until statistics.size) {
            var outputLine : String =
                benchmarkType + separator +
                (if(queries.isEmpty) "NONE" else queries.reduce (_ + " | " + _)) + separator +
                measurementLocation + separator +
                eventSets(i)(0).eventTime + separator +
                eventSets(i).size + separator +
                dataStatistics(i).classCount + separator +
                dataStatistics(i).methodCount + separator +
                dataStatistics(i).fieldCount + separator +
                dataStatistics(i).instructionCount + separator +
                warmUpIterations + separator +
                measurementIterations
                //resultCounts(i)

            for (j <- 1 to measurementIterations)
                outputLine = outputLine + separator + measurementUnit.fromBase(statistics(i)(j))

            out.println(outputLine)

        }
        out.close()
    }

    def benchmarkType : String
	def queryType : String



   // def dataStatistics(eventSets: List[Seq[ReplayEvent]]): List[DataStatistic]

    def measurementUnit: MeasurementUnit

    /**
     * Perform the actual measurement.
     */
    def measure(iterations: Int, eventSets: List[Seq[ReplayEvent]], queries: List[String]): List[ReplayStatistic]

    /**
     * Perform the warmup by doing exactly the same operation as in the measurement.
     * The warmup is must return the number of results returned by the measured analyses.
     */
    def warmup(iterations: Int, eventSets: List[Seq[ReplayEvent]], queries: List[String])


}
