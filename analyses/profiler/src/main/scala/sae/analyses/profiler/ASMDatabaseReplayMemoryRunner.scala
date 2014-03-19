package sae.analyses.profiler

import sae.analyses.profiler.interfaces.{AbstractPropertiesFileReplayProfiler, BytecodeDatabaseAnalysesReplayProfiler, AbstractBytcodeDatabaseRunner}

/**
  * @author Mirko Köhler
  */
object ASMDatabaseReplayMemoryRunner extends AbstractBytcodeDatabaseRunner {
	 override var profiler: BytecodeDatabaseAnalysesReplayProfiler with AbstractPropertiesFileReplayProfiler = ASMDatabaseReplayMemoryProfiler
 }
