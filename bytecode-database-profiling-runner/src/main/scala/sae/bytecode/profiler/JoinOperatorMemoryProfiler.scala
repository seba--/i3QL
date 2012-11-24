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

import java.io.File
import sae.bytecode.BytecodeDatabase
import sae.syntax.sql._
import sae.bytecode.instructions.{INVOKEVIRTUAL, INVOKESPECIAL, InstructionInfo}
import util.{Byte, MegaByte}
import sae.Relation

/**
 *
 * @author Ralf Mitschke
 *
 */

object JoinOperatorMemoryProfiler
    extends AbstractMemoryProfiler
{
    def warmUp(implicit files: Seq[File]) {
        // warmup
        print ("warmup")
        for (i <- 1 to warmupIterations) {
            count (joinConsecutiveInstructions)
            print (".")
        }
        println ("")

    }

    def profile(implicit files: Seq[File]) {
        implicit val iter = 50
        val joinCount = count (joinConsecutiveInstructions)

        val (joinData, _) = measureDataMemory (joinConsecutiveInstructions)
        val (joinComp, _) = measureComputationMemory (joinConsecutiveInstructions)

        println ("join -- data:               " + (joinData).summary (MegaByte))
        println ("join -- computation + data: " + (joinComp).summary (MegaByte))
        println ("join -- computation/unit:   " + (joinComp - joinData).summaryPerUnit (joinCount)(Byte))
    }


    def joinConsecutiveInstructions(db: BytecodeDatabase) = {
        import sae.bytecode._
        //val invokeSpecial: Relation[INVOKESPECIAL] = SELECT ((_: InstructionInfo).asInstanceOf[INVOKESPECIAL]) FROM db.instructions WHERE (_.isInstanceOf[INVOKESPECIAL])
        //val invokeVirtual: Relation[INVOKEVIRTUAL] = SELECT ((_: InstructionInfo).asInstanceOf[INVOKEVIRTUAL]) FROM db.instructions WHERE (_.isInstanceOf[INVOKEVIRTUAL])

        import sae.syntax.RelationalAlgebraSyntax._

        val invokeSpecial = Π((_: InstructionInfo).asInstanceOf[INVOKESPECIAL])( σ((_:InstructionInfo).isInstanceOf[INVOKESPECIAL])(db.instructions) ).forceToSet

        val invokeVirtual = Π((_: InstructionInfo).asInstanceOf[INVOKEVIRTUAL])( σ((_:InstructionInfo).isInstanceOf[INVOKEVIRTUAL])(db.instructions) ).forceToSet

        compile (
            SELECT (*) FROM
                (invokeSpecial, invokeVirtual) WHERE
                (declaringMethod === declaringMethod) AND
                (sequenceIndex === ((second: INVOKEVIRTUAL) => second.sequenceIndex - 1))

        )
    }
}
