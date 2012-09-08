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
package sae.bytecode.structure

import sae.bytecode.instructions._

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 25.08.12
 * Time: 12:58
 */

trait InstructionInfo
{
    def declaringMethod: MethodDeclaration

    def instruction: de.tud.cs.st.bat.resolved.Instruction

    def bytecodeIndex: Int

    def sequenceIndex: Int
}


object InstructionInfo
{
    def apply(declaringMethod: MethodDeclaration, instruction: de.tud.cs.st.bat.resolved.Instruction, bytecodeIndex: Int, sequenceIndex: Int) = {
        instruction.opcode match {

            case 25 => ALOAD (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ALOAD], bytecodeIndex, sequenceIndex)
            case 49 => DALOAD (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DALOAD.type], bytecodeIndex, sequenceIndex)
            case 72 => DSTORE_1(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DALOAD.type], bytecodeIndex, sequenceIndex)
            case 74 => DSTORE_3(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DALOAD.type], bytecodeIndex, sequenceIndex)
            case 75 => ASTORE_0 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ASTORE_0.type], bytecodeIndex, sequenceIndex)
            case 77 => ASTORE_2 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ASTORE_2.type], bytecodeIndex, sequenceIndex)
            case 83 => AASTORE (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.AASTORE.type], bytecodeIndex, sequenceIndex)
            case 89 => DUP(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.AASTORE.type], bytecodeIndex, sequenceIndex)
            case 111 => DDIV (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DDIV.type], bytecodeIndex, sequenceIndex)
            case 115 => DREM (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DREM.type], bytecodeIndex, sequenceIndex)
            case 142 => D2I (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.D2I.type], bytecodeIndex, sequenceIndex)
            case 143 => D2L (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.D2L.type], bytecodeIndex, sequenceIndex)
            case 151 => DCMPL (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DCMPL.type], bytecodeIndex, sequenceIndex)
            case 189 => ANEWARRAY (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ANEWARRAY], bytecodeIndex, sequenceIndex)
            case 190 => ARRAYLENGTH (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ARRAYLENGTH.type], bytecodeIndex, sequenceIndex)
        }
    }
}
