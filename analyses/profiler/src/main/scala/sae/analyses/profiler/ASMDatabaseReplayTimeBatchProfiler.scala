package sae.analyses.profiler

import sae.bytecode.{ASMBatchDatabaseFactory, BytecodeDatabase, ASMDatabaseFactory}
import sae.analyses.profiler.interfaces.{AbstractBytcodeDatabaseRunner, AbstractAnalysesReplayTimeProfiler, BytecodeDatabaseAnalysesReplayProfiler}

/**
 * @author Mirko Köhler
 */
object ASMDatabaseReplayTimeBatchProfiler
	extends BytecodeDatabaseAnalysesReplayProfiler
	with AbstractAnalysesReplayTimeProfiler
	with AbstractBytcodeDatabaseRunner
{
	override def createBytecodeDatabase: BytecodeDatabase = ASMBatchDatabaseFactory.create

	override def benchmarkType : String = "time-batch"
	override def queryType : String = "default"


}
