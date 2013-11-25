package sae.analyses.profiler

import sae.bytecode.{ASMDatabaseFactory, BytecodeDatabaseFactory, BytecodeDatabase}
import idb.Relation
import sae.analyses.findbugs.Analyses

/**
 * @author Ralf Mitschke, Mirko Köhler
 */
object ASMDatabaseTimeProfiler
  extends AbstractAnalysesTimeProfiler {


  def benchmarkType = "ASM Database time"

  def databaseFactory: BytecodeDatabaseFactory = ASMDatabaseFactory

  //def getAnalysis(query: String, database: BytecodeDatabase)(optimized: Boolean, transactional: Boolean, shared: Boolean): Relation[_]
  def getAnalysis(query: String, database: BytecodeDatabase): Relation[_] =
    Analyses (query) (database)

}
