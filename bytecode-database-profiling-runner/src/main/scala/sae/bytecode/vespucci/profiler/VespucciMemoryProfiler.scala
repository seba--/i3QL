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
package sae.bytecode.vespucci.profiler

import sae.bytecode.bat.BATDatabaseFactory
import sae._
import analyses.architecture.hibernate.HibernateEnsembles
import bytecode.BytecodeDatabase
import bytecode.profiler.{MemoryUsage, AbstractPropertiesFileProfiler}
import bytecode.profiler.statistics.{SimpleDataStatistic, DataStatistic}
import bytecode.profiler.util.MegaByte
import unisson.model.UnissonDatabase

/**
 *
 * @author Ralf Mitschke
 *
 */

object VespucciMemoryProfiler
    extends AbstractPropertiesFileProfiler
    with MemoryUsage
{
    val usage: String = """|Usage: java VespucciMemoryProfiler propertiesFile
                          |(c) 2012 Ralf Mitschke (mitschke@st.informatik.tu-darmstadt.de)
                          | """.stripMargin

    def benchmarkType = "SAE Vespucci"

    def getAnalysis(query: String, database: UnissonDatabase): Relation[_] = {
        query match {
            case "SOURCE_DEPENDENCIES" => database.source_code_dependencies
            case "ENSEMBLE_DEPENDENCIES" => database.ensemble_dependencies
        }
    }

    def dataStatistic(jars: List[String]): DataStatistic = {
        var database = BATDatabaseFactory.create ()

        val classes = relationToResult (database.classDeclarations)

        val methods = relationToResult (database.methodDeclarations)

        val fields = relationToResult (database.fieldDeclarations)

        val instructions = relationToResult (database.instructions)

        jars.foreach (jar => {
            val stream = this.getClass.getClassLoader.getResourceAsStream (jar)
            database.addArchive (stream)
            stream.close ()
        })

        SimpleDataStatistic (classes.size, methods.size, fields.size, instructions.size)
    }


    def measurementUnit = MegaByte

    def measure(iterations: Int, jars: List[String], queries: List[String]) = {
        measureMemory (iterations)(() => createVanillaDatabase (jars, queries))._1
    }

    def warmup(iterations: Int, jars: List[String], queries: List[String]) = {
        var i = 0
        while (i < iterations) {
            createVanillaDatabase (jars, queries)
            i += 1
        }
        dependencyCount (jars, queries)
    }


    def createVanillaDatabase(jars: List[String], queries: List[String]): Long = {
        var taken: Long = 0
        var database = BATDatabaseFactory.create ()
        var unisson = new UnissonDatabase (database)
        val res = unisson.ensemble_elements
        memory {
            l => taken += l
        }
        {
            unisson.setRepository(HibernateEnsembles.Hibernate_3_6_6)
            jars.foreach (jar => {
                val stream = this.getClass.getClassLoader.getResourceAsStream (jar)
                database.addArchive (stream)
                stream.close ()
            })
        }


        database = null
        unisson = null
        val memoryMXBean = java.lang.management.ManagementFactory.getMemoryMXBean
        memoryMXBean.gc ()
        print (".")
        taken
    }


    def dependencyCount(jars: List[String], queries: List[String]): Long = {
        var database = BATDatabaseFactory.create ()
        var unisson = new UnissonDatabase (database)
        val res = sae.relationToResult (unisson.ensemble_elements)
        unisson.setRepository(HibernateEnsembles.Hibernate_3_6_6)
        jars.foreach (jar => {
            val stream = this.getClass.getClassLoader.getResourceAsStream (jar)
            database.addArchive (stream)
            stream.close ()
        })


        database = null
        unisson = null
        val memoryMXBean = java.lang.management.ManagementFactory.getMemoryMXBean
        memoryMXBean.gc ()
        res.size
    }
}
