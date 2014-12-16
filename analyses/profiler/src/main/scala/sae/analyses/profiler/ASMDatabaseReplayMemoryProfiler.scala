package sae.analyses.profiler

import sae.bytecode.{BytecodeDatabase, ASMDatabaseFactory}
import sae.analyses.profiler.interfaces.{AbstractPropertiesFileReplayProfiler, AbstractBytcodeDatabaseRunner, BytecodeDatabaseAnalysesReplayProfiler, AbstractAnalysesReplayMemoryProfiler}

/**
 * @author Mirko Köhler
 */
object ASMDatabaseReplayMemoryProfiler
	extends BytecodeDatabaseAnalysesReplayProfiler
	with AbstractAnalysesReplayMemoryProfiler
	with AbstractBytcodeDatabaseRunner
{

	override def createBytecodeDatabase: BytecodeDatabase = ASMDatabaseFactory.create

	override def benchmarkType : String = "memory"
	override def queryType : String = "default"


}
