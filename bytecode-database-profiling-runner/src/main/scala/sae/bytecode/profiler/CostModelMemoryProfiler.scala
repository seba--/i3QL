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

import sae.syntax.sql._
import sae.bytecode._
import bat.BATDatabaseFactory
import statistics.Statistic
import util.{MegaByte, RelationMap}


/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 23.08.12
 * Time: 15:52
 *
 * The memory profiler computes a cost model for different concerns of the database:<br>
 * Cost of the data for different relations: which includes symbols (strings) and referenced objects
 */
object CostModelMemoryProfiler
    extends MemoryUsage
{
    val usage = """|Usage: java CostModelMemoryProfiler <ZIP or JAR file containing class files>+
                  |(c) 2012 Ralf Mitschke (mitschke@st.informatik.tu-darmstadt.de)
                  | """.stripMargin

    private val iterations = 20

    private val warmup = 0

    def main(args: Array[String]) {
        if (args.length == 0 || !args.forall (arg ⇒ arg.endsWith (".zip") || arg.endsWith (".jar"))) {
            println (usage)
            sys.exit (1)
        }

        val q = (db: BytecodeDatabase) => compile(SELECT (classType) FROM db.classDeclarations)
        val x = q(BATDatabaseFactory.create())

        val files = for (arg ← args) yield {
            val file = new java.io.File (arg)
            if (!file.canRead || file.isDirectory) {
                println ("The file: " + file + " cannot be read.")
                println (usage)
                sys.exit (1)
            }
            file
        }


        println ("statistics")
        Statistic.elementStatistic (files).map (e => e._1 + ": " + e._2).foreach (println)

        val elementCount = elementCounts (files)

        // warmup
        print ("warmup")
        for (i <- 1 to warmup) {
            MemoryProfiler.memoryOfMaterializedData (files) ((db: BytecodeDatabase) => Seq (
                db.classDeclarations,
                db.fieldDeclarations,
                db.methodDeclarations,
                db.classInheritance,
                db.interfaceInheritance,
                db.instructions
            ))
            print (".")
        }
        println ("")

        classDeclarations(iterations, files)
    }


    def classDeclarations(iterations: Int, files: Seq[java.io.File]) {

        val mem = MemoryProfiler.measureMemory (iterations) _

        val classDataFunction = () => MemoryProfiler.memoryOfData (files)((db: BytecodeDatabase) => Seq (db.classDeclarations))

        val classTypeDataFunction = () => MemoryProfiler.memoryOfData (files)((db: BytecodeDatabase) => Seq (
            SELECT (classType)  FROM db.classDeclarations
        )
        )
        val (classMem, classLeak) = mem (classDataFunction)
        val (typeMem, typeLeak) = mem (classTypeDataFunction)
        println(classMem.summary(MegaByte))
        println(typeMem.summary(MegaByte))
    }

    def elementCounts(files: Seq[java.io.File]): RelationMap[Int] = {
        val database = new MaterializedBytecodeDatabase (BATDatabaseFactory.create ())
        database.relations.foreach (r => () /* do nothing but iterate over the relations to instantiate*/)
        for (file <- files) {
            database.addArchive (new java.io.FileInputStream (file))
        }
        val map = new RelationMap[Int]
        map ((db: BytecodeDatabase) => db.classDeclarations) = database.classDeclarations.size
        map ((db: BytecodeDatabase) => db.methodDeclarations) = database.methodDeclarations.size
        map ((db: BytecodeDatabase) => db.fieldDeclarations) = database.fieldDeclarations.size
        map ((db: BytecodeDatabase) => db.classInheritance) = database.classInheritance.size
        map ((db: BytecodeDatabase) => db.interfaceInheritance) = database.interfaceInheritance.size
        map ((db: BytecodeDatabase) => db.instructions) = database.instructions.size
        map
    }
}