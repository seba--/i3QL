package sae.analyses.profiler.interfaces

import sae.bytecode.ASMDatabaseFactory

/**
 * @author Mirko Köhler
 */
abstract class AbstractBytcodeDatabaseRunner {

	var profiler : BytecodeDatabaseAnalysesReplayProfiler with AbstractPropertiesFileReplayProfiler

	def main(args: Array[String]) {
		if (args.length == 0) {
			System.err.println("No properties file specified.")
			sys.exit(1)
		}

		for (i <- 0 until args.length) {
			val propertiesFile = args(i)

			println("Running analyses for properties file " + propertiesFile +"...")

			profiler.execute(propertiesFile)

			profiler = null

			val memoryMXBean = java.lang.management.ManagementFactory.getMemoryMXBean
			memoryMXBean.gc ()
		}
	}

}
