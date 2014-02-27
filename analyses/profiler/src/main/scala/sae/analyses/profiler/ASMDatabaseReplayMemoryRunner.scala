package sae.analyses.profiler

/**
  * @author Mirko Köhler
  */
object ASMDatabaseReplayMemoryRunner extends AbstractBytcodeDatabaseRunner {
	 override var profiler: BytecodeDatabaseAnalysesReplayProfiler with AbstractPropertiesFileReplayProfiler = ASMDatabaseReplayMemoryProfiler
 }
