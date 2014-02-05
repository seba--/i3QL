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
import util.Byte
import sae.syntax.sql._
import sae.bytecode.structure.InheritanceRelation
import sae.analyses.profiler.measure.units.MebiByte

/**
 * This class profiles the memory used in the acyclic transitive closure used for inheritance relations
 *
 * @author Ralf Mitschke
 *
 */
object SubTypingMemoryProfiler
    extends AbstractMemoryProfiler
{
    def warmUp(implicit files: Seq[File]) {
        // warmup
        print ("warmup")
        for (i <- 1 to warmupIterations) {
            count ((db: BytecodeDatabase) => db.subTypes)
            print (".")
        }
        println ("")
    }

    def profile(implicit files: Seq[File]) {
        implicit val iter = 50
        val subTypesCount = count ((db: BytecodeDatabase) => db.subTypes)
        val edgeCount = count ((db: BytecodeDatabase) => db.inheritance)
        val nodeCount = count (nodes)


        val (subTypesData, _) = measureDataMemory ((db: BytecodeDatabase) => db.subTypes)
        val (subTypesComputation, _) = measureComputationMemory ((db: BytecodeDatabase) => db.subTypes)

        val (nodeData, _) = measureDataMemory (nodes)

        println ("subtypes -- data:               " + (subTypesData).summary (MebiByte))
        println ("subtypes -- computation + data: " + (subTypesComputation).summary (MebiByte))

        println ("nodes -- data:                  " + (nodeData).summary (MebiByte))
        println ("node  -- data/unit              " + (nodeData).summaryPerUnit (nodeCount)(Byte))


        println ("subtypes -- computation:        " + (subTypesComputation - subTypesData).summary (MebiByte))
        println ("subtypes -- computation/unit:   " + (subTypesComputation - subTypesData).summaryPerUnit(subTypesCount) (Byte))

        println ("subtypes -- computation/node:   " + (subTypesComputation - subTypesData).summaryPerUnit(nodeCount) (Byte))
    }


    /**
     * Select all class type literals that are a node in the inheritance relation
     */
    def nodes (db: BytecodeDatabase) = compile (
        SELECT DISTINCT (*) FROM (
            (SELECT ((_: InheritanceRelation).subType) FROM db.inheritance) UNION_ALL (
                SELECT ((_: InheritanceRelation).superType) FROM db.inheritance)
            )
    )
}
