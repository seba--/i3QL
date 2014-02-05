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
import util.Byte
import sae.bytecode.BytecodeDatabase
import sae.analyses.profiler.measure.units.MebiByte

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 12.09.12
 * Time: 21:35
 */

object CFGMemoryProfiler
    extends AbstractMemoryProfiler
{
    override def warmupIterations: Int = 20

    def warmUp(implicit files: Seq[java.io.File]) {
        // warmup
        print ("warmup")
        for (i <- 1 to warmupIterations) {
            count ((db: BytecodeDatabase) => db.basicBlocks)
            print (".")
        }
        println ("")
    }


    def profile(implicit files: Seq[File]) {
        implicit val iter = 10

        //val (databaseMemory, _) = dataMemory((db: BytecodeDatabase) => db.relations)

        val methodDeclarationCount = count ((db: BytecodeDatabase) => db.methodDeclarations)
        val basicBlockEndPcsCount = count ((db: BytecodeDatabase) => db.basicBlockEndPcs)
        val immediateBasicBlockSuccessorEdgesCount = count ((db: BytecodeDatabase) => db.immediateBasicBlockSuccessorEdges)
        val fallThroughCaseSuccessorsCount = count ((db: BytecodeDatabase) => db.fallThroughCaseSuccessors)
        val basicBlockSuccessorEdgesCount = count ((db: BytecodeDatabase) => db.basicBlockSuccessorEdges)
        val basicBlockStartPcsCount = count ((db: BytecodeDatabase) => db.basicBlockStartPcs)
        val bordersCount = count ((db: BytecodeDatabase) => db.borders)
        val startBlocksCount = count ((db: BytecodeDatabase) => db.sortedBasicBlockStartPcsByMethod)
        val endBlocksCount = count ((db: BytecodeDatabase) => db.sortedBasicBlockEndPcsByMethod)
        val basicBlocksCount = count ((db: BytecodeDatabase) => db.basicBlocks)
        val basicBlocksNewCount = count ((db: BytecodeDatabase) => db.basicBlocksNew)


        val (methodDeclarations, _) = measureDataMemory ((db: BytecodeDatabase) => db.methodDeclarations)
        val (basicBlockEndPcs, _) = measureDataMemory ((db: BytecodeDatabase) => db.basicBlockEndPcs)
        val (immediateBasicBlockSuccessorEdges, _) = measureDataMemory ((db: BytecodeDatabase) => db.immediateBasicBlockSuccessorEdges)
        val (fallThroughCaseSuccessors, _) = measureDataMemory ((db: BytecodeDatabase) => db.fallThroughCaseSuccessors)
        val (basicBlockSuccessorEdges, _) = measureDataMemory ((db: BytecodeDatabase) => db.basicBlockSuccessorEdges)
        val (basicBlockStartPcs, _) = measureDataMemory ((db: BytecodeDatabase) => db.basicBlockStartPcs)
        val (borders, _) = measureDataMemory ((db: BytecodeDatabase) => db.borders)
        val (startBorders, _)  = measureDataMemory ((db: BytecodeDatabase) => db.sortedBasicBlockStartPcsByMethod)
        val (endBorders, _)  = measureDataMemory ((db: BytecodeDatabase) => db.sortedBasicBlockEndPcsByMethod)
        val (basicBlocks, _) = measureDataMemory ((db: BytecodeDatabase) => db.basicBlocks)
        val (basicBlocksNew, _)  = measureDataMemory ((db: BytecodeDatabase) => db.basicBlocksNew)

        val (methodDeclarationsComp, _) = measureComputationMemory ((db: BytecodeDatabase) => db.methodDeclarations)
        val (basicBlockEndPcsComp, _) = measureComputationMemory ((db: BytecodeDatabase) => db.basicBlockEndPcs)
        val (immediateBasicBlockSuccessorEdgesComp, _) = measureComputationMemory ((db: BytecodeDatabase) => db.immediateBasicBlockSuccessorEdges)
        val (fallThroughCaseSuccessorsComp, _) = measureComputationMemory ((db: BytecodeDatabase) => db.fallThroughCaseSuccessors)
        val (basicBlockSuccessorEdgesComp, _) = measureComputationMemory ((db: BytecodeDatabase) => db.basicBlockSuccessorEdges)
        val (basicBlockStartPcsComp, _) = measureComputationMemory ((db: BytecodeDatabase) => db.basicBlockStartPcs)
        val (bordersComp, _) = measureComputationMemory ((db: BytecodeDatabase) => db.borders)
        val (startBordersComp, _)  = measureComputationMemory ((db: BytecodeDatabase) => db.sortedBasicBlockStartPcsByMethod)
        val (endBordersComp, _)  = measureComputationMemory ((db: BytecodeDatabase) => db.sortedBasicBlockEndPcsByMethod)
        val (basicBlocksComp, _) = measureComputationMemory ((db: BytecodeDatabase) => db.basicBlocks)
        val (basicBlocksNewComp, _)  = measureComputationMemory ((db: BytecodeDatabase) => db.basicBlocksNew)


        println ("DATA")

        println ("methodDeclarations:                " + (methodDeclarations).summary (MebiByte))
        println ("methodDeclaration:                 " + (methodDeclarations).summaryPerUnit (methodDeclarationCount)(Byte))
        println ("basicBlockEndPcs:                  " + (basicBlockEndPcs).summary (MebiByte))
        println ("basicBlockEndPc:                   " + (basicBlockEndPcs - methodDeclarations).summaryPerUnit (basicBlockEndPcsCount)(Byte))
        println ("immediateBasicBlockSuccessorEdges: " + (immediateBasicBlockSuccessorEdges).summary (MebiByte))
        println ("immediateBasicBlockSuccessorEdge:  " + (immediateBasicBlockSuccessorEdges - methodDeclarations).summaryPerUnit (immediateBasicBlockSuccessorEdgesCount)(Byte))
        println ("fallThroughCaseSuccessors:         " + (fallThroughCaseSuccessors).summary (MebiByte))
        println ("fallThroughCaseSuccessor:          " + (fallThroughCaseSuccessors - methodDeclarations).summaryPerUnit (fallThroughCaseSuccessorsCount)(Byte))
        println ("basicBlockSuccessorEdges:          " + (basicBlockSuccessorEdges).summary (MebiByte))
        println ("basicBlockSuccessorEdge:           " + (basicBlockSuccessorEdges - methodDeclarations).summaryPerUnit (basicBlockSuccessorEdgesCount)(Byte))
        println ("basicBlockStartPcs:                " + (basicBlockStartPcs).summary (MebiByte))
        println ("basicBlockStartPc:                 " + (basicBlockStartPcs - methodDeclarations).summaryPerUnit (basicBlockStartPcsCount)(Byte))
        println ("borders:                           " + (borders).summary (MebiByte))
        println ("border:                            " + (borders - methodDeclarations).summaryPerUnit (bordersCount)(Byte))
        println ("startBorder                        " + (startBorders).summary (MebiByte))
        println ("startBorder                        " + (startBorders - methodDeclarations).summaryPerUnit (startBlocksCount)(Byte))
        println ("endBorders:                        " + (endBorders).summary (MebiByte))
        println ("endBorder:                         " + (endBorders - methodDeclarations).summaryPerUnit (endBlocksCount)(Byte))
        println ("basicBlocks:                       " + (basicBlocks).summary (MebiByte))
        println ("basicBlock:                        " + (basicBlocks - methodDeclarations).summaryPerUnit (basicBlocksCount)(Byte))
        println ("basicBlocksNew:                    " + (basicBlocksNew).summary (MebiByte))
        println ("basicBlockNew:                     " + (basicBlocksNew - methodDeclarations).summaryPerUnit (basicBlocksCount)(Byte))



        println ("COMPUTATION")
        println ("methodDeclarations:                " + (methodDeclarationsComp).summary (MebiByte))
        println ("methodDeclaration:                 " + (methodDeclarationsComp).summaryPerUnit (methodDeclarationCount)(Byte))
        println ("basicBlockEndPcs:                  " + (basicBlockEndPcsComp).summary (MebiByte))
        println ("basicBlockEndPc:                   " + (basicBlockEndPcsComp - methodDeclarations).summaryPerUnit (basicBlockEndPcsCount)(Byte))
        println ("immediateBasicBlockSuccessorEdges: " + (immediateBasicBlockSuccessorEdgesComp).summary (MebiByte))
        println ("immediateBasicBlockSuccessorEdge:  " + (immediateBasicBlockSuccessorEdgesComp - methodDeclarations).summaryPerUnit (immediateBasicBlockSuccessorEdgesCount)(Byte))
        println ("fallThroughCaseSuccessors:         " + (fallThroughCaseSuccessorsComp).summary (MebiByte))
        println ("fallThroughCaseSuccessor:          " + (fallThroughCaseSuccessorsComp - methodDeclarations).summaryPerUnit (fallThroughCaseSuccessorsCount)(Byte))
        println ("basicBlockSuccessorEdges:          " + (basicBlockSuccessorEdgesComp).summary (MebiByte))
        println ("basicBlockSuccessorEdge:           " + (basicBlockSuccessorEdgesComp - methodDeclarations).summaryPerUnit (basicBlockSuccessorEdgesCount)(Byte))
        println ("basicBlockStartPcs:                " + (basicBlockStartPcsComp).summary (MebiByte))
        println ("basicBlockStartPc:                 " + (basicBlockStartPcsComp - methodDeclarations).summaryPerUnit (basicBlockStartPcsCount)(Byte))
        println ("borders:                           " + (bordersComp).summary (MebiByte))
        println ("border:                            " + (bordersComp - methodDeclarations).summaryPerUnit (bordersCount)(Byte))
        println ("startBorder                        " + (startBordersComp).summary (MebiByte))
        println ("startBorder                        " + (startBordersComp - methodDeclarations).summaryPerUnit (startBlocksCount)(Byte))
        println ("endBorders:                        " + (endBordersComp).summary (MebiByte))
        println ("endBorder:                         " + (endBordersComp - methodDeclarations).summaryPerUnit (endBlocksCount)(Byte))
        println ("basicBlocks:                       " + (basicBlocksComp).summary (MebiByte))
        println ("basicBlock:                        " + (basicBlocksComp - methodDeclarations).summaryPerUnit (basicBlocksCount)(Byte))
        println ("basicBlocksNew:                    " + (basicBlocksNewComp).summary (MebiByte))
        println ("basicBlockNew:                     " + (basicBlocksNewComp - methodDeclarations).summaryPerUnit (basicBlocksCount)(Byte))


    }


}
