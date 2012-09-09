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
        case 50 => AALOAD(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.AALOAD], bytecodeIndex, sequenceIndex)
        case 83 => AASTORE(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.AASTORE], bytecodeIndex, sequenceIndex)
        case 1 => ACONST_NULL(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ACONST_NULL], bytecodeIndex, sequenceIndex)
        case 25 => ALOAD(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ALOAD], bytecodeIndex, sequenceIndex)
        case 42 => ALOAD_0(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ALOAD_0], bytecodeIndex, sequenceIndex)
        case 43 => ALOAD_1(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ALOAD_1], bytecodeIndex, sequenceIndex)
        case 44 => ALOAD_2(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ALOAD_2], bytecodeIndex, sequenceIndex)
        case 45 => ALOAD_3(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ALOAD_3], bytecodeIndex, sequenceIndex)
        case 189 => ANEWARRAY(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ANEWARRAY], bytecodeIndex, sequenceIndex)
        case 176 => ARETURN(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ARETURN], bytecodeIndex, sequenceIndex)
        case 190 => ARRAYLENGTH(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ARRAYLENGTH], bytecodeIndex, sequenceIndex)
        case 58 => ASTORE(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ASTORE], bytecodeIndex, sequenceIndex)
        case 75 => ASTORE_0(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ASTORE_0], bytecodeIndex, sequenceIndex)
        case 76 => ASTORE_1(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ASTORE_1], bytecodeIndex, sequenceIndex)
        case 77 => ASTORE_2(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ASTORE_2], bytecodeIndex, sequenceIndex)
        case 78 => ASTORE_3(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ASTORE_3], bytecodeIndex, sequenceIndex)
        case 191 => ATHROW(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ATHROW], bytecodeIndex, sequenceIndex)
        case 51 => BALOAD(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.BALOAD], bytecodeIndex, sequenceIndex)
        case 84 => BASTORE(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.BASTORE], bytecodeIndex, sequenceIndex)
        case 16 => BIPUSH(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.BIPUSH], bytecodeIndex, sequenceIndex)
        case 52 => CALOAD(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.CALOAD], bytecodeIndex, sequenceIndex)
        case 85 => CASTORE(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.CASTORE], bytecodeIndex, sequenceIndex)
        case 192 => CHECKCAST(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.CHECKCAST], bytecodeIndex, sequenceIndex)
        case 144 => D2F(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.D2F], bytecodeIndex, sequenceIndex)
        case 142 => D2I(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.D2I], bytecodeIndex, sequenceIndex)
        case 143 => D2L(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.D2L], bytecodeIndex, sequenceIndex)
        case 99 => DADD(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DADD], bytecodeIndex, sequenceIndex)
        case 49 => DALOAD(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DALOAD], bytecodeIndex, sequenceIndex)
        case 82 => DASTORE(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DASTORE], bytecodeIndex, sequenceIndex)
        case 152 => DCMPG(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DCMPG], bytecodeIndex, sequenceIndex)
        case 151 => DCMPL(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DCMPL], bytecodeIndex, sequenceIndex)
        case 14 => DCONST_0(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DCONST_0], bytecodeIndex, sequenceIndex)
        case 15 => DCONST_1(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DCONST_1], bytecodeIndex, sequenceIndex)
        case 111 => DDIV(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DDIV], bytecodeIndex, sequenceIndex)
        case 24 => DLOAD(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DLOAD], bytecodeIndex, sequenceIndex)
        case 38 => DLOAD_0(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DLOAD_0], bytecodeIndex, sequenceIndex)
        case 39 => DLOAD_1(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DLOAD_1], bytecodeIndex, sequenceIndex)
        case 40 => DLOAD_2(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DLOAD_2], bytecodeIndex, sequenceIndex)
        case 41 => DLOAD_3(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DLOAD_3], bytecodeIndex, sequenceIndex)
        case 107 => DMUL(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DMUL], bytecodeIndex, sequenceIndex)
        case 119 => DNEG(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DNEG], bytecodeIndex, sequenceIndex)
        case 115 => DREM(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DREM], bytecodeIndex, sequenceIndex)
        case 175 => DRETURN(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DRETURN], bytecodeIndex, sequenceIndex)
        case 57 => DSTORE(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DSTORE], bytecodeIndex, sequenceIndex)
        case 71 => DSTORE_0(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DSTORE_0], bytecodeIndex, sequenceIndex)
        case 72 => DSTORE_1(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DSTORE_1], bytecodeIndex, sequenceIndex)
        case 73 => DSTORE_2(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DSTORE_2], bytecodeIndex, sequenceIndex)
        case 74 => DSTORE_3(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DSTORE_3], bytecodeIndex, sequenceIndex)
        case 103 => DSUB(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DSUB], bytecodeIndex, sequenceIndex)
        case 89 => DUP(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DUP], bytecodeIndex, sequenceIndex)
        case 90 => DUP_X1(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DUP_X1], bytecodeIndex, sequenceIndex)
        case 91 => DUP_X2(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DUP_X2], bytecodeIndex, sequenceIndex)
        case 92 => DUP2(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DUP2], bytecodeIndex, sequenceIndex)
        case 93 => DUP2_X1(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DUP2_X1], bytecodeIndex, sequenceIndex)
        case 94 => DUP2_X2(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.DUP2_X2], bytecodeIndex, sequenceIndex)
        case 141 => F2D(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.F2D], bytecodeIndex, sequenceIndex)
        case 139 => F2I(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.F2I], bytecodeIndex, sequenceIndex)
        case 140 => F2L(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.F2L], bytecodeIndex, sequenceIndex)
        case 98 => FADD(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FADD], bytecodeIndex, sequenceIndex)
        case 48 => FALOAD(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FALOAD], bytecodeIndex, sequenceIndex)
        case 81 => FASTORE(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FASTORE], bytecodeIndex, sequenceIndex)
        case 150 => FCMPG(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FCMPG], bytecodeIndex, sequenceIndex)
        case 149 => FCMPL(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FCMPL], bytecodeIndex, sequenceIndex)
        case 11 => FCONST_0(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FCONST_0], bytecodeIndex, sequenceIndex)
        case 12 => FCONST_1(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FCONST_1], bytecodeIndex, sequenceIndex)
        case 13 => FCONST_2(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FCONST_2], bytecodeIndex, sequenceIndex)
        case 110 => FDIV(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FDIV], bytecodeIndex, sequenceIndex)
        case 23 => FLOAD(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FLOAD], bytecodeIndex, sequenceIndex)
        case 34 => FLOAD_0(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FLOAD_0], bytecodeIndex, sequenceIndex)
        case 35 => FLOAD_1(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FLOAD_1], bytecodeIndex, sequenceIndex)
        case 36 => FLOAD_2(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FLOAD_2], bytecodeIndex, sequenceIndex)
        case 37 => FLOAD_3(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FLOAD_3], bytecodeIndex, sequenceIndex)
        case 106 => FMUL(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FMUL], bytecodeIndex, sequenceIndex)
        case 118 => FNEG(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FNEG], bytecodeIndex, sequenceIndex)
        case 114 => FREM(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FREM], bytecodeIndex, sequenceIndex)
        case 174 => FRETURN(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FRETURN], bytecodeIndex, sequenceIndex)
        case 56 => FSTORE(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FSTORE], bytecodeIndex, sequenceIndex)
        case 67 => FSTORE_0(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FSTORE_0], bytecodeIndex, sequenceIndex)
        case 68 => FSTORE_1(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FSTORE_1], bytecodeIndex, sequenceIndex)
        case 69 => FSTORE_2(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FSTORE_2], bytecodeIndex, sequenceIndex)
        case 70 => FSTORE_3(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FSTORE_3], bytecodeIndex, sequenceIndex)
        case 102 => FSUB(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.FSUB], bytecodeIndex, sequenceIndex)
        case 180 =>GETFIELD (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.GETFIELD, bytecodeIndex, sequenceIndex)
        case 178 => GETSTATIC(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.GETSTATIC], bytecodeIndex, sequenceIndex)
        case 167 => GOTO(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.GOTO], bytecodeIndex, sequenceIndex)
        case 200 => GOTO_W(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.GOTO_W], bytecodeIndex, sequenceIndex)
        case 145 => I2B(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.I2B], bytecodeIndex, sequenceIndex)
        case 146 => I2C(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.I2C], bytecodeIndex, sequenceIndex)
        case 135 => I2D(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.I2D], bytecodeIndex, sequenceIndex)
        case 134 => I2F(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.I2F], bytecodeIndex, sequenceIndex)
        case 133 => I2L(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.I2L], bytecodeIndex, sequenceIndex)
        case 147 => I2S(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.I2S], bytecodeIndex, sequenceIndex)
        case 96 => IADD(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IADD], bytecodeIndex, sequenceIndex)
        case 46 => IALOAD(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IALOAD], bytecodeIndex, sequenceIndex)
        case 126 => IAND(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IAND], bytecodeIndex, sequenceIndex)
        case 79 => IASTORE(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IASTORE], bytecodeIndex, sequenceIndex)
        case 2 => ICONST_M1(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ICONST_M1], bytecodeIndex, sequenceIndex)
        case 3 => ICONST_0(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ICONST_0], bytecodeIndex, sequenceIndex)
        case 4 => ICONST_1(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ICONST_1], bytecodeIndex, sequenceIndex)
        case 5 => ICONST_2(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ICONST_2], bytecodeIndex, sequenceIndex)
        case 6 => ICONST_3(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ICONST_3], bytecodeIndex, sequenceIndex)
        case 7 => ICONST_4(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ICONST_4], bytecodeIndex, sequenceIndex)
        case 8 => ICONST_5(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ICONST_5], bytecodeIndex, sequenceIndex)
        case 108 => IDIV(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IDIV], bytecodeIndex, sequenceIndex)
        case 165 => IF_ACMPEQ(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IF_ACMPEQ], bytecodeIndex, sequenceIndex)
        case 166 => IF_ACMPNE(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IF_ACMPNE], bytecodeIndex, sequenceIndex)
        case 159 => IF_ICMPEQ(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IF_ICMPEQ], bytecodeIndex, sequenceIndex)
        case 160 => IF_ICMPNE(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IF_ICMPNE], bytecodeIndex, sequenceIndex)
        case 161 => IF_ICMPLT(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IF_ICMPLT], bytecodeIndex, sequenceIndex)
        case 162 => IF_ICMPGE(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IF_ICMPGE], bytecodeIndex, sequenceIndex)
        case 163 => IF_ICMPGT(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IF_ICMPGT], bytecodeIndex, sequenceIndex)
        case 164 => IF_ICMPLE(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IF_ICMPLE], bytecodeIndex, sequenceIndex)
        case 153 => IFEQ(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IFEQ], bytecodeIndex, sequenceIndex)
        case 154 => IFNE(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IFNE], bytecodeIndex, sequenceIndex)
        case 155 => IFLT(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IFLT], bytecodeIndex, sequenceIndex)
        case 156 => IFGE(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IFGE], bytecodeIndex, sequenceIndex)
        case 157 => IFGT(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IFGT], bytecodeIndex, sequenceIndex)
        case 158 => IFLE(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IFLE], bytecodeIndex, sequenceIndex)
        case 199 => IFNONNULL(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IFNONNULL], bytecodeIndex, sequenceIndex)
        case 198 => IFNULL(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IFNULL], bytecodeIndex, sequenceIndex)
        case 132 => IINC(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IINC], bytecodeIndex, sequenceIndex)
        case 21 => ILOAD(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ILOAD], bytecodeIndex, sequenceIndex)
        case 26 => ILOAD_0(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ILOAD_0], bytecodeIndex, sequenceIndex)
        case 27 => ILOAD_1(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ILOAD_1], bytecodeIndex, sequenceIndex)
        case 28 => ILOAD_2(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ILOAD_2], bytecodeIndex, sequenceIndex)
        case 29 => ILOAD_3(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ILOAD_3], bytecodeIndex, sequenceIndex)
        case 104 => IMUL(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IMUL], bytecodeIndex, sequenceIndex)
        case 116 => INEG(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.INEG], bytecodeIndex, sequenceIndex)
        case 193 => INSTANCEOF(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.INSTANCEOF], bytecodeIndex, sequenceIndex)
        case 186 => INVOKEDYNAMIC(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.INVOKEDYNAMIC], bytecodeIndex, sequenceIndex)
        case 185 => INVOKEINTERFACE(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.INVOKEINTERFACE], bytecodeIndex, sequenceIndex)
        case 183 => INVOKESPECIAL(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.INVOKESPECIAL], bytecodeIndex, sequenceIndex)
        case 184 =>INVOKESTATIC (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.INVOKESTATIC], bytecodeIndex, sequenceIndex)
        case 182 => INVOKEVIRTUAL(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.INVOKEVIRTUAL], bytecodeIndex, sequenceIndex)
        case 128 => IOR(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IOR], bytecodeIndex, sequenceIndex)
        case 112 => IREM(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IREM], bytecodeIndex, sequenceIndex)
        case 172 => IRETURN(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IRETURN], bytecodeIndex, sequenceIndex)
        case 120 => ISHL(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ISHL], bytecodeIndex, sequenceIndex)
        case 122 => ISHR(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ISHR], bytecodeIndex, sequenceIndex)
        case 54 => ISTORE(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ISTORE], bytecodeIndex, sequenceIndex)
        case 59 => ISTORE_0(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ISTORE_0], bytecodeIndex, sequenceIndex)
        case 60 => ISTORE_1(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ISTORE_1], bytecodeIndex, sequenceIndex)
        case 61 => ISTORE_2(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ISTORE_2], bytecodeIndex, sequenceIndex)
        case 62 => ISTORE_3(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ISTORE_3], bytecodeIndex, sequenceIndex)
        case 100 => ISUB(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.ISUB], bytecodeIndex, sequenceIndex)
        case 124 => IUSHR(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IUSHR], bytecodeIndex, sequenceIndex)
        case 130 => IXOR(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.IXOR], bytecodeIndex, sequenceIndex)
        case 168 => JSR(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.JSR], bytecodeIndex, sequenceIndex)
        case 201 => JSR_W(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.JSR_W], bytecodeIndex, sequenceIndex)
        case 138 => L2D(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.L2D], bytecodeIndex, sequenceIndex)
        case 137 => L2F(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.L2F], bytecodeIndex, sequenceIndex)
        case 136 => L2I(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.L2I], bytecodeIndex, sequenceIndex)
        case 97 => LADD(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LADD], bytecodeIndex, sequenceIndex)
        case 47 => LALOAD(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LALOAD], bytecodeIndex, sequenceIndex)
        case 127 => LAND(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LAND], bytecodeIndex, sequenceIndex)
        case 80 => LASTORE(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LASTORE], bytecodeIndex, sequenceIndex)
        case 148 => LCMP(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LCMP], bytecodeIndex, sequenceIndex)
        case 9 => LCONST_0(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LCONST_0], bytecodeIndex, sequenceIndex)
        case 10 => LCONST_1(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LCONST_1], bytecodeIndex, sequenceIndex)
        case 18 => LDC(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LDC], bytecodeIndex, sequenceIndex)
        case 19 => LDC_W(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LDC_W], bytecodeIndex, sequenceIndex)
        case 20 => LDC2_W(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LDC2_W], bytecodeIndex, sequenceIndex)
        case 109 => LDIV(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LDIV], bytecodeIndex, sequenceIndex)
        case 22 => LLOAD(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LLOAD], bytecodeIndex, sequenceIndex)
        case 30 => LLOAD_0(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LLOAD_0], bytecodeIndex, sequenceIndex)
        case 31 => LLOAD_1(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LLOAD_1], bytecodeIndex, sequenceIndex)
        case 32 => LLOAD_2(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LLOAD_2], bytecodeIndex, sequenceIndex)
        case 33 => LLOAD_3(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LLOAD_3], bytecodeIndex, sequenceIndex)
        case 105 => LMUL(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LMUL], bytecodeIndex, sequenceIndex)
        case 117 => LNEG(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LNEG], bytecodeIndex, sequenceIndex)
        case 171 => LOOKUPSWITCH(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LOOKUPSWITCH], bytecodeIndex, sequenceIndex)
        case 129 => LOR(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LOR], bytecodeIndex, sequenceIndex)
        case 113 => LREM(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LREM], bytecodeIndex, sequenceIndex)
        case 173 => LRETURN(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LRETURN], bytecodeIndex, sequenceIndex)
        case 121 => LSHL(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LSHL], bytecodeIndex, sequenceIndex)
        case 123 => LSHR(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LSHR], bytecodeIndex, sequenceIndex)
        case 55 => LSTORE(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LSTORE], bytecodeIndex, sequenceIndex)
        case 63 => LSTORE_0(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LSTORE_0], bytecodeIndex, sequenceIndex)
        case 64 => LSTORE_1(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LSTORE_1], bytecodeIndex, sequenceIndex)
        case 65 => LSTORE_2(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LSTORE_2], bytecodeIndex, sequenceIndex)
        case 66 => LSTORE_3(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LSTORE_3], bytecodeIndex, sequenceIndex)
        case 101 => LSUB(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LSUB], bytecodeIndex, sequenceIndex)
        case 125 => LUSHR(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LUSHR], bytecodeIndex, sequenceIndex)
        case 131 => LXOR(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.LXOR], bytecodeIndex, sequenceIndex)
        case 194 => MONITORENTER(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.MONITORENTER], bytecodeIndex, sequenceIndex)
        case 195 => MONITOREXIT(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.MONITOREXIT], bytecodeIndex, sequenceIndex)
        case 197 => MULTIANEWARRAY(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.MULTIANEWARRAY], bytecodeIndex, sequenceIndex)
        case 187 => NEW(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.NEW], bytecodeIndex, sequenceIndex)
        case 188 => NEWARRAY(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.NEWARRAY], bytecodeIndex, sequenceIndex)
        case 0 => NOP(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.NOP], bytecodeIndex, sequenceIndex)
        case 87 => POP(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.POP], bytecodeIndex, sequenceIndex)
        case 88 => POP2(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.POP2], bytecodeIndex, sequenceIndex)
        case 181 => (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.], bytecodeIndex, sequenceIndex)
        case 179 => (declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.], bytecodeIndex, sequenceIndex)
        case 169 => RET(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.RET], bytecodeIndex, sequenceIndex)
        case 177 => RETURN(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.RETURN], bytecodeIndex, sequenceIndex)
        case 53 => SALOAD(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.SALOAD], bytecodeIndex, sequenceIndex)
        case 86 => SASTORE(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.SASTORE], bytecodeIndex, sequenceIndex)
        case 17 => SIPUSH(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.SIPUSH], bytecodeIndex, sequenceIndex)
        case 95 => SWAP(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.SWAP], bytecodeIndex, sequenceIndex)
        case 170 => TABLESWITCH(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.TABLESWITCH], bytecodeIndex, sequenceIndex)
        case 196 => WIDE(declaringMethod, instruction.asInstanceOf[de.tud.cs.st.bat.resolved.WIDE], bytecodeIndex, sequenceIndex)
    }
}
