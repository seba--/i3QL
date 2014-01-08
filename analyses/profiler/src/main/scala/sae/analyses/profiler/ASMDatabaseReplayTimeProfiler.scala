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
import sae.analyses.profiler.measure.{MilliSeconds, TimeMeasurement}
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

	val usage: String = """|Usage: java SAEAnalysesReplayTimeProfiler propertiesFile
                          |(c) 2012 Ralf Mitschke (mitschke@st.informatik.tu-darmstadt.de)
                          | """.stripMargin

	def getAnalysis(query: String): Relation[_] =
		analyses (query)

	def benchmarkType: String = "ASM databse replay time"

}

object ASMDatabaseReplayTimeProfiler {

	def main(args: Array[String]) {
		val profiler = new ASMDatabaseReplayTimeProfiler(ASMDatabaseFactory.create())

		if (args.length == 0 || !args(0).endsWith(".properties")) {
			println(profiler.usage)
			sys.exit(1)
		}
		val propertiesFile = args(0)

		profiler.execute(propertiesFile)

		sys.exit(0)
	}

}
