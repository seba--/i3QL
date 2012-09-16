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
package sae.bytecode.profiler

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 16.09.12
 * Time: 20:52
 */

trait AbstractJarProfiler
{
    val usage = """|Usage: java BaseRelationMemoryProfiler <ZIP or JAR file containing class files>+
                  |(c) 2012 Ralf Mitschke (mitschke@st.informatik.tu-darmstadt.de)
                  | """.stripMargin

    def iterations = 20

    def warmupIterations: Int = 10

    def main(args: Array[String]) {
        if (args.length == 0 || !args.forall (arg ⇒ arg.endsWith (".zip") || arg.endsWith (".jar"))) {
            println (usage)
            sys.exit (1)
        }

        val files = for (arg ← args) yield {
            val file = new java.io.File (arg)
            if (!file.canRead || file.isDirectory) {
                println ("The file: " + file + " cannot be read.")
                println (usage)
                sys.exit (1)
            }
            file
        }

        warmUp (files)

        profile (files)
    }

    def profile(implicit files: Seq[java.io.File])

    def warmUp(files: Seq[java.io.File])
}
