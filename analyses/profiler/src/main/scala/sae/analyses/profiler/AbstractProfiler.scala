package sae.analyses.profiler

import java.util.Properties
import java.io.{FileInputStream, FileWriter, PrintWriter, File}
import sae.analyses.profiler.statistics.{SampleStatistic, DataStatistic}
import sae.analyses.profiler.measure.MeasurementUnit

/**
 * @author Ralf Mitschke, Mirko Köhler
 */
trait AbstractProfiler
{

		def optimized: Boolean = isOptimized

		private var isOptimized = false

		def reReadJars: Boolean = isReReadJars

		private var isReReadJars = false

		def transactional: Boolean = isTransactional

		private var isTransactional = false

		def sharedSubQueries: Boolean = isSharedSubQueries

		private var isSharedSubQueries = false

		def properties = lProperties

		private var lProperties: Properties = new Properties ()

		def main(args: Array[String]) {
			if (args.length == 0 || !args (0).endsWith (".properties")) {
				println (usage)
				sys.exit (1)
			}

      isReReadJars =
        if (args.length > 1)
        {
          java.lang.Boolean.parseBoolean (args (1))
        }
        else
        {
          false
        }

      isOptimized =
        if (args.length > 2)
        {
          java.lang.Boolean.parseBoolean (args (2))
        }
        else
        {
          false
        }
      isTransactional =
        if (args.length > 3)
        {
          java.lang.Boolean.parseBoolean (args (3))
        }
        else
        {
          false
        }
      isSharedSubQueries =
        if (args.length > 4)
        {
          java.lang.Boolean.parseBoolean (args (4))
        }
        else
        {
          false
        }

			val propertiesFile = args (0)

			execute (propertiesFile)

			sys.exit (0)
		}

    def execute (propertiesFile : String) {



      lProperties = getProperties (propertiesFile).getOrElse (
      {
        println ("could not find properties file or resource: " + propertiesFile)
        sys.exit (-1)
      }
      )

      val warmupIterations = properties.getProperty ("sae.warmup.iterations").toInt
      val warmupJars = properties.getProperty ("sae.warmup.jars").split (";").toList

      val measurementIterations = properties.getProperty ("sae.benchmark.iterations").toInt
      val measurementJars = properties.getProperty ("sae.benchmark.jars").split (";").toList
      val queries =
        if (properties.getProperty ("sae.benchmark.queries").isEmpty)
        {
          Nil
        }
        else
        {
          properties.getProperty ("sae.benchmark.queries").split (";").toList
        }

      val outputFile = properties.getProperty ("sae.benchmark.out", System.getProperty ("user.dir") + "/bench.txt")


      println ("Warmup: " + warmupIterations + " times : " + queries + " on " + warmupJars + " re-read = " + reReadJars + " optimized = " + optimized + " transactional = " + transactional + " sharedSubQueries = " + sharedSubQueries)
      val count = warmup (warmupIterations, warmupJars, queries)
      println ("\tdone")
      println ("Num. of Results: " + count)

      val memoryMXBean = java.lang.management.ManagementFactory.getMemoryMXBean
      memoryMXBean.gc ()

      println ("Measure: " + measurementIterations + " times : " + queries + " on " + measurementJars + " re-read = " + reReadJars + " optimized = " + optimized + " transactional = " + transactional + " sharedSubQueries = " + sharedSubQueries)
      val statistic = measure (measurementIterations, measurementJars, queries)
      println ("\tdone")
      println (statistic.summary (measurementUnit))

      val dataStatistics = dataStatistic (measurementJars)

      println (dataStatistics.summary)

      reportCSV (outputFile, warmupIterations, measurementIterations, measurementJars, dataStatistics, queries, statistic, count)
    }

		def reportCSV(outputFile: String, warumUpIterations: Int, measurementIterations: Int, measurementJars: List[String], dataStatistics: DataStatistic, queries: List[String], statistic: SampleStatistic, resultCount: Long) {
			val file = new File (outputFile)
			val writeHeader = !file.exists ()

			val out = new PrintWriter (new FileWriter (file, true))

			val separator = ";"

			val header = "bench type" + separator + "jars" + separator +
				"num. classes" + separator + "num. methods" + separator + "num. fields" + separator + "num. instructions" + separator +
				"num. warmup iterations" + separator + "num. measure iterations" + separator +
				"re-read jars" + separator + "optimized" + separator + "transactional" + separator + "shared" + separator +
				"queries" + separator + "newResult count" + separator +
				"mean" + separator + "std. dev" + separator + "std err." + separator + "measured unit"



			val outputLine =
				benchmarkType + separator +
					measurementJars.reduce (_ + " | " + _) + separator +
					dataStatistics.classCount + separator +
					dataStatistics.methodCount + separator +
					dataStatistics.fieldCount + separator +
					dataStatistics.instructionCount + separator +
					warumUpIterations + separator +
					measurementIterations + separator +
					reReadJars.toString + separator +
					optimized.toString + separator +
					transactional.toString + separator +
					sharedSubQueries.toString + separator +
					(if(queries.isEmpty){"NONE"} else {queries.reduce (_ + " | " + _) + separator}) +
					resultCount + separator +
					("%.3f" formatLocal (java.util.Locale.UK, measurementUnit.fromBase (statistic.mean))) + separator +
					("%.3f" formatLocal (java.util.Locale.UK, measurementUnit.fromBase (statistic.standardDeviation))) + separator +
          ("%.3f" formatLocal (java.util.Locale.UK, measurementUnit.fromBase (statistic.standardError))) + separator +
					measurementUnit.descriptor
			if (writeHeader) {
				out.println (header)
			}
			out.println (outputLine)
			out.close ()
		}

		def usage: String

		def benchmarkType: String

		def dataStatistic(jars: List[String]): DataStatistic

		def measurementUnit: MeasurementUnit

		/**
		 * Perform the actual measurement.
		 */
		def measure(iterations: Int, jars: List[String], queries: List[String]): SampleStatistic

		/**
		 * Perform the warmup by doing exactly the same operation as in the measurement.
		 * The warmup is must return the number of results returned by the measured analyses.
		 */
		def warmup(iterations: Int, jars: List[String], queries: List[String]): Long


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
