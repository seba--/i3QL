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
package sae.bytecode


/**
 *
 * @author Ralf Mitschke
 */
trait BytecodeInstructions
    extends BytecodeStructure
{

    protected def inlineWideInstructions: Boolean

    trait BasicInstruction extends Instruction
    {
        def nextProgramCounter = programCounter + 1
    }

    trait LocalVariableInstruction extends Instruction
    {
        def localVariableIndex: Int

        def isShortVariable: Boolean = localVariableIndex < 256
    }

    trait ImplicitLocalVariableInstruction extends LocalVariableInstruction
    {
        override def nextProgramCounter = programCounter + 1
    }

    trait ExplicitLocalVariableInstruction extends LocalVariableInstruction
    {
        override def nextProgramCounter = programCounter + (
            if (isShortVariable) {
                if (inlineWideInstructions)
                    3
                else
                    2
            }
            else
            {
                if (inlineWideInstructions)
                    4
                else
                    3
            }
            )
    }

    trait ConstantValueInstruction[V] extends Instruction
    {
        def value: V
    }

    trait FieldInstruction extends Instruction
    {
        override def nextProgramCounter = programCounter + 3

        def fieldInfo: FieldInfo
    }

    trait MethodInstruction extends Instruction
    {
        def methodInfo: MethodInfo
    }

    trait ObjectTypeInstruction extends Instruction
    {
        override def nextProgramCounter = programCounter + 3

        def objectType: ObjectType
    }

    trait JumpInstruction extends Instruction
    {
        def offset: Int
    }

    trait ConditionalJumpInstruction extends JumpInstruction
    {
        override def nextProgramCounter = programCounter + 3
    }

    trait UnconditionalJumpInstruction extends JumpInstruction
    {
        override def nextProgramCounter = programCounter + 5
    }

    trait NewArrayInstruction[V] extends Instruction
    {
        def elementType: V

        def arrayType: ArrayType[V]
    }

    trait NOP extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.NOP

    }

    trait ACONST_NULL extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.ACONST_NULL
    }

    trait ICONST_M1 extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.ICONST_M1
    }

    trait ICONST_0 extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.ICONST_0
    }

    trait ICONST_1 extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.ICONST_1
    }

    trait ICONST_2 extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.ICONST_2
    }

    trait ICONST_3 extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.ICONST_3
    }

    trait ICONST_4 extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.ICONST_4
    }

    trait ICONST_5 extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.ICONST_5
    }

    trait LCONST_0 extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.LCONST_0
    }

    trait LCONST_1 extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.LCONST_1
    }

    trait FCONST_0 extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.FCONST_0
    }

    trait FCONST_1 extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.FCONST_1
    }

    trait FCONST_2 extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.FCONST_2
    }

    trait DCONST_0 extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.DCONST_0
    }

    trait DCONST_1 extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.DCONST_1
    }

    trait BIPUSH extends ConstantValueInstruction[Byte]
    {
        override def opcode = BytecodeOpCodes.BIPUSH

        override def nextProgramCounter = programCounter + 2
    }

    trait SIPUSH extends ConstantValueInstruction[Short]
    {
        override def opcode = BytecodeOpCodes.SIPUSH

        override def nextProgramCounter = programCounter + 3
    }

    trait LDC[V] extends ConstantValueInstruction[V]
    {
        override def opcode = BytecodeOpCodes.LDC

        override def nextProgramCounter = programCounter + 2
    }

    trait LDC_W[V] extends ConstantValueInstruction[V]
    {
        override def opcode = BytecodeOpCodes.LDC_W

        override def nextProgramCounter = programCounter + 3
    }

    trait LDC2_W[V] extends ConstantValueInstruction[V]
    {
        override def opcode = BytecodeOpCodes.LDC2_W

        override def nextProgramCounter = programCounter + 3
    }

    trait ILOAD extends ExplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.ILOAD
    }

    trait LLOAD extends ExplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.LLOAD
    }

    trait FLOAD extends ExplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.FLOAD
    }

    trait DLOAD extends ExplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.DLOAD
    }

    trait ALOAD extends ExplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.ALOAD
    }

    trait ILOAD_0 extends ImplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.ILOAD_0

        override def localVariableIndex = 0
    }

    trait ILOAD_1 extends ImplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.ILOAD_1

        override def localVariableIndex = 1
    }

    trait ILOAD_2 extends ImplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.ILOAD_2

        override def localVariableIndex = 2
    }

    trait ILOAD_3 extends ImplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.ILOAD_3

        override def localVariableIndex = 3
    }

    trait LLOAD_0 extends ImplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.LLOAD_0

        override def localVariableIndex = 0
    }

    trait LLOAD_1 extends ImplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.LLOAD_1

        override def localVariableIndex = 1
    }

    trait LLOAD_2 extends ImplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.LLOAD_2

        override def localVariableIndex = 2
    }

    trait LLOAD_3 extends ImplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.LLOAD_3

        override def localVariableIndex = 3
    }

    trait FLOAD_0 extends ImplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.FLOAD_0

        override def localVariableIndex = 0
    }

    trait FLOAD_1 extends ImplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.FLOAD_1

        override def localVariableIndex = 1
    }

    trait FLOAD_2 extends ImplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.FLOAD_2

        override def localVariableIndex = 2
    }

    trait FLOAD_3 extends ImplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.FLOAD_3

        override def localVariableIndex = 3
    }

    trait DLOAD_0 extends ImplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.DLOAD_0

        override def localVariableIndex = 0
    }

    trait DLOAD_1 extends ImplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.DLOAD_1

        override def localVariableIndex = 1
    }

    trait DLOAD_2 extends ImplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.DLOAD_2

        override def localVariableIndex = 2
    }

    trait DLOAD_3 extends ImplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.DLOAD_3

        override def localVariableIndex = 3
    }

    trait ALOAD_0 extends ImplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.ALOAD_0

        override def localVariableIndex = 0
    }

    trait ALOAD_1 extends ImplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.ALOAD_1

        override def localVariableIndex = 1
    }

    trait ALOAD_2 extends ImplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.ALOAD_2

        override def localVariableIndex = 2
    }

    trait ALOAD_3 extends ImplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.ALOAD_3

        override def localVariableIndex = 3
    }

    trait IALOAD extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.IALOAD
    }

    trait LALOAD extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.LALOAD
    }

    trait FALOAD extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.FALOAD
    }

    trait DALOAD extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.DALOAD
    }

    trait AALOAD extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.AALOAD
    }

    trait BALOAD extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.BALOAD
    }

    trait CALOAD extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.CALOAD
    }

    trait SALOAD extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.SALOAD
    }

    trait ISTORE extends ExplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.ISTORE
    }

    trait LSTORE extends ExplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.LSTORE
    }

    trait FSTORE extends ExplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.FSTORE
    }

    trait DSTORE extends ExplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.DSTORE
    }

    trait ASTORE extends ExplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.ASTORE
    }

    trait ISTORE_0 extends ImplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.ISTORE_0

        override def localVariableIndex = 0
    }

    trait ISTORE_1 extends ImplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.ISTORE_1

        override def localVariableIndex = 1
    }

    trait ISTORE_2 extends ImplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.ISTORE_2

        override def localVariableIndex = 2
    }

    trait ISTORE_3 extends ImplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.ISTORE_3

        override def localVariableIndex = 3
    }

    trait LSTORE_0 extends ImplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.LSTORE_0

        override def localVariableIndex = 0
    }

    trait LSTORE_1 extends ImplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.LSTORE_1

        override def localVariableIndex = 1
    }

    trait LSTORE_2 extends ImplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.LSTORE_2

        override def localVariableIndex = 2
    }

    trait LSTORE_3 extends ImplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.LSTORE_3

        override def localVariableIndex = 3
    }

    trait FSTORE_0 extends ImplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.FSTORE_0

        override def localVariableIndex = 0
    }

    trait FSTORE_1 extends ImplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.FSTORE_1

        override def localVariableIndex = 1
    }

    trait FSTORE_2 extends ImplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.FSTORE_2

        override def localVariableIndex = 2
    }

    trait FSTORE_3 extends ImplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.FSTORE_3

        override def localVariableIndex = 3
    }

    trait DSTORE_0 extends ImplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.DSTORE_0

        override def localVariableIndex = 0
    }

    trait DSTORE_1 extends ImplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.DSTORE_1

        override def localVariableIndex = 1
    }

    trait DSTORE_2 extends ImplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.DSTORE_2

        override def localVariableIndex = 2
    }

    trait DSTORE_3 extends ImplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.DSTORE_3

        override def localVariableIndex = 3
    }

    trait ASTORE_0 extends ImplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.ASTORE_0

        override def localVariableIndex = 0
    }

    trait ASTORE_1 extends ImplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.ASTORE_1

        override def localVariableIndex = 1
    }

    trait ASTORE_2 extends ImplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.ASTORE_2

        override def localVariableIndex = 2
    }

    trait ASTORE_3 extends ImplicitLocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.ASTORE_3

        override def localVariableIndex = 3
    }

    trait IASTORE extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.IASTORE
    }

    trait LASTORE extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.LASTORE
    }

    trait FASTORE extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.FASTORE
    }

    trait DASTORE extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.DASTORE
    }

    trait AASTORE extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.AASTORE
    }

    trait BASTORE extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.BASTORE
    }

    trait CASTORE extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.CASTORE
    }

    trait SASTORE extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.SASTORE
    }

    trait POP extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.POP
    }

    trait POP2 extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.POP2
    }

    trait DUP extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.DUP
    }

    trait DUP_X1 extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.DUP_X1
    }

    trait DUP_X2 extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.DUP_X2
    }

    trait DUP2 extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.DUP2
    }

    trait DUP2_X1 extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.DUP2_X1
    }

    trait DUP2_X2 extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.DUP2_X2
    }

    trait SWAP extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.SWAP
    }

    trait IADD extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.IADD
    }

    trait LADD extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.LADD
    }

    trait FADD extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.FADD
    }

    trait DADD extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.DADD
    }

    trait ISUB extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.ISUB
    }

    trait LSUB extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.LSUB
    }

    trait FSUB extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.FSUB
    }

    trait DSUB extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.DSUB
    }

    trait IMUL extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.IMUL
    }

    trait LMUL extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.LMUL
    }

    trait FMUL extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.FMUL
    }

    trait DMUL extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.DMUL
    }

    trait IDIV extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.IDIV
    }

    trait LDIV extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.LDIV
    }

    trait FDIV extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.FDIV
    }

    trait DDIV extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.DDIV
    }

    trait IREM extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.IREM
    }

    trait LREM extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.LREM
    }

    trait FREM extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.FREM
    }

    trait DREM extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.DREM
    }

    trait INEG extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.INEG
    }

    trait LNEG extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.LNEG
    }

    trait FNEG extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.FNEG
    }

    trait DNEG extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.DNEG
    }

    trait ISHL extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.ISHL
    }

    trait LSHL extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.LSHL
    }

    trait ISHR extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.ISHR
    }

    trait LSHR extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.LSHR
    }

    trait IUSHR extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.IUSHR
    }

    trait LUSHR extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.LUSHR
    }

    trait IAND extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.IAND
    }

    trait LAND extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.LAND
    }

    trait IOR extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.IOR
    }

    trait LOR extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.LOR
    }

    trait IXOR extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.IXOR
    }

    trait LXOR extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.LXOR
    }

    trait IINC extends LocalVariableInstruction with ConstantValueInstruction[Int]
    {
        override def opcode = BytecodeOpCodes.IINC

        override def nextProgramCounter = programCounter + (
            if (isShortVariable) {
                if (inlineWideInstructions)
                    4
                else
                    3
            }
            else
            {
                if (inlineWideInstructions)
                    6
                else
                    5
            }
            )
    }

    trait I2L extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.I2L
    }

    trait I2F extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.I2F
    }

    trait I2D extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.I2D
    }

    trait L2I extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.L2I
    }

    trait L2F extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.L2F
    }

    trait L2D extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.L2D
    }

    trait F2I extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.F2I
    }

    trait F2L extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.F2L
    }

    trait F2D extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.F2D
    }

    trait D2I extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.D2I
    }

    trait D2L extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.D2L
    }

    trait D2F extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.D2F
    }

    trait I2B extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.I2B
    }

    trait I2C extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.I2C
    }


    trait I2S extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.I2S
    }


    trait LCMP extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.LCMP
    }

    trait FCMPL extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.FCMPL
    }

    trait FCMPG extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.FCMPG
    }

    trait DCMPL extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.DCMPL
    }

    trait DCMPG extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.DCMPG
    }

    trait IFEQ extends ConditionalJumpInstruction
    {
        override def opcode = BytecodeOpCodes.IFEQ
    }

    trait IFNE extends ConditionalJumpInstruction
    {
        override def opcode = BytecodeOpCodes.IFNE
    }

    trait IFLT extends ConditionalJumpInstruction
    {
        override def opcode = BytecodeOpCodes.IFLT
    }

    trait IFGE extends ConditionalJumpInstruction
    {
        override def opcode = BytecodeOpCodes.IFGE
    }

    trait IFGT extends ConditionalJumpInstruction
    {
        override def opcode = BytecodeOpCodes.IFGT
    }

    trait IFLE extends ConditionalJumpInstruction
    {
        override def opcode = BytecodeOpCodes.IFLE
    }

    trait IF_ICMPEQ extends ConditionalJumpInstruction
    {
        override def opcode = BytecodeOpCodes.IF_ICMPEQ
    }

    trait IF_ICMPNE extends ConditionalJumpInstruction
    {
        override def opcode = BytecodeOpCodes.IF_ICMPNE
    }

    trait IF_ICMPLT extends ConditionalJumpInstruction
    {
        override def opcode = BytecodeOpCodes.IF_ICMPLT
    }

    trait IF_ICMPGE extends ConditionalJumpInstruction
    {
        override def opcode = BytecodeOpCodes.IF_ICMPGE
    }

    trait IF_ICMPGT extends ConditionalJumpInstruction
    {
        override def opcode = BytecodeOpCodes.IF_ICMPGT
    }

    trait IF_ICMPLE extends ConditionalJumpInstruction
    {
        override def opcode = BytecodeOpCodes.IF_ICMPLE
    }

    trait IF_ACMPEQ extends ConditionalJumpInstruction
    {
        override def opcode = BytecodeOpCodes.IF_ACMPEQ
    }

    trait IF_ACMPNE extends ConditionalJumpInstruction
    {
        override def opcode = BytecodeOpCodes.IF_ACMPNE
    }

    trait GOTO extends UnconditionalJumpInstruction
    {
        override def opcode = BytecodeOpCodes.GOTO
    }

    trait JSR extends UnconditionalJumpInstruction
    {
        override def opcode = BytecodeOpCodes.JSR
    }

    trait RET extends LocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.RET

        override def nextProgramCounter = programCounter + 2
    }

    trait TABLESWITCH extends Instruction
    {
        override def opcode = BytecodeOpCodes.TABLESWITCH

        override def nextProgramCounter = programCounter +
            1 + (3 - (programCounter % 4)) + 12 + offsets.size * 4

        def defaultOffset: Int

        def low: Int

        def high: Int

        def offsets: Seq[Int]
    }

    trait LOOKUPSWITCH extends Instruction
    {
        override def opcode = BytecodeOpCodes.LOOKUPSWITCH

        override def nextProgramCounter = programCounter +
            1 + (3 - (programCounter % 4)) + 8 + keys.size * 8

        def defaultOffset: Int

        def keys: Seq[Int]

        def offsets: Seq[Int]
    }

    trait IRETURN extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.IRETURN
    }

    trait LRETURN extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.LRETURN
    }

    trait FRETURN extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.FRETURN
    }

    trait DRETURN extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.DRETURN
    }

    trait ARETURN extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.ARETURN
    }

    trait RETURN extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.RETURN
    }

    trait GETSTATIC extends FieldInstruction
    {
        override def opcode = BytecodeOpCodes.GETSTATIC
    }

    trait PUTSTATIC extends FieldInstruction
    {
        override def opcode = BytecodeOpCodes.PUTSTATIC
    }

    trait GETFIELD extends FieldInstruction
    {
        override def opcode = BytecodeOpCodes.GETFIELD
    }

    trait PUTFIELD extends FieldInstruction
    {
        override def opcode = BytecodeOpCodes.PUTFIELD
    }

    trait INVOKEVIRTUAL extends MethodInstruction
    {
        override def opcode = BytecodeOpCodes.INVOKEVIRTUAL

        override def nextProgramCounter = programCounter + 3
    }

    trait INVOKESPECIAL extends MethodInstruction
    {
        override def opcode = BytecodeOpCodes.INVOKESPECIAL

        override def nextProgramCounter = programCounter + 3
    }

    trait INVOKESTATIC extends MethodInstruction
    {
        override def opcode = BytecodeOpCodes.INVOKESTATIC

        override def nextProgramCounter = programCounter + 3
    }

    trait INVOKEINTERFACE extends MethodInstruction
    {
        override def opcode = BytecodeOpCodes.INVOKEINTERFACE

        override def nextProgramCounter = programCounter + 5
    }

    trait INVOKEDYNAMIC extends MethodInstruction
    {
        override def opcode = BytecodeOpCodes.INVOKEDYNAMIC

        override def nextProgramCounter = programCounter + 5
    }


    trait NEW extends ObjectTypeInstruction
    {
        override def opcode = BytecodeOpCodes.NEW
    }

    trait NEWARRAY extends NewArrayInstruction[PrimitiveType]
    {
        override def opcode = BytecodeOpCodes.NEWARRAY

        override def nextProgramCounter = programCounter + 2
    }

    trait ANEWARRAY extends NewArrayInstruction[ObjectType]
    {
        override def opcode = BytecodeOpCodes.ANEWARRAY

        override def nextProgramCounter = programCounter + 3
    }

    trait ARRAYLENGTH extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.ARRAYLENGTH
    }

    trait ATHROW extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.ATHROW
    }

    trait CHECKCAST extends ObjectTypeInstruction
    {
        override def opcode = BytecodeOpCodes.CHECKCAST
    }

    trait INSTANCEOF extends ObjectTypeInstruction
    {
        override def opcode = BytecodeOpCodes.INSTANCEOF
    }

    trait MONITORENTER extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.MONITORENTER
    }

    trait MONITOREXIT extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.MONITOREXIT
    }

    trait WIDE extends BasicInstruction
    {
        override def opcode = BytecodeOpCodes.WIDE
    }

    trait MULTIANEWARRAY[V] extends NewArrayInstruction[ArrayType[V]]
    {
        override def opcode = BytecodeOpCodes.MULTIANEWARRAY

        override def nextProgramCounter = programCounter + 4

        def dimensions: Int
    }

    trait IFNULL extends ConditionalJumpInstruction
    {
        override def opcode = BytecodeOpCodes.IFNULL
    }

    trait IFNONNULL extends ConditionalJumpInstruction
    {
        override def opcode = BytecodeOpCodes.IFNONNULL
    }

    trait GOTO_W extends UnconditionalJumpInstruction
    {
        override def opcode = BytecodeOpCodes.GOTO_W
    }

    trait JSR_W extends UnconditionalJumpInstruction
    {
        override def opcode = BytecodeOpCodes.JSR_W
    }


}
