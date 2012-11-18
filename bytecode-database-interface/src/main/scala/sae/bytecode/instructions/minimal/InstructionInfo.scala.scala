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
package sae.bytecode.instructions.minimal

import sae.bytecode.structure.minimal.MethodDeclaration


trait InstructionInfo
{
    def declaringMethod: MethodDeclaration

    def instruction: de.tud.cs.st.bat.resolved.Instruction

    def pc: Int

    def sequenceIndex: Int
}


object InstructionInfo
{
    def apply(declaringMethod: MethodDeclaration, instruction: de.tud.cs.st.bat.resolved.Instruction, bytecodeIndex: Int, sequenceIndex: Int): InstructionInfo =
        instruction.opcode match {
            case 50 => AALOAD (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.AALOAD.type], bytecodeIndex, sequenceIndex)
            case 83 => AASTORE (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.AASTORE.type], bytecodeIndex, sequenceIndex)
            case 1 => ACONST_NULL (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ACONST_NULL.type], bytecodeIndex, sequenceIndex)
            case 25 => ALOAD (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ALOAD], bytecodeIndex, sequenceIndex)
            case 42 => ALOAD_0 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ALOAD_0.type], bytecodeIndex, sequenceIndex)
            case 43 => ALOAD_1 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ALOAD_1.type], bytecodeIndex, sequenceIndex)
            case 44 => ALOAD_2 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ALOAD_2.type], bytecodeIndex, sequenceIndex)
            case 45 => ALOAD_3 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ALOAD_3.type], bytecodeIndex, sequenceIndex)
            case 189 => ANEWARRAY (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ANEWARRAY], bytecodeIndex, sequenceIndex)
            case 176 => ARETURN (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ARETURN.type], bytecodeIndex, sequenceIndex)
            case 190 => ARRAYLENGTH (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ARRAYLENGTH.type], bytecodeIndex, sequenceIndex)
            case 58 => ASTORE (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ASTORE], bytecodeIndex, sequenceIndex)
            case 75 => ASTORE_0 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ASTORE_0.type], bytecodeIndex, sequenceIndex)
            case 76 => ASTORE_1 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ASTORE_1.type], bytecodeIndex, sequenceIndex)
            case 77 => ASTORE_2 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ASTORE_2.type], bytecodeIndex, sequenceIndex)
            case 78 => ASTORE_3 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ASTORE_3.type], bytecodeIndex, sequenceIndex)
            case 191 => ATHROW (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ATHROW.type], bytecodeIndex, sequenceIndex)
            case 51 => BALOAD (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.BALOAD.type], bytecodeIndex, sequenceIndex)
            case 84 => BASTORE (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.BASTORE.type], bytecodeIndex, sequenceIndex)
            case 16 => BIPUSH (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.BIPUSH], bytecodeIndex, sequenceIndex)
            case 52 => CALOAD (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.CALOAD.type], bytecodeIndex, sequenceIndex)
            case 85 => CASTORE (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.CASTORE.type], bytecodeIndex, sequenceIndex)
            case 192 => CHECKCAST (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.CHECKCAST], bytecodeIndex, sequenceIndex)
            case 144 => D2F (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.D2F.type], bytecodeIndex, sequenceIndex)
            case 142 => D2I (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.D2I.type], bytecodeIndex, sequenceIndex)
            case 143 => D2L (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.D2L.type], bytecodeIndex, sequenceIndex)
            case 99 => DADD (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DADD.type], bytecodeIndex, sequenceIndex)
            case 49 => DALOAD (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DALOAD.type], bytecodeIndex, sequenceIndex)
            case 82 => DASTORE (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DASTORE.type], bytecodeIndex, sequenceIndex)
            case 152 => DCMPG (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DCMPG.type], bytecodeIndex, sequenceIndex)
            case 151 => DCMPL (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DCMPL.type], bytecodeIndex, sequenceIndex)
            case 14 => DCONST_0 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DCONST_0.type], bytecodeIndex, sequenceIndex)
            case 15 => DCONST_1 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DCONST_1.type], bytecodeIndex, sequenceIndex)
            case 111 => DDIV (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DDIV.type], bytecodeIndex, sequenceIndex)
            case 24 => DLOAD (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DLOAD], bytecodeIndex, sequenceIndex)
            case 38 => DLOAD_0 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DLOAD_0.type], bytecodeIndex, sequenceIndex)
            case 39 => DLOAD_1 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DLOAD_1.type], bytecodeIndex, sequenceIndex)
            case 40 => DLOAD_2 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DLOAD_2.type], bytecodeIndex, sequenceIndex)
            case 41 => DLOAD_3 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DLOAD_3.type], bytecodeIndex, sequenceIndex)
            case 107 => DMUL (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DMUL.type], bytecodeIndex, sequenceIndex)
            case 119 => DNEG (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DNEG.type], bytecodeIndex, sequenceIndex)
            case 115 => DREM (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DREM.type], bytecodeIndex, sequenceIndex)
            case 175 => DRETURN (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DRETURN.type], bytecodeIndex, sequenceIndex)
            case 57 => DSTORE (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DSTORE], bytecodeIndex, sequenceIndex)
            case 71 => DSTORE_0 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DSTORE_0.type], bytecodeIndex, sequenceIndex)
            case 72 => DSTORE_1 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DSTORE_1.type], bytecodeIndex, sequenceIndex)
            case 73 => DSTORE_2 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DSTORE_2.type], bytecodeIndex, sequenceIndex)
            case 74 => DSTORE_3 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DSTORE_3.type], bytecodeIndex, sequenceIndex)
            case 103 => DSUB (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DSUB.type], bytecodeIndex, sequenceIndex)
            case 89 => DUP (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DUP.type], bytecodeIndex, sequenceIndex)
            case 90 => DUP_X1 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DUP_X1.type], bytecodeIndex, sequenceIndex)
            case 91 => DUP_X2 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DUP_X2.type], bytecodeIndex, sequenceIndex)
            case 92 => DUP2 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DUP2.type], bytecodeIndex, sequenceIndex)
            case 93 => DUP2_X1 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DUP2_X1.type], bytecodeIndex, sequenceIndex)
            case 94 => DUP2_X2 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DUP2_X2.type], bytecodeIndex, sequenceIndex)
            case 141 => F2D (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.F2D.type], bytecodeIndex, sequenceIndex)
            case 139 => F2I (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.F2I.type], bytecodeIndex, sequenceIndex)
            case 140 => F2L (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.F2L.type], bytecodeIndex, sequenceIndex)
            case 98 => FADD (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FADD.type], bytecodeIndex, sequenceIndex)
            case 48 => FALOAD (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FALOAD.type], bytecodeIndex, sequenceIndex)
            case 81 => FASTORE (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FASTORE.type], bytecodeIndex, sequenceIndex)
            case 150 => FCMPG (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FCMPG.type], bytecodeIndex, sequenceIndex)
            case 149 => FCMPL (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FCMPL.type], bytecodeIndex, sequenceIndex)
            case 11 => FCONST_0 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FCONST_0.type], bytecodeIndex, sequenceIndex)
            case 12 => FCONST_1 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FCONST_1.type], bytecodeIndex, sequenceIndex)
            case 13 => FCONST_2 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FCONST_2.type], bytecodeIndex, sequenceIndex)
            case 110 => FDIV (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FDIV.type], bytecodeIndex, sequenceIndex)
            case 23 => FLOAD (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FLOAD], bytecodeIndex, sequenceIndex)
            case 34 => FLOAD_0 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FLOAD_0.type], bytecodeIndex, sequenceIndex)
            case 35 => FLOAD_1 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FLOAD_1.type], bytecodeIndex, sequenceIndex)
            case 36 => FLOAD_2 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FLOAD_2.type], bytecodeIndex, sequenceIndex)
            case 37 => FLOAD_3 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FLOAD_3.type], bytecodeIndex, sequenceIndex)
            case 106 => FMUL (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FMUL.type], bytecodeIndex, sequenceIndex)
            case 118 => FNEG (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FNEG.type], bytecodeIndex, sequenceIndex)
            case 114 => FREM (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FREM.type], bytecodeIndex, sequenceIndex)
            case 174 => FRETURN (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FRETURN.type], bytecodeIndex, sequenceIndex)
            case 56 => FSTORE (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FSTORE], bytecodeIndex, sequenceIndex)
            case 67 => FSTORE_0 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FSTORE_0.type], bytecodeIndex, sequenceIndex)
            case 68 => FSTORE_1 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FSTORE_1.type], bytecodeIndex, sequenceIndex)
            case 69 => FSTORE_2 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FSTORE_2.type], bytecodeIndex, sequenceIndex)
            case 70 => FSTORE_3 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FSTORE_3.type], bytecodeIndex, sequenceIndex)
            case 102 => FSUB (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FSUB.type], bytecodeIndex, sequenceIndex)
            case 180 => GETFIELD (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.GETFIELD], bytecodeIndex, sequenceIndex)
            case 178 => GETSTATIC (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.GETSTATIC], bytecodeIndex, sequenceIndex)
            case 167 => GOTO (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.GOTO], bytecodeIndex, sequenceIndex)
            case 200 => GOTO_W (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.GOTO_W], bytecodeIndex, sequenceIndex)
            case 145 => I2B (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.I2B.type], bytecodeIndex, sequenceIndex)
            case 146 => I2C (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.I2C.type], bytecodeIndex, sequenceIndex)
            case 135 => I2D (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.I2D.type], bytecodeIndex, sequenceIndex)
            case 134 => I2F (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.I2F.type], bytecodeIndex, sequenceIndex)
            case 133 => I2L (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.I2L.type], bytecodeIndex, sequenceIndex)
            case 147 => I2S (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.I2S.type], bytecodeIndex, sequenceIndex)
            case 96 => IADD (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IADD.type], bytecodeIndex, sequenceIndex)
            case 46 => IALOAD (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IALOAD.type], bytecodeIndex, sequenceIndex)
            case 126 => IAND (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IAND.type], bytecodeIndex, sequenceIndex)
            case 79 => IASTORE (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IASTORE.type], bytecodeIndex, sequenceIndex)
            case 2 => ICONST_M1 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ICONST_M1.type], bytecodeIndex, sequenceIndex)
            case 3 => ICONST_0 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ICONST_0.type], bytecodeIndex, sequenceIndex)
            case 4 => ICONST_1 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ICONST_1.type], bytecodeIndex, sequenceIndex)
            case 5 => ICONST_2 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ICONST_2.type], bytecodeIndex, sequenceIndex)
            case 6 => ICONST_3 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ICONST_3.type], bytecodeIndex, sequenceIndex)
            case 7 => ICONST_4 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ICONST_4.type], bytecodeIndex, sequenceIndex)
            case 8 => ICONST_5 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ICONST_5.type], bytecodeIndex, sequenceIndex)
            case 108 => IDIV (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IDIV.type], bytecodeIndex, sequenceIndex)
            case 165 => IF_ACMPEQ (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IF_ACMPEQ], bytecodeIndex, sequenceIndex)
            case 166 => IF_ACMPNE (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IF_ACMPNE], bytecodeIndex, sequenceIndex)
            case 159 => IF_ICMPEQ (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IF_ICMPEQ], bytecodeIndex, sequenceIndex)
            case 160 => IF_ICMPNE (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IF_ICMPNE], bytecodeIndex, sequenceIndex)
            case 161 => IF_ICMPLT (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IF_ICMPLT], bytecodeIndex, sequenceIndex)
            case 162 => IF_ICMPGE (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IF_ICMPGE], bytecodeIndex, sequenceIndex)
            case 163 => IF_ICMPGT (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IF_ICMPGT], bytecodeIndex, sequenceIndex)
            case 164 => IF_ICMPLE (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IF_ICMPLE], bytecodeIndex, sequenceIndex)
            case 153 => IFEQ (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IFEQ], bytecodeIndex, sequenceIndex)
            case 154 => IFNE (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IFNE], bytecodeIndex, sequenceIndex)
            case 155 => IFLT (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IFLT], bytecodeIndex, sequenceIndex)
            case 156 => IFGE (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IFGE], bytecodeIndex, sequenceIndex)
            case 157 => IFGT (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IFGT], bytecodeIndex, sequenceIndex)
            case 158 => IFLE (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IFLE], bytecodeIndex, sequenceIndex)
            case 199 => IFNONNULL (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IFNONNULL], bytecodeIndex, sequenceIndex)
            case 198 => IFNULL (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IFNULL], bytecodeIndex, sequenceIndex)
            case 132 => IINC (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IINC], bytecodeIndex, sequenceIndex)
            case 21 => ILOAD (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ILOAD], bytecodeIndex, sequenceIndex)
            case 26 => ILOAD_0 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ILOAD_0.type], bytecodeIndex, sequenceIndex)
            case 27 => ILOAD_1 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ILOAD_1.type], bytecodeIndex, sequenceIndex)
            case 28 => ILOAD_2 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ILOAD_2.type], bytecodeIndex, sequenceIndex)
            case 29 => ILOAD_3 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ILOAD_3.type], bytecodeIndex, sequenceIndex)
            case 104 => IMUL (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IMUL.type], bytecodeIndex, sequenceIndex)
            case 116 => INEG (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.INEG.type], bytecodeIndex, sequenceIndex)
            case 193 => INSTANCEOF (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.INSTANCEOF], bytecodeIndex, sequenceIndex)
            case 186 => INVOKEDYNAMIC (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.INVOKEDYNAMIC], bytecodeIndex, sequenceIndex)
            case 185 => INVOKEINTERFACE (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.INVOKEINTERFACE], bytecodeIndex, sequenceIndex)
            case 183 => INVOKESPECIAL (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.INVOKESPECIAL], bytecodeIndex, sequenceIndex)
            case 184 => INVOKESTATIC (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.INVOKESTATIC], bytecodeIndex, sequenceIndex)
            case 182 => INVOKEVIRTUAL (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.INVOKEVIRTUAL], bytecodeIndex, sequenceIndex)
            case 128 => IOR (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IOR.type], bytecodeIndex, sequenceIndex)
            case 112 => IREM (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IREM.type], bytecodeIndex, sequenceIndex)
            case 172 => IRETURN (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IRETURN.type], bytecodeIndex, sequenceIndex)
            case 120 => ISHL (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ISHL.type], bytecodeIndex, sequenceIndex)
            case 122 => ISHR (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ISHR.type], bytecodeIndex, sequenceIndex)
            case 54 => ISTORE (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ISTORE], bytecodeIndex, sequenceIndex)
            case 59 => ISTORE_0 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ISTORE_0.type], bytecodeIndex, sequenceIndex)
            case 60 => ISTORE_1 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ISTORE_1.type], bytecodeIndex, sequenceIndex)
            case 61 => ISTORE_2 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ISTORE_2.type], bytecodeIndex, sequenceIndex)
            case 62 => ISTORE_3 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ISTORE_3.type], bytecodeIndex, sequenceIndex)
            case 100 => ISUB (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ISUB.type], bytecodeIndex, sequenceIndex)
            case 124 => IUSHR (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IUSHR.type], bytecodeIndex, sequenceIndex)
            case 130 => IXOR (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IXOR.type], bytecodeIndex, sequenceIndex)
            case 168 => JSR (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.JSR], bytecodeIndex, sequenceIndex)
            case 201 => JSR_W (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.JSR_W], bytecodeIndex, sequenceIndex)
            case 138 => L2D (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.L2D.type], bytecodeIndex, sequenceIndex)
            case 137 => L2F (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.L2F.type], bytecodeIndex, sequenceIndex)
            case 136 => L2I (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.L2I.type], bytecodeIndex, sequenceIndex)
            case 97 => LADD (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LADD.type], bytecodeIndex, sequenceIndex)
            case 47 => LALOAD (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LALOAD.type], bytecodeIndex, sequenceIndex)
            case 127 => LAND (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LAND.type], bytecodeIndex, sequenceIndex)
            case 80 => LASTORE (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LASTORE.type], bytecodeIndex, sequenceIndex)
            case 148 => LCMP (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LCMP.type], bytecodeIndex, sequenceIndex)
            case 9 => LCONST_0 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LCONST_0.type], bytecodeIndex, sequenceIndex)
            case 10 => LCONST_1 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LCONST_1.type], bytecodeIndex, sequenceIndex)
            case 18 => LDC (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LDC], bytecodeIndex, sequenceIndex)
            case 19 => LDC_W (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LDC_W], bytecodeIndex, sequenceIndex)
            case 20 => LDC2_W (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LDC2_W], bytecodeIndex, sequenceIndex)
            case 109 => LDIV (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LDIV.type], bytecodeIndex, sequenceIndex)
            case 22 => LLOAD (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LLOAD], bytecodeIndex, sequenceIndex)
            case 30 => LLOAD_0 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LLOAD_0.type], bytecodeIndex, sequenceIndex)
            case 31 => LLOAD_1 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LLOAD_1.type], bytecodeIndex, sequenceIndex)
            case 32 => LLOAD_2 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LLOAD_2.type], bytecodeIndex, sequenceIndex)
            case 33 => LLOAD_3 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LLOAD_3.type], bytecodeIndex, sequenceIndex)
            case 105 => LMUL (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LMUL.type], bytecodeIndex, sequenceIndex)
            case 117 => LNEG (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LNEG.type], bytecodeIndex, sequenceIndex)
            case 171 => LOOKUPSWITCH (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LOOKUPSWITCH], bytecodeIndex, sequenceIndex)
            case 129 => LOR (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LOR.type], bytecodeIndex, sequenceIndex)
            case 113 => LREM (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LREM.type], bytecodeIndex, sequenceIndex)
            case 173 => LRETURN (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LRETURN.type], bytecodeIndex, sequenceIndex)
            case 121 => LSHL (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LSHL.type], bytecodeIndex, sequenceIndex)
            case 123 => LSHR (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LSHR.type], bytecodeIndex, sequenceIndex)
            case 55 => LSTORE (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LSTORE], bytecodeIndex, sequenceIndex)
            case 63 => LSTORE_0 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LSTORE_0.type], bytecodeIndex, sequenceIndex)
            case 64 => LSTORE_1 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LSTORE_1.type], bytecodeIndex, sequenceIndex)
            case 65 => LSTORE_2 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LSTORE_2.type], bytecodeIndex, sequenceIndex)
            case 66 => LSTORE_3 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LSTORE_3.type], bytecodeIndex, sequenceIndex)
            case 101 => LSUB (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LSUB.type], bytecodeIndex, sequenceIndex)
            case 125 => LUSHR (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LUSHR.type], bytecodeIndex, sequenceIndex)
            case 131 => LXOR (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LXOR.type], bytecodeIndex, sequenceIndex)
            case 194 => MONITORENTER (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.MONITORENTER.type], bytecodeIndex, sequenceIndex)
            case 195 => MONITOREXIT (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.MONITOREXIT.type], bytecodeIndex, sequenceIndex)
            case 197 => MULTIANEWARRAY (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.MULTIANEWARRAY], bytecodeIndex, sequenceIndex)
            case 187 => NEW (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.NEW], bytecodeIndex, sequenceIndex)
            case 188 => NEWARRAY (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.NEWARRAY], bytecodeIndex, sequenceIndex)
            case 0 => NOP (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.NOP.type], bytecodeIndex, sequenceIndex)
            case 87 => POP (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.POP.type], bytecodeIndex, sequenceIndex)
            case 88 => POP2 (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.POP2.type], bytecodeIndex, sequenceIndex)
            case 181 => PUTFIELD (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.PUTFIELD], bytecodeIndex, sequenceIndex)
            case 179 => PUTSTATIC (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.PUTSTATIC], bytecodeIndex, sequenceIndex)
            case 169 => RET (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.RET], bytecodeIndex, sequenceIndex)
            case 177 => RETURN (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.RETURN.type], bytecodeIndex, sequenceIndex)
            case 53 => SALOAD (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.SALOAD.type], bytecodeIndex, sequenceIndex)
            case 86 => SASTORE (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.SASTORE.type], bytecodeIndex, sequenceIndex)
            case 17 => SIPUSH (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.SIPUSH], bytecodeIndex, sequenceIndex)
            case 95 => SWAP (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.SWAP.type], bytecodeIndex, sequenceIndex)
            case 170 => TABLESWITCH (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.TABLESWITCH], bytecodeIndex, sequenceIndex)
            case 196 => WIDE (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.WIDE.type], bytecodeIndex, sequenceIndex)
        }
}
