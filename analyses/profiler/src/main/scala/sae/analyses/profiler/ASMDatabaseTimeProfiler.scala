package sae.analyses.profiler

import sae.bytecode.{ASMDatabaseFactory, BytecodeDatabaseFactory, BytecodeDatabase}
import idb.Relation
import sae.analyses.findbugs.Analyses
import sae.analyses.profiler.util.BytecodeDatabaseReader

/**
 * @author Ralf Mitschke, Mirko Köhler
 */
class ASMDatabaseTimeProfiler/*(val bytecodeDatabase : BytecodeDatabase)
	extends AbstractAnalysesTimeProfiler {

	val database = new BytecodeDatabaseReader(bytecodeDatabase)

	private val analyses = new Analyses(bytecodeDatabase)

	val usage: String = """|Usage: run-main sae.analyses.profiler.ASMDatabaseProfiler propertiesFile
						  |(c) 2012 Ralf Mitschke (mitschke@st.informatik.tu-darmstadt.de)
						  | """.stripMargin

	def benchmarkType = "ASM Database time"

	def getAnalysis(query: String): Relation[_] =
		analyses (query)

	override var storeResults: Boolean = false

	override def initializeDatabase() {}

	override def disposeDatabase() {}
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

}       */
