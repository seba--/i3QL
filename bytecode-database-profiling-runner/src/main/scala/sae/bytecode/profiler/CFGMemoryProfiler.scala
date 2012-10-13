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
import util.{MegaByte, Byte}
import sae.bytecode.BytecodeDatabase
import sae.{Observable, Relation}

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 12.09.12
 * Time: 21:35
 */

object CFGMemoryProfiler
    extends AbstractMemoryProfiler
{
    override def warmupIterations: Int = 1


    def profile(implicit files: Seq[File]) {
        implicit val iter = 5

        //val (databaseMemory, _) = dataMemory((db: BytecodeDatabase) => db.relations)

        val (methodDeclarations, _) = measureDataMemory ((db: BytecodeDatabase) => db.methodDeclarations)
        val (basicBlockEndPcs, _) = measureDataMemory ((db: BytecodeDatabase) => db.basicBlockEndPcs)
        val (immediateBasicBlockSuccessorEdges, _) = measureDataMemory ((db: BytecodeDatabase) => db.immediateBasicBlockSuccessorEdges)
        val (fallThroughCaseSuccessors, _) = measureDataMemory ((db: BytecodeDatabase) => db.fallThroughCaseSuccessors)
        val (basicBlockSuccessorEdges, _) = measureDataMemory ((db: BytecodeDatabase) => db.basicBlockSuccessorEdges)
        val (basicBlockStartPcs, _) = measureDataMemory ((db: BytecodeDatabase) => db.basicBlockStartPcs)
        val (basicBlocks, _) = measureDataMemory ((db: BytecodeDatabase) => db.basicBlocks)

        println ("methodDeclarations:                " + (methodDeclarations).summary (MegaByte))
        println ("methodDeclaration:                 " + (methodDeclarations).summaryPerUnit(count((db: BytecodeDatabase) => db.methodDeclarations))(Byte))
        println ("basicBlockEndPcs:                  " + (basicBlockEndPcs).summary (MegaByte))
        println ("basicBlockEndPc:                   " + (basicBlockEndPcs - methodDeclarations).summaryPerUnit(count((db: BytecodeDatabase) => db.basicBlockEndPcs))(Byte))
        println ("immediateBasicBlockSuccessorEdges: " + (immediateBasicBlockSuccessorEdges).summary (MegaByte))
        println ("immediateBasicBlockSuccessorEdge:  " + (immediateBasicBlockSuccessorEdges - methodDeclarations).summaryPerUnit(count((db: BytecodeDatabase) => db.immediateBasicBlockSuccessorEdges))(Byte))
        println ("fallThroughCaseSuccessors:         " + (fallThroughCaseSuccessors).summary (MegaByte))
        println ("fallThroughCaseSuccessor:          " + (fallThroughCaseSuccessors - methodDeclarations).summaryPerUnit(count((db: BytecodeDatabase) => db.fallThroughCaseSuccessors))(Byte))
        println ("basicBlockSuccessorEdges:          " + (basicBlockSuccessorEdges).summary (MegaByte))
        println ("basicBlockSuccessorEdge:           " + (basicBlockSuccessorEdges - methodDeclarations).summaryPerUnit(count((db: BytecodeDatabase) => db.basicBlockSuccessorEdges))(Byte))
        println ("basicBlockStartPcs:                " + (basicBlockStartPcs).summary (MegaByte))
        println ("basicBlockStartPc:                 " + (basicBlockStartPcs - methodDeclarations).summaryPerUnit(count((db: BytecodeDatabase) => db.basicBlockStartPcs))(Byte))
        println ("basicBlocks:                       " + (basicBlocks).summary (MegaByte))
        println ("basicBlock:                        " + (basicBlocks - methodDeclarations).summaryPerUnit(count((db: BytecodeDatabase) => db.basicBlocks))(Byte))


    }


}
