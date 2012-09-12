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
package sae.analyses.findbugs.profiler

import sae.bytecode.profiler.AbstractMemoryProfiler
import java.io.File
import sae.bytecode._
import profiler.util.MegaByte
import sae.analyses.findbugs.{BX_BOXING_IMMEDIATELY_UNBOXED_TO_PERFORM_COERCION, CI_CONFUSED_INHERITANCE}
import sae.bytecode.profiler.MemoryProfiler._
import sae.{Observable, LazyView}

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 01.09.12
 * Time: 14:08
 */

object AnalysesMemoryProfiler
    extends AbstractMemoryProfiler
{
    def profile(implicit files: Seq[File]) {
        implicit val iter = iterations

        val (databaseMemory, _) = dataMemory((db: BytecodeDatabase) => db.relations)

        val (memory_CI_CONFUSED_INHERITANCE,_) = dataMemory(measure (CI_CONFUSED_INHERITANCE))

        val (memory_BX_BOXING_IMMEDIATELY_UNBOXED_TO_PERFORM_COERCION, _) = dataMemory(measure (BX_BOXING_IMMEDIATELY_UNBOXED_TO_PERFORM_COERCION))

        println("CI_CONFUSED_INHERITANCE: " + (memory_CI_CONFUSED_INHERITANCE - databaseMemory).summary(MegaByte))

        println("BX_BOXING_IMMEDIATELY_UNBOXED_TO_PERFORM_COERCION: " + (memory_BX_BOXING_IMMEDIATELY_UNBOXED_TO_PERFORM_COERCION - databaseMemory).summary(MegaByte))
    }


}
