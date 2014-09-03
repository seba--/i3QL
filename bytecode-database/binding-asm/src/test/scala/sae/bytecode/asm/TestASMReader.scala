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
package sae.bytecode.asm

import org.junit.Test
import org.junit.Assert._
import sae.bytecode.ASMDatabaseFactory

/**
 *
 * @author Ralf Mitschke
 */
class TestASMReader
{

    @Test
    def testReadJDK () {
        val database = ASMDatabaseFactory.create


		//TODO you cant materialize those relations because the memory size is too big
       /* val classes = database.classDeclarations.asMaterialized
        val methods = database.methodDeclarations.asMaterialized
        val fields = database.fieldDeclarations.asMaterialized
        val instructions = database.instructions.asMaterialized */

        val start = System.nanoTime ()
        val stream = this.getClass.getClassLoader.getResourceAsStream ("jdk1.7.0-win-64-rt.jar")

        database.addArchive (stream)

        val end = System.nanoTime ()

      /*  assertEquals(18663, classes.size)
        assertEquals(76399, fields.size)
        assertEquals(165512, methods.size)
        //assertEquals(4505310, instructions.size)
        assertEquals(4505268, instructions.size) // TODO check that the number of WIDE modifiers is 42  */
        println ("took: " + ((end - start).toDouble / 1000000000) + " s")
    }

}
