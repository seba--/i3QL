package sae.analyses.profiler

/**
 * @author Mirko Köhler
 */
object ASMDatabaseReplayTimeRunner extends AbstractBytcodeDatabaseRunner {
	override var profiler: BytecodeDatabaseAnalysesReplayProfiler with AbstractPropertiesFileReplayProfiler = ASMDatabaseReplayTimeProfiler
}
