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
	override var benchmarkType: String = "ASM-database-replay-memory"
}
