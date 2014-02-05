/* License (BSD Style License):
 *  Copyright (c) 2009, 2011
 *  Software Technology Group
 *  Department of Computer Science
 *  Technische Universität Darmstadt
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  - Neither the name of the Software Technology Group or Technische
 *    Universität Darmstadt nor the names of its contributors may be used to
 *    endorse or promote products derived from this software without specific
 *    prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 */
package sae.analyses.profiler


import java.io.FileInputStream
import sae.analyses.profiler.measure.TimeMeasurement
import idb.Relation
import sae.analyses.profiler.util.ReplayEvent
import sae.analyses.profiler.statistics.SampleStatistic
import sae.bytecode.{ASMDatabaseFactory, BytecodeDatabase}
import sae.analyses.findbugs.Analyses


/**
 *
 * @author Ralf Mitschke, Mirko Köhler
 *
 */

class ASMDatabaseReplayTimeProfiler(val database : BytecodeDatabase)
    extends AbstractAnalysesReplayTimeProfiler
{
	private val analyses = new Analyses(database)

	def getAnalysis(query: String): Relation[_] =
		analyses (query)

	def benchmarkType: String = "ASM-database-replay-time"

}

object ASMDatabaseReplayTimeProfiler {

	def main(args: Array[String]) {


		if (args.length == 0) {
            System.err.println("No properties file specified.")
			sys.exit(1)
		}

        for (i <- 0 until args.length) {
            val propertiesFile = args(i)

            println("Running analyses for properties file " + propertiesFile +"...")

            var profiler = new ASMDatabaseReplayTimeProfiler(ASMDatabaseFactory.create())
            profiler.execute(propertiesFile)

            profiler = null

            val memoryMXBean = java.lang.management.ManagementFactory.getMemoryMXBean
            memoryMXBean.gc ()
        }


		sys.exit(0)
	}

}
