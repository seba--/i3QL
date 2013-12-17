package sae.analyses.profiler

import sae.bytecode.{ASMDatabaseFactory, BytecodeDatabaseFactory, BytecodeDatabase}
import idb.Relation
import sae.analyses.findbugs.Analyses

/**
 * @author Ralf Mitschke, Mirko Köhler
 */
class ASMDatabaseTimeProfiler(val database: BytecodeDatabase)
	extends AbstractAnalysesTimeProfiler {

	val analyses = new Analyses(database)

	val usage: String = """|Usage: run-main sae.analyses.profiler.ASMDatabaseProfiler propertiesFile
						  |(c) 2012 Ralf Mitschke (mitschke@st.informatik.tu-darmstadt.de)
						  | """.stripMargin

	def benchmarkType = "ASM Database time"

	//def getAnalysis(query: String, database: BytecodeDatabase)(optimized: Boolean, transactional: Boolean, shared: Boolean): Relation[_]
	def getAnalysis(query: String): Relation[_] =
		analyses (query)

}

object ASMDatabaseTimeProfiler {

	def main(args: Array[String]) {
		val profiler = new ASMDatabaseTimeProfiler(ASMDatabaseFactory.create())

		if (args.length == 0 || !args(0).endsWith(".properties")) {
			println(profiler.usage)
			sys.exit(1)
		}
		val propertiesFile = args(0)

		profiler.execute(propertiesFile)

		sys.exit(0)
	}

}
