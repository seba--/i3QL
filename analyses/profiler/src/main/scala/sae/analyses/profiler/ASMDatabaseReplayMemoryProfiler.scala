package sae.analyses.profiler

import sae.bytecode.{BytecodeDatabase, ASMDatabaseFactory}
import sae.analyses.profiler.interfaces.{BytecodeDatabaseAnalysesReplayProfiler, AbstractAnalysesReplayMemoryProfiler}

/**
 * @author Mirko Köhler
 */
object ASMDatabaseReplayMemoryProfiler
	extends BytecodeDatabaseAnalysesReplayProfiler
	with AbstractAnalysesReplayMemoryProfiler
{
	override def createBytecodeDatabase: BytecodeDatabase = ASMDatabaseFactory.create

	override def benchmarkType : String = "memory"
	override def queryType : String = "default"
}
