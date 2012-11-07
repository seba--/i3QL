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
package sae.bytecode.analyses.profiler

import java.io.InputStream
import de.tud.cs.st.bat.resolved.analyses.{Analyses, Project}
import de.tud.cs.st.bat.resolved.reader.Java6Framework
import java.util.zip.{ZipEntry, ZipInputStream}
import sae.bytecode.profiler.{TimeMeasurement, AbstractPropertiesFileProfiler}
import sae.bytecode.profiler.statistics.SampleStatistic


/**
 *
 * @author Ralf Mitschke
 *
 */

object BATAnalysesTimeProfiler
    extends AbstractPropertiesFileProfiler
    with TimeMeasurement
{

    val usage: String = """|Usage: java BATAnalysesTimeProfiler propertiesFile
                          |(c) 2012 Ralf Mitschke (mitschke@st.informatik.tu-darmstadt.de)
                          | """.stripMargin


    def measure(iterations: Int, jars: List[String], queries: List[String], reReadJars: Boolean): SampleStatistic = {
        if (reReadJars) {
            measureTime (iterations)(() => applyAnalysesWithJarReading (jars, queries))
        }
        else
        {
            val project = readJars (jars)
            measureTime (iterations)(() => applyAnalysesWithoutJarReading (project, queries))

        }
    }


    def warmup(iterations: Int, jars: List[String], queries: List[String], reReadJars: Boolean): Long = {
        val project =
            if (reReadJars) {
                None
            }
            else
            {
                Some (readJars (jars))
            }

        var i = 0
        while (i < iterations) {
            if (reReadJars) {
                applyAnalysesWithJarReading (jars, queries)
            }
            else
            {
                applyAnalysesWithoutJarReading (project.get, queries)
            }
            i += 1
        }

        resultCount (jars, queries)
    }


    def resultCount(jars: List[String], queries: List[String]): Long = {
        val project = readJars (jars)
        var count: Long = 0
        for (query <- queries) {
            val analysis = Analyses (query)
            val results = analysis.apply (project)
            count += results.size
        }
        count
    }


    def applyAnalysesWithJarReading(jars: List[String], queries: List[String]): Long = {
        var taken: Long = 0
        for (query <- queries) {
            val analysis = Analyses (query)
            time[Iterable[_]] {
                l => taken += l
            }
            {
                val project = readJars (jars)
                analysis.apply (project)
            }
        }
        val memoryMXBean = java.lang.management.ManagementFactory.getMemoryMXBean
        memoryMXBean.gc ()
        print (".")
        taken
    }

    def applyAnalysesWithoutJarReading(project: Project, queries: List[String]): Long = {
        var taken: Long = 0
        for (query <- queries) {
            val analysis = Analyses (query)
            time[Iterable[_]] {
                l => taken += l
            }
            {
                analysis.apply (project)
            }
        }
        val memoryMXBean = java.lang.management.ManagementFactory.getMemoryMXBean
        memoryMXBean.gc ()
        print (".")
        taken
    }


    def readJars(jars: List[String]): Project = {

        var project = new Project ()
        for (entry ← jars)
        {
            val zipStream: ZipInputStream = new ZipInputStream (this.getClass.getClassLoader.getResource (entry).openStream ())
            var zipEntry: ZipEntry = null
            while ((({
                zipEntry = zipStream.getNextEntry
                zipEntry
            })) != null)
            {
                if (!zipEntry.isDirectory && zipEntry.getName.endsWith (".class"))
                {
                    project += Java6Framework.ClassFile (() => new ZipStreamEntryWrapper (zipStream, zipEntry))
                }
            }
        }
        project
    }

    private class ZipStreamEntryWrapper(val stream: ZipInputStream, val entry: ZipEntry) extends InputStream
    {

        private var availableCounter = entry.getCompressedSize.toInt;

        override def close() {
            stream.closeEntry ()
        }

        override def read: Int = {
            availableCounter -= 1
            stream.read
        }

        override def read(b: Array[Byte]) = {
            val read = stream.read (b)
            availableCounter -= read
            read
        }

        override def read(b: Array[Byte], off: Int, len: Int) = {
            val read = stream.read (b, off, len)
            availableCounter -= read
            read
        }

        override def skip(n: Long) = {
            availableCounter -= n.toInt
            stream.skip (n)
        }

        override def available(): Int = {
            availableCounter
        }
    }

}
