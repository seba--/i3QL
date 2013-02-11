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
import de.tud.cs.st.bat.resolved.analyses.Project
import de.tud.cs.st.bat.resolved.reader.Java6Framework
import java.util.zip.{ZipEntry, ZipInputStream}
import sae.bytecode.profiler.{TimeMeasurement, AbstractPropertiesFileProfiler}
import sae.bytecode.profiler.statistics.{SimpleDataStatistic, DataStatistic, SampleStatistic}
import sae.bytecode.profiler.util.MilliSeconds
import de.tud.cs.st.bat.resolved.ClassFile


/**
 *
 * @author Ralf Mitschke
 *
 */

object BATProjectConstructionTimeProfiler
    extends AbstractPropertiesFileProfiler
    with TimeMeasurement
{

    val usage: String = """|Usage: java BATAnalysesTimeProfiler propertiesFile
                          |(c) 2012 Ralf Mitschke (mitschke@st.informatik.tu-darmstadt.de)
                          | """.stripMargin


    def measure(iterations: Int, jars: List[String], queries: List[String]): SampleStatistic = {
        if (reReadJars) {
            measureTime (iterations)(() => makeProjectWithJarReading (jars))
        }
        else
        {
            val classFiles = readJars (jars)
            measureTime (iterations)(() => makeProjectWithoutJarReading (classFiles))

        }
    }


    def warmup(iterations: Int, jars: List[String], queries: List[String]): Long = {
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
                makeProjectWithJarReading (jars)
            }
            else
            {
                makeProjectWithoutJarReading (project.get)
            }
            i += 1
        }

        resultCount (jars, queries)
    }


    def resultCount(jars: List[String], queries: List[String]): Long = {
        0L
    }


    def makeProjectWithJarReading(jars: List[String]): Long = {
        var taken: Long = 0
        time {
            l => taken += l
        }
        {
            val project = new Project ()
            val classFiles = readJars (jars)
            project ++ classFiles
        }
        val memoryMXBean = java.lang.management.ManagementFactory.getMemoryMXBean
        memoryMXBean.gc ()
        print (".")
        taken
    }

    def makeProjectWithoutJarReading(classFiles: List[ClassFile]): Long = {
        var taken: Long = 0
        time {
            l => taken += l
        }
        {
            val project = new Project ()
            project ++ classFiles
        }
        val memoryMXBean = java.lang.management.ManagementFactory.getMemoryMXBean
        memoryMXBean.gc ()
        print (".")
        taken
    }


    def readJars(jars: List[String]): List[ClassFile] = {
        var classsFiles: List[ClassFile] = Nil
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
                    classsFiles ::= Java6Framework.ClassFile (() => new ZipStreamEntryWrapper (zipStream, zipEntry))
                }
            }
        }
        classsFiles
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


    def measurementUnit = MilliSeconds

    def dataStatistic(jars: List[String]): DataStatistic = {
        val classFiles = readJars (jars)

        val classCount = classFiles.size
        val methods = classFiles.flatMap (_.methods)
        val methodCount = methods.size
        val fields = classFiles.flatMap (_.fields)
        val fieldCount = fields.size
        val instructions =
            for {method <- methods
                 if method.body.isDefined
                 instructions = method.body.get.instructions
                 instruction <- instructions
                 if (instruction != null)
            } yield instruction
        val instructionCount = instructions.size
        SimpleDataStatistic (classCount, methodCount, fieldCount, instructionCount)
    }

    def benchmarkType = "BAT time"
}
