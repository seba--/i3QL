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
	override var benchmarkType: String = "ASM-database-replay-time"
}
