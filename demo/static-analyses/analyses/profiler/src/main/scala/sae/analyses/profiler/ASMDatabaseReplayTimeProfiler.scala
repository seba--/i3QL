package sae.analyses.profiler

import sae.bytecode.{BytecodeDatabase, ASMDatabaseFactory}
import sae.analyses.profiler.interfaces.{AbstractBytcodeDatabaseRunner, AbstractAnalysesReplayTimeProfiler, BytecodeDatabaseAnalysesReplayProfiler}

/**
 * @author Mirko Köhler
 */
object ASMDatabaseReplayTimeProfiler
	extends BytecodeDatabaseAnalysesReplayProfiler
	with AbstractAnalysesReplayTimeProfiler
	with AbstractBytcodeDatabaseRunner
{
	override def createBytecodeDatabase: BytecodeDatabase = ASMDatabaseFactory.create

	override def benchmarkType : String = "time"
	override def queryType : String = "default"


}
