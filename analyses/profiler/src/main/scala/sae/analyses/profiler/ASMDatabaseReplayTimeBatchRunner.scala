package sae.analyses.profiler

import sae.analyses.profiler.interfaces.{AbstractPropertiesFileReplayProfiler, AbstractBytcodeDatabaseRunner, BytecodeDatabaseAnalysesReplayProfiler}

/**
 * @author Mirko Köhler
 */
object ASMDatabaseReplayTimeBatchRunner extends AbstractBytcodeDatabaseRunner {
	override var profiler: BytecodeDatabaseAnalysesReplayProfiler with AbstractPropertiesFileReplayProfiler = ASMDatabaseReplayTimeBatchProfiler
}
