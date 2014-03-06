package sae.analyses.profiler

import sae.bytecode.{BytecodeDatabase, ASMDatabaseFactory}

/**
 * @author Mirko Köhler
 */
object ASMDatabaseReplayTimeProfiler
	extends BytecodeDatabaseAnalysesReplayProfiler
	with AbstractAnalysesReplayTimeProfiler
{
	override def createBytecodeDatabase: BytecodeDatabase = ASMDatabaseFactory.create()

	override def benchmarkType : String = "time"
	override def queryType : String = "no-lms-opts"
}
