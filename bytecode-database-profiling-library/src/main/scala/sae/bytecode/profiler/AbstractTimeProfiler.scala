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

import observers.CountingObserver
import sae.bytecode.BytecodeDatabase
import sae.{Observable, LazyView}
import java.io.FileInputStream
import sae.bytecode.bat.BATDatabaseFactory
import sae.collections.Conversions

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 01.09.12
 * Time: 14:09
 */

trait AbstractTimeProfiler
    extends AbstractJarProfiler
    with TimeMeasurement
{

    def operations: BytecodeDatabase => Seq[Observable[_]]

    def profile(implicit files: Seq[java.io.File])

    def warmUp(files: Seq[java.io.File]) {
        // warmup
        print ("warmup")
        for (i <- 1 to warmupIterations) {
            MemoryProfiler.memoryOfMaterializedData (files)((db: BytecodeDatabase) => Seq (
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
    }

    def measure[V <: AnyRef](f: BytecodeDatabase => LazyView[V]): BytecodeDatabase => Seq[Observable[_]] = {
        (db: BytecodeDatabase) => Seq (f (db)).asInstanceOf[Seq[Observable[_]]] ++ db.relations.asInstanceOf[Seq[Observable[_]]]
    }

    /**
     * Measure the time taken by computing the given views.
     * The results are stored in QueryResults, hence in hashtables
     */
    def computeViewAsResult(files: Seq[java.io.File])(views: BytecodeDatabase => Seq[LazyView[_ <: AnyRef]]): Long = {
        val database: BytecodeDatabase = BATDatabaseFactory.create ()

        val results = for (view <- views (database)) yield {
            Conversions.lazyViewToResult (view)
        }

        var taken: Long = 0
        for (file <- files) {
            time (t => (taken += t)) {
                database.addArchive (new FileInputStream (file))
            }
        }

        results.foreach(println(_))
        taken
    }

    def computeViewAsCount(files: Seq[java.io.File])(views: BytecodeDatabase => Seq[LazyView[_ <: AnyRef]]): Long = {
        val database: BytecodeDatabase = BATDatabaseFactory.create ()

        val results = for (view <- views (database)) yield {
            val o = new CountingObserver[AnyRef]
            view.addObserver (o)
            o
        }

        var taken: Long = 0
        for (file <- files) {
            time (t => (taken += t)) {
                database.addArchive (new FileInputStream (file))
            }
        }

        results.foreach(println(_))
        taken
    }


}
