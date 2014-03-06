package sae.analyses.profiler

import sae.bytecode.{BytecodeDatabase, ASMDatabaseFactory}

/**
 * @author Mirko Köhler
 */
object ASMDatabaseReplayMemoryProfiler
	extends BytecodeDatabaseAnalysesReplayProfiler
	with AbstractAnalysesReplayMemoryProfiler
{
	override def createBytecodeDatabase: BytecodeDatabase = ASMDatabaseFactory.create()

	override def benchmarkType : String = "memory"
	override def queryType : String = "no-opts"
}
