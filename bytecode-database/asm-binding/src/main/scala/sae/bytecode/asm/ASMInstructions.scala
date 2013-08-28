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

import sae.bytecode.BytecodeInstructions

/**
 *
 * @author Ralf Mitschke
 */
trait ASMInstructions
    extends BytecodeInstructions
{


    case class NOP (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.NOP

    case class ACONST_NULL (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.ACONST_NULL

    case class ICONST_M1 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.ICONST_M1

    case class ICONST_0 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.ICONST_0

    case class ICONST_1 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.ICONST_1

    case class ICONST_2 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.ICONST_2

    case class ICONST_3 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.ICONST_3

    case class ICONST_4 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.ICONST_4

    case class ICONST_5 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.ICONST_5

    case class LCONST_0 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.LCONST_0

    case class LCONST_1 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.LCONST_1

    case class FCONST_0 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.FCONST_0

    case class FCONST_1 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.FCONST_1

    case class FCONST_2 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.FCONST_2

    case class DCONST_0 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.DCONST_0

    case class DCONST_1 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.DCONST_1

    case class BIPUSH (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        value: Byte
    ) extends super.BIPUSH

    case class SIPUSH (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        value: Short
    ) extends super.SIPUSH

    case class LDC[V] (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        value: V
    ) extends super.LDC[V]

    case class LDC_W[V] (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        value: V
    ) extends super.LDC_W[V]

    case class LDC2_W[V] (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        value: V
    ) extends super.LDC2_W[V]

    case class ILOAD (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        localVariableIndex: Int
    ) extends super.ILOAD

    case class LLOAD (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        localVariableIndex: Int
    ) extends super.LLOAD

    case class FLOAD (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        localVariableIndex: Int
    ) extends super.FLOAD

    case class DLOAD (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        localVariableIndex: Int
    ) extends super.DLOAD

    case class ALOAD (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        localVariableIndex: Int
    ) extends super.ALOAD

    case class ILOAD_0 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.ILOAD_0

    case class ILOAD_1 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.ILOAD_1

    case class ILOAD_2 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.ILOAD_2

    case class ILOAD_3 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.ILOAD_3

    case class LLOAD_0 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.LLOAD_0

    case class LLOAD_1 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.LLOAD_1

    case class LLOAD_2 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.LLOAD_2

    case class LLOAD_3 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.LLOAD_3

    case class FLOAD_0 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.FLOAD_0

    case class FLOAD_1 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.FLOAD_1

    case class FLOAD_2 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.FLOAD_2

    case class FLOAD_3 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.FLOAD_3

    case class DLOAD_0 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.DLOAD_0

    case class DLOAD_1 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.DLOAD_1

    case class DLOAD_2 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.DLOAD_2

    case class DLOAD_3 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.DLOAD_3

    case class ALOAD_0 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.ALOAD_0

    case class ALOAD_1 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.ALOAD_1

    case class ALOAD_2 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.ALOAD_2

    case class ALOAD_3 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.ALOAD_3

    case class IALOAD (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.IALOAD

    case class LALOAD (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.LALOAD

    case class FALOAD (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.FALOAD

    case class DALOAD (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.DALOAD

    case class AALOAD (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.AALOAD

    case class BALOAD (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.BALOAD

    case class CALOAD (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.CALOAD

    case class SALOAD (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.SALOAD

    case class ISTORE (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        localVariableIndex: Int
    ) extends super.ISTORE

    case class LSTORE (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        localVariableIndex: Int
    ) extends super.LSTORE

    case class FSTORE (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        localVariableIndex: Int
    ) extends super.FSTORE

    case class DSTORE (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        localVariableIndex: Int
    ) extends super.DSTORE

    case class ASTORE (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        localVariableIndex: Int
    ) extends super.ASTORE

    case class ISTORE_0 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.ISTORE_0

    case class ISTORE_1 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.ISTORE_1

    case class ISTORE_2 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.ISTORE_2

    case class ISTORE_3 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.ISTORE_3

    case class LSTORE_0 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.LSTORE_0

    case class LSTORE_1 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.LSTORE_1

    case class LSTORE_2 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.LSTORE_2

    case class LSTORE_3 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.LSTORE_3

    case class FSTORE_0 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.FSTORE_0

    case class FSTORE_1 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.FSTORE_1

    case class FSTORE_2 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.FSTORE_2

    case class FSTORE_3 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.FSTORE_3

    case class DSTORE_0 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.DSTORE_0

    case class DSTORE_1 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.DSTORE_1

    case class DSTORE_2 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.DSTORE_2

    case class DSTORE_3 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.DSTORE_3

    case class ASTORE_0 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.ASTORE_0

    case class ASTORE_1 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.ASTORE_1

    case class ASTORE_2 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.ASTORE_2

    case class ASTORE_3 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.ASTORE_3

    case class IASTORE (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.IASTORE

    case class LASTORE (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.LASTORE

    case class FASTORE (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.FASTORE

    case class DASTORE (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.DASTORE

    case class AASTORE (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.AASTORE

    case class BASTORE (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.BASTORE

    case class CASTORE (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.CASTORE

    case class SASTORE (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.SASTORE

    case class POP (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.POP

    case class POP2 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.POP2

    case class DUP (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.DUP

    case class DUP_X1 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.DUP_X1

    case class DUP_X2 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.DUP_X2

    case class DUP2 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.DUP2

    case class DUP2_X1 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.DUP2_X1

    case class DUP2_X2 (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.DUP2_X2

    case class SWAP (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.SWAP

    case class IADD (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.IADD

    case class LADD (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.LADD

    case class FADD (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.FADD

    case class DADD (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.DADD

    case class ISUB (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.ISUB

    case class LSUB (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.LSUB

    case class FSUB (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.FSUB

    case class DSUB (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.DSUB

    case class IMUL (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.IMUL

    case class LMUL (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.LMUL

    case class FMUL (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.FMUL

    case class DMUL (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.DMUL

    case class IDIV (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.IDIV

    case class LDIV (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.LDIV

    case class FDIV (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.FDIV

    case class DDIV (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.DDIV

    case class IREM (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.IREM

    case class LREM (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.LREM

    case class FREM (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.FREM

    case class DREM (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.DREM

    case class INEG (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.INEG

    case class LNEG (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.LNEG

    case class FNEG (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.FNEG

    case class DNEG (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.DNEG

    case class ISHL (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.ISHL

    case class LSHL (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.LSHL

    case class ISHR (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.ISHR

    case class LSHR (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.LSHR

    case class IUSHR (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.IUSHR

    case class LUSHR (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.LUSHR

    case class IAND (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.IAND

    case class LAND (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.LAND

    case class IOR (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.IOR

    case class LOR (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.LOR

    case class IXOR (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.IXOR

    case class LXOR (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.LXOR

    case class IINC (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        localVariableIndex: Int,
        value: Int
    ) extends super.IINC

    case class I2L (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.I2L

    case class I2F (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.I2F

    case class I2D (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.I2D

    case class L2I (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.L2I

    case class L2F (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.L2F

    case class L2D (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.L2D

    case class F2I (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.F2I

    case class F2L (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.F2L

    case class F2D (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.F2D

    case class D2I (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.D2I

    case class D2L (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.D2L

    case class D2F (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.D2F

    case class I2B (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.I2B

    case class INT2BYTE (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.INT2BYTE

    case class I2C (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.I2C

    case class INT2CHAR (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.INT2CHAR

    case class I2S (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.I2S

    case class INT2SHORT (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.INT2SHORT

    case class LCMP (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.LCMP

    case class FCMPL (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.FCMPL

    case class FCMPG (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.FCMPG

    case class DCMPL (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.DCMPL

    case class DCMPG (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.DCMPG

    case class IFEQ (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        offset: Int
    ) extends super.IFEQ

    case class IFNE (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        offset: Int
    ) extends super.IFNE

    case class IFLT (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        offset: Int
    ) extends super.IFLT

    case class IFGE (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        offset: Int
    ) extends super.IFGE

    case class IFGT (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        offset: Int
    ) extends super.IFGT

    case class IFLE (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        offset: Int
    ) extends super.IFLE

    case class IF_ICMPEQ (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        offset: Int
    ) extends super.IF_ICMPEQ

    case class IF_ICMPNE (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        offset: Int
    ) extends super.IF_ICMPNE

    case class IF_ICMPLT (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        offset: Int
    ) extends super.IF_ICMPLT

    case class IF_ICMPGE (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        offset: Int
    ) extends super.IF_ICMPGE

    case class IF_ICMPGT (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        offset: Int
    ) extends super.IF_ICMPGT

    case class IF_ICMPLE (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        offset: Int
    ) extends super.IF_ICMPLE

    case class IF_ACMPEQ (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        offset: Int
    ) extends super.IF_ACMPEQ

    case class IF_ACMPNE (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        offset: Int
    ) extends super.IF_ACMPNE

    case class GOTO (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        offset: Int
    ) extends super.GOTO

    case class JSR (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        offset: Int
    ) extends super.JSR

    case class RET (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        localVariableIndex: Int
    ) extends super.RET

    case class TABLESWITCH (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        high: Int,
        low: Int,
        defaultOffset: Int,
        offsets: Seq[Int]
    ) extends super.TABLESWITCH

    case class LOOKUPSWITCH (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        keys: Seq[Int],
        defaultOffset: Int,
        offsets: Seq[Int]
    ) extends super.LOOKUPSWITCH

    case class IRETURN (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.IRETURN

    case class LRETURN (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.LRETURN

    case class FRETURN (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.FRETURN

    case class DRETURN (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.DRETURN

    case class ARETURN (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.ARETURN

    case class RETURN (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.RETURN

    case class GETSTATIC (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        fieldInfo: FieldInfo
    ) extends super.GETSTATIC

    case class PUTSTATIC (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        fieldInfo: FieldInfo
    ) extends super.PUTSTATIC

    case class GETFIELD (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        fieldInfo: FieldInfo
    ) extends super.GETFIELD

    case class PUTFIELD (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        fieldInfo: FieldInfo
    ) extends super.PUTFIELD

    case class INVOKEVIRTUAL (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        methodInfo: MethodInfo
    ) extends super.INVOKEVIRTUAL

    case class INVOKESPECIAL (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        methodInfo: MethodInfo
    ) extends super.INVOKESPECIAL

    case class INVOKENONVIRTUAL (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        methodInfo: MethodInfo
    ) extends super.INVOKENONVIRTUAL

    case class INVOKESTATIC (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        methodInfo: MethodInfo
    ) extends super.INVOKESTATIC

    case class INVOKEINTERFACE (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        methodInfo: MethodInfo
    ) extends super.INVOKEINTERFACE

    case class NEW (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        objectType: ObjectType
    ) extends super.NEW

    case class NEWARRAY (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        elementType: PrimitiveType,
        arrayType: ArrayType[PrimitiveType]
    ) extends super.NEWARRAY

    case class ANEWARRAY (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        elementType: ObjectType,
        arrayType: ArrayType[ObjectType]
    ) extends super.ANEWARRAY

    case class ARRAYLENGTH (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.ARRAYLENGTH

    case class ATHROW (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.ATHROW

    case class CHECKCAST (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        objectType: ObjectType
    ) extends super.CHECKCAST

    case class INSTANCEOF (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        objectType: ObjectType
    ) extends super.INSTANCEOF

    case class MONITORENTER (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.MONITORENTER

    case class MONITOREXIT (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.MONITOREXIT

    case class WIDE (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.WIDE

    case class MULTIANEWARRAY[V] (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        elementType: ArrayType[V],
        arrayType: ArrayType[ArrayType[V]],
        dimensions: Int
    ) extends super.MULTIANEWARRAY[V]

    case class IFNULL (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        offset: Int
    ) extends super.IFNULL

    case class IFNONNULL (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int,
        offset: Int
    ) extends super.IFNONNULL

    case class GOTO_W (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.GOTO_W

    case class JSR_W (
        declaringMethod: MethodDeclaration,
        programCounter: Int,
        sequenceIndex: Int
    ) extends super.JSR_W


}
