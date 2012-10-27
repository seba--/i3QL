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
package sae.bytecode.profiler.statistics

import sae.bytecode.bat.BATDatabaseFactory
import sae.bytecode.LazyMaterializedBytecodeDatabase

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 26.08.12
 * Time: 13:56
 */

object Statistic
{

    def elementStatistic(files: Seq[java.io.File]): Seq[(String, Int)] = {
        val database = new LazyMaterializedBytecodeDatabase (BATDatabaseFactory.create ())
        database.relations.foreach (r => () /* do nothing but iterate over the relations to instantiate*/)
        for (file <- files) {
            database.addArchive (new java.io.FileInputStream (file))
        }
        List (
            ("classes", database.classDeclarations.size),
            ("fields", database.fieldDeclarations.size),
            ("methods", database.methodDeclarations.size),
            ("class inheritance", database.classInheritance.size),
            ("interface inheritance", database.interfaceInheritance.size),
            ("instructions", database.instructions.size)
        )
    }

    def apply(sampleSize: Int): SampleStatistic = {
        new ArrayBufferSampleStatistic (sampleSize)
    }

    def apply(sampleSize: Int, f: () => Long): SampleStatistic = {
        val statistic = new ArrayBufferSampleStatistic (sampleSize)
        var i = 0
        while (i < sampleSize)
        {
            statistic.add (f ())
            i += 1
        }
        statistic
    }


    def apply[T1, T2](sampleSize: Int, f: (T1, T2) => Long)(t1: T1, t2: T2): SampleStatistic = {
        val statistic = new ArrayBufferSampleStatistic (sampleSize)
        var i = 0
        while (i < sampleSize)
        {
            statistic.add (f (t1, t2))
            i += 1
        }
        statistic
    }
}
