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

import sae.bytecode.bat.BATDatabaseFactory
import sae.bytecode.{MaterializedBytecodeDatabase, BytecodeDatabase}
import sae._
import bytecode.profiler.AbstractPropertiesFileProfiler
import bytecode.profiler.statistics.{SimpleDataStatistic, DataStatistic}


/**
 *
 * @author Ralf Mitschke
 *
 */

abstract class SAEAnalysesProfiler
    extends AbstractPropertiesFileProfiler
{

    def getAnalysis(query: String, database: BytecodeDatabase)(implicit optimized: Boolean = false): Relation[_]


    def createMaterializedDatabase(jars: List[String], queries: List[String], transactional: Boolean) = {
        val database = BATDatabaseFactory.create ()
        val materializedDatabase = new MaterializedBytecodeDatabase (database)

        // initialize the needed materializations at least once
        val relations = for (query <- queries) yield {
            getAnalysis (query, materializedDatabase)(optimized)
        }


        jars.foreach (jar => {
            val stream = this.getClass.getClassLoader.getResourceAsStream (jar)
            if (transactional)
            {
                database.addArchiveAsClassFileTransactions(stream)
            }
            else
            {
                database.addArchive (stream)
            }
            stream.close ()
        })

        relations.foreach (_.clearObserversForChildren (visitChild => true))

        materializedDatabase
    }


    def getResultsWithReadingJars(jars: List[String], queries: List[String], transactional: Boolean): Long = {
        var database = BATDatabaseFactory.create ()
        val results = for (query <- queries) yield {
            sae.relationToResult (getAnalysis (query, database)(optimized))
        }
        jars.foreach (jar => {
            val stream = this.getClass.getClassLoader.getResourceAsStream (jar)
            if (transactional)
            {
                database.addArchiveAsClassFileTransactions(stream)
            }
            else
            {
                database.addArchive (stream)
            }
            stream.close ()
        })

        results.map (_.size).sum
    }

    def getResultsWithoutReadingJars(jars: List[String], queries: List[String], transactional: Boolean): Long = {
        val database = createMaterializedDatabase (jars, queries, transactional)
        val results = for (query <- queries) yield {
            //sae.relationToResult (AnalysesOO (query, database))
            getAnalysis (query, database)(optimized)
        }

        results.map (_.size).sum
    }


    def dataStatistic(jars: List[String], transactional: Boolean): DataStatistic = {
        var database = BATDatabaseFactory.create ()

        val classes = relationToResult (database.classDeclarations)

        val methods = relationToResult (database.methodDeclarations)

        val fields = relationToResult (database.fieldDeclarations)

        val instructions = relationToResult (database.instructions)

        jars.foreach (jar => {
            val stream = this.getClass.getClassLoader.getResourceAsStream (jar)
            if (transactional)
            {
                database.addArchiveAsClassFileTransactions(stream)
            }
            else
            {
                database.addArchive (stream)
            }
            stream.close ()
        })

        SimpleDataStatistic (classes.size, methods.size, fields.size, instructions.size)
    }

}
