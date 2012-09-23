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

import sae.bytecode._
import statistics.Statistic
import util.{MegaByte, KiloByte}


/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 23.08.12
 * Time: 15:52
 *
 *
 */
object BaseRelationMemoryProfiler
    extends MemoryUsage
{
    val usage = """|Usage: java BaseRelationMemoryProfiler <ZIP or JAR file containing class files>+
                  |(c) 2012 Ralf Mitschke (mitschke@st.informatik.tu-darmstadt.de)
                  | """.stripMargin

    private val iterations = 20

    private val warmup = 10

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

        val measureMem = measureMemory (iterations) _

        val baseMemory = MemoryProfiler.memoryOfData (files) _

        val materializedMemory = MemoryProfiler.memoryOfMaterializedData (files) _

        println ("statistics")
        Statistic.elementStatistic (files).map (e => e._1 + ": " + e._2).foreach (println)


        // warmup
        print ("warmup")
        for (i <- 1 to warmup) {
            materializedMemory ((db: BytecodeDatabase) => Seq (
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

        measureMem ("class inheritance - indices", () => baseMemory ((db: BytecodeDatabase) => {
            Seq (
                db.classInheritance.index (_.subType),
                db.classInheritance.index (_.superType)
            )
        }))

        measureMem ("declared classes - data", () => baseMemory ((db: BytecodeDatabase) => Seq (db.classDeclarations)))
        measureMem ("declared methods - data", () => baseMemory ((db: BytecodeDatabase) => Seq (db.methodDeclarations)))
        measureMem ("declared fields  - data", () => baseMemory ((db: BytecodeDatabase) => Seq (db.fieldDeclarations)))
        measureMem ("class inheritance - data", () => baseMemory ((db: BytecodeDatabase) => Seq (db.classInheritance)))
        measureMem ("interface inheritance - data", () => baseMemory ((db: BytecodeDatabase) => Seq (db.interfaceInheritance)))
        measureMem ("declared structures - data", () => baseMemory ((db: BytecodeDatabase) => Seq (
            db.classDeclarations,
            db.fieldDeclarations,
            db.methodDeclarations
        )))
        measureMem ("inheritance - data", () => baseMemory ((db: BytecodeDatabase) => Seq (
            db.classInheritance,
            db.interfaceInheritance
        )))
        measureMem ("instructions - data", () => baseMemory ((db: BytecodeDatabase) => Seq (db.instructions)))
        measureMem ("base relations - data", () => baseMemory ((db: BytecodeDatabase) => Seq (
            db.classDeclarations,
            db.fieldDeclarations,
            db.methodDeclarations,
            db.classInheritance,
            db.interfaceInheritance,
            db.instructions
        )))



        measureMem ("declared classes - materialized", () => materializedMemory ((db: BytecodeDatabase) => Seq (db.classDeclarations)))
        measureMem ("declared methods - materialized", () => materializedMemory ((db: BytecodeDatabase) => Seq (db.methodDeclarations)))
        measureMem ("declared fields  - materialized", () => materializedMemory ((db: BytecodeDatabase) => Seq (db.fieldDeclarations)))
        measureMem ("class inheritance - materialized", () => materializedMemory ((db: BytecodeDatabase) => Seq (db.classInheritance)))
        measureMem ("interface inheritance - materialized", () => materializedMemory ((db: BytecodeDatabase) => Seq (db.interfaceInheritance)))
        measureMem ("declared structures - materialized", () => materializedMemory ((db: BytecodeDatabase) => Seq (
            db.classDeclarations,
            db.fieldDeclarations,
            db.methodDeclarations
        )))
        measureMem ("inheritance - materialized", () => baseMemory ((db: BytecodeDatabase) => Seq (
            db.classInheritance,
            db.interfaceInheritance
        )))
        measureMem ("instructions - materialized", () => materializedMemory ((db: BytecodeDatabase) => Seq (db.instructions)))
        measureMem ("base relations - materialized", () => materializedMemory ((db: BytecodeDatabase) => Seq (
            db.classDeclarations,
            db.fieldDeclarations,
            db.methodDeclarations,
            db.classInheritance,
            db.interfaceInheritance,
            db.instructions
        )))

    }


    def measureMemory(iterations: Int)(msg: String, f: () => Long) {
        val leakStatistic = Statistic (iterations)
        val memStatistic = Statistic (iterations)
        for (i <- 1 to iterations)
        {
            memory (leakStatistic.add (_))(memStatistic.add (f ()))

        }
        println (msg + " memory: " + memStatistic.summary (MegaByte))
        println (msg + " leak  : " + leakStatistic.summary (KiloByte))
    }

}