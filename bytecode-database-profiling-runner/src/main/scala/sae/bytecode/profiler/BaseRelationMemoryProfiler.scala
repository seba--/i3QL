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

import observers.ArrayBufferObserver
import sae.bytecode._
import sae.Observable
import java.io.FileInputStream
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

        val measureFiles = measure (files, iterations) _

        measureFiles("declared classes", (db: BytecodeDatabase) => Seq (db.declared_classes))

        sys.exit (0)

        val leakStatistic = Statistic (iterations)
        val memStatistic = Statistic (iterations)
        for (i <- 1 to iterations)
        {
            //memory( l => println((l / 1024) + " KB leak"))(measure (files)) // good

            //memory( l => println((l.toDouble / 1024) + " KB leak"))(measure (files)) // good
            //memory (leakStatistic.add (_))(measure (files)) // good

            //memory (leakStatistic.add (_))(memStatistic.add (measure (files))) // good

            memory (leakStatistic.add (_))(memStatistic.add ( MemoryProfiler.dataMemory (files,  (db: BytecodeDatabase) => Seq (db.declared_classes))  )) // good

        }
        println ("leak " + leakStatistic.summary (KiloByte))
        println ("mem  " + memStatistic.summary (MegaByte))


        sys.exit (0)

        def allBaseRelations = (db: BytecodeDatabase) => Seq (
            db.declared_classes,
            db.declared_fields,
            db.declared_methods,
            db.instructions
        )

        def declaredClasses = (db: BytecodeDatabase) => Seq (db.declared_classes)

        def declaredFields = (db: BytecodeDatabase) => Seq (db.declared_fields)

        def declaredMethods = (db: BytecodeDatabase) => Seq (db.declared_methods)

        def instructions = (db: BytecodeDatabase) => Seq (db.instructions)

        measureFiles ("declared classes", declaredClasses)

        measureFiles ("declared fields", declaredFields)

        measureFiles ("declared methods", declaredMethods)

        measureFiles ("instructions", instructions)

        measureFiles ("base relations", allBaseRelations)

        sys.exit (0)
    }


    def measure(files: Seq[java.io.File], iterations: Int)(msg: String, relationSelector: BytecodeDatabase => Seq[Observable[_]]) {
        val leakStatistic = Statistic (iterations)
        val memStatistic = Statistic (iterations)
        for (i <- 1 to iterations)
        {
            memory (leakStatistic.add (_))(memStatistic.add ( MemoryProfiler.dataMemory (files,  relationSelector )))

        }
        println (msg + " memory: " + memStatistic.summary (MegaByte))
        println (msg + " leak  : " + leakStatistic.summary (KiloByte))
    }

    /*
        def report(msg: String)(memory: Long)(implicit unit: MemoryUnit) {
            println (msg + " " + unit.fromBase (memory) + " " + unit.descriptor)
        }
    */


    def measure(files: Seq[java.io.File]): Long = {
        val database = BATDatabaseFactory.create ()

        val classBuffer = new ArrayBufferObserver[ClassDeclaration](10000)

        database.declared_classes.addObserver (classBuffer)
        var mem: Long = 0
        for (file <- files) {
            //memory (l => println (((l / 1024)) + " KB")) {
            memory (l => mem += l) {
                database.addArchive (new FileInputStream (file))
                classBuffer.trim ()
            }
            mem -= classBuffer.bufferConsumption
        }
        println (((mem / 1024)) + " KB")
        mem
    }
}