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

import java.io.FileInputStream
import java.util.Properties
import statistics.SampleStatistic
import util.MilliSeconds

/**
 *
 * @author Ralf Mitschke
 *
 */

trait AbstractPropertiesFileProfiler
{
    def main(args: Array[String]) {
        if (args.length == 0 || !args (0).endsWith (".properties")) {
            println (usage)
            sys.exit (1)
        }

        val propertiesFile = args (0)

        val reReadJars =
            if (args.length > 1)
            {
                java.lang.Boolean.parseBoolean (args (1))
            }
            else
            {
                false
            }

        val properties = getProperties (propertiesFile).getOrElse (
        {
            println ("could not find properties file or resource: " + propertiesFile)
            sys.exit (-1)
        }
        )

        val warmupIterations = properties.getProperty ("sae.warmup.iterations").toInt
        val warmupJars = properties.getProperty ("sae.warmup.jars").split (";").toList

        val measurementIterations = properties.getProperty ("sae.benchmark.iterations").toInt
        val measurementJars = properties.getProperty ("sae.benchmark.jars").split (";").toList
        val queries = properties.getProperty ("sae.benchmark.queries").split (";").toList

        val outputFile = properties.getProperty ("sae.benchmark.out", System.getProperty ("user.dir") + "/bench.txt")


        println ("Warmup: " + warmupIterations + " times : " + queries + " on " + warmupJars + " re-read = " + reReadJars)
        val count = warmup (warmupIterations, warmupJars, queries, reReadJars)
        println ("\tdone")
        println ("Num. of Results: " + count)

        val memoryMXBean = java.lang.management.ManagementFactory.getMemoryMXBean
        memoryMXBean.gc ()

        println ("Measure: " + measurementIterations + " times : " + queries + " on " + measurementJars + " re-read = " + reReadJars)
        val statistic = measure (measurementIterations, measurementJars, queries, reReadJars)
        println ("\tdone")
        println (statistic.summary (MilliSeconds))

        sys.exit (0)
    }

    def usage: String


    /**
     * Perform the actual measurement.
     */
    def measure(iterations: Int, jars: List[String], queries: List[String], reReadJars: Boolean): SampleStatistic

    /**
     * Perform the warmup by doing exactly the same operation as in the measurement.
     * The warmup is must return the number of results returned by the measured analyses.
     */
    def warmup(iterations: Int, jars: List[String], queries: List[String], reReadJars: Boolean): Long


    def isFile(propertiesFile: String): Boolean = {
        val file = new java.io.File (propertiesFile)
        file.exists () && file.canRead && !file.isDirectory

    }

    def isResource(propertiesFile: String): Boolean = {
        this.getClass.getClassLoader.getResource (propertiesFile) != null
    }

    def getProperties(propertiesFile: String): Option[Properties] = {
        if (isFile (propertiesFile)) {
            val file = new java.io.File (propertiesFile)
            val properties = new Properties ()
            properties.load (new FileInputStream (file))
            return Some (properties)
        }
        if (isResource (propertiesFile))
        {
            val properties = new Properties ()
            properties.load (this.getClass.getClassLoader.getResource (propertiesFile).openStream ())
            return Some (properties)
        }
        None
    }

}
