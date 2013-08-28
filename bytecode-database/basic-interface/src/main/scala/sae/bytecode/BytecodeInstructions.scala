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



    trait LocalVariableInstruction extends Instruction
    {
        def localVariableIndex: Int
    }

    trait ConstantValueInstruction[V] extends Instruction
    {
        def value: V
    }

    trait FieldInstruction extends Instruction
    {
        def fieldInfo: FieldInfo
    }

    trait MethodInstruction extends Instruction
    {
        def methodInfo: MethodInfo
    }

    trait ObjectTypeInstruction extends Instruction
    {
        def objectType: ObjectType
    }

    trait JumpInstruction extends Instruction
    {
        def offset: Int
    }

    trait NewArrayInstruction[V] extends Instruction
    {
        def elementType: V

        def arrayType: ArrayType[V]
    }

    trait NOP extends Instruction
    {
        override def opcode = BytecodeOpCodes.NOP

    }

    trait ACONST_NULL extends Instruction
    {
        override def opcode = BytecodeOpCodes.ACONST_NULL
    }

    trait ICONST_M1 extends Instruction
    {
        override def opcode = BytecodeOpCodes.ICONST_M1
    }

    trait ICONST_0 extends Instruction
    {
        override def opcode = BytecodeOpCodes.ICONST_0
    }

    trait ICONST_1 extends Instruction
    {
        override def opcode = BytecodeOpCodes.ICONST_1
    }

    trait ICONST_2 extends Instruction
    {
        override def opcode = BytecodeOpCodes.ICONST_2
    }

    trait ICONST_3 extends Instruction
    {
        override def opcode = BytecodeOpCodes.ICONST_3
    }

    trait ICONST_4 extends Instruction
    {
        override def opcode = BytecodeOpCodes.ICONST_4
    }

    trait ICONST_5 extends Instruction
    {
        override def opcode = BytecodeOpCodes.ICONST_5
    }

    trait LCONST_0 extends Instruction
    {
        override def opcode = BytecodeOpCodes.LCONST_0
    }

    trait LCONST_1 extends Instruction
    {
        override def opcode = BytecodeOpCodes.LCONST_1
    }

    trait FCONST_0 extends Instruction
    {
        override def opcode = BytecodeOpCodes.FCONST_0
    }

    trait FCONST_1 extends Instruction
    {
        override def opcode = BytecodeOpCodes.FCONST_1
    }

    trait FCONST_2 extends Instruction
    {
        override def opcode = BytecodeOpCodes.FCONST_2
    }

    trait DCONST_0 extends Instruction
    {
        override def opcode = BytecodeOpCodes.DCONST_0
    }

    trait DCONST_1 extends Instruction
    {
        override def opcode = BytecodeOpCodes.DCONST_1
    }

    trait BIPUSH extends ConstantValueInstruction[Byte]
    {
        override def opcode = BytecodeOpCodes.BIPUSH
    }

    trait SIPUSH extends ConstantValueInstruction[Short]
    {
        override def opcode = BytecodeOpCodes.SIPUSH
    }

    trait LDC[V] extends ConstantValueInstruction[V]
    {
        override def opcode = BytecodeOpCodes.LDC
    }

    trait LDC_W[V] extends ConstantValueInstruction[V]
    {
        override def opcode = BytecodeOpCodes.LDC_W
    }

    trait LDC2_W[V] extends ConstantValueInstruction[V]
    {
        override def opcode = BytecodeOpCodes.LDC2_W
    }

    trait ILOAD extends LocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.ILOAD
    }

    trait LLOAD extends LocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.LLOAD
    }

    trait FLOAD extends LocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.FLOAD
    }

    trait DLOAD extends LocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.DLOAD
    }

    trait ALOAD extends LocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.ALOAD
    }

    trait ILOAD_0 extends Instruction
    {
        override def opcode = BytecodeOpCodes.ILOAD_0
    }

    trait ILOAD_1 extends Instruction
    {
        override def opcode = BytecodeOpCodes.ILOAD_1
    }

    trait ILOAD_2 extends Instruction
    {
        override def opcode = BytecodeOpCodes.ILOAD_2
    }

    trait ILOAD_3 extends Instruction
    {
        override def opcode = BytecodeOpCodes.ILOAD_3
    }

    trait LLOAD_0 extends Instruction
    {
        override def opcode = BytecodeOpCodes.LLOAD_0
    }

    trait LLOAD_1 extends Instruction
    {
        override def opcode = BytecodeOpCodes.LLOAD_1
    }

    trait LLOAD_2 extends Instruction
    {
        override def opcode = BytecodeOpCodes.LLOAD_2
    }

    trait LLOAD_3 extends Instruction
    {
        override def opcode = BytecodeOpCodes.LLOAD_3
    }

    trait FLOAD_0 extends Instruction
    {
        override def opcode = BytecodeOpCodes.FLOAD_0
    }

    trait FLOAD_1 extends Instruction
    {
        override def opcode = BytecodeOpCodes.FLOAD_1
    }

    trait FLOAD_2 extends Instruction
    {
        override def opcode = BytecodeOpCodes.FLOAD_2
    }

    trait FLOAD_3 extends Instruction
    {
        override def opcode = BytecodeOpCodes.FLOAD_3
    }

    trait DLOAD_0 extends Instruction
    {
        override def opcode = BytecodeOpCodes.DLOAD_0
    }

    trait DLOAD_1 extends Instruction
    {
        override def opcode = BytecodeOpCodes.DLOAD_1
    }

    trait DLOAD_2 extends Instruction
    {
        override def opcode = BytecodeOpCodes.DLOAD_2
    }

    trait DLOAD_3 extends Instruction
    {
        override def opcode = BytecodeOpCodes.DLOAD_3
    }

    trait ALOAD_0 extends Instruction
    {
        override def opcode = BytecodeOpCodes.ALOAD_0
    }

    trait ALOAD_1 extends Instruction
    {
        override def opcode = BytecodeOpCodes.ALOAD_1
    }

    trait ALOAD_2 extends Instruction
    {
        override def opcode = BytecodeOpCodes.ALOAD_2
    }

    trait ALOAD_3 extends Instruction
    {
        override def opcode = BytecodeOpCodes.ALOAD_3
    }

    trait IALOAD extends Instruction
    {
        override def opcode = BytecodeOpCodes.IALOAD
    }

    trait LALOAD extends Instruction
    {
        override def opcode = BytecodeOpCodes.LALOAD
    }

    trait FALOAD extends Instruction
    {
        override def opcode = BytecodeOpCodes.FALOAD
    }

    trait DALOAD extends Instruction
    {
        override def opcode = BytecodeOpCodes.DALOAD
    }

    trait AALOAD extends Instruction
    {
        override def opcode = BytecodeOpCodes.AALOAD
    }

    trait BALOAD extends Instruction
    {
        override def opcode = BytecodeOpCodes.BALOAD
    }

    trait CALOAD extends Instruction
    {
        override def opcode = BytecodeOpCodes.CALOAD
    }

    trait SALOAD extends Instruction
    {
        override def opcode = BytecodeOpCodes.SALOAD
    }

    trait ISTORE extends LocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.ISTORE
    }

    trait LSTORE extends LocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.LSTORE
    }

    trait FSTORE extends LocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.FSTORE
    }

    trait DSTORE extends LocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.DSTORE
    }

    trait ASTORE extends LocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.ASTORE
    }

    trait ISTORE_0 extends Instruction
    {
        override def opcode = BytecodeOpCodes.ISTORE_0
    }

    trait ISTORE_1 extends Instruction
    {
        override def opcode = BytecodeOpCodes.ISTORE_1
    }

    trait ISTORE_2 extends Instruction
    {
        override def opcode = BytecodeOpCodes.ISTORE_2
    }

    trait ISTORE_3 extends Instruction
    {
        override def opcode = BytecodeOpCodes.ISTORE_3
    }

    trait LSTORE_0 extends Instruction
    {
        override def opcode = BytecodeOpCodes.LSTORE_0
    }

    trait LSTORE_1 extends Instruction
    {
        override def opcode = BytecodeOpCodes.LSTORE_1
    }

    trait LSTORE_2 extends Instruction
    {
        override def opcode = BytecodeOpCodes.LSTORE_2
    }

    trait LSTORE_3 extends Instruction
    {
        override def opcode = BytecodeOpCodes.LSTORE_3
    }

    trait FSTORE_0 extends Instruction
    {
        override def opcode = BytecodeOpCodes.FSTORE_0
    }

    trait FSTORE_1 extends Instruction
    {
        override def opcode = BytecodeOpCodes.FSTORE_1
    }

    trait FSTORE_2 extends Instruction
    {
        override def opcode = BytecodeOpCodes.FSTORE_2
    }

    trait FSTORE_3 extends Instruction
    {
        override def opcode = BytecodeOpCodes.FSTORE_3
    }

    trait DSTORE_0 extends Instruction
    {
        override def opcode = BytecodeOpCodes.DSTORE_0
    }

    trait DSTORE_1 extends Instruction
    {
        override def opcode = BytecodeOpCodes.DSTORE_1
    }

    trait DSTORE_2 extends Instruction
    {
        override def opcode = BytecodeOpCodes.DSTORE_2
    }

    trait DSTORE_3 extends Instruction
    {
        override def opcode = BytecodeOpCodes.DSTORE_3
    }

    trait ASTORE_0 extends Instruction
    {
        override def opcode = BytecodeOpCodes.ASTORE_0
    }

    trait ASTORE_1 extends Instruction
    {
        override def opcode = BytecodeOpCodes.ASTORE_1
    }

    trait ASTORE_2 extends Instruction
    {
        override def opcode = BytecodeOpCodes.ASTORE_2
    }

    trait ASTORE_3 extends Instruction
    {
        override def opcode = BytecodeOpCodes.ASTORE_3
    }

    trait IASTORE extends Instruction
    {
        override def opcode = BytecodeOpCodes.IASTORE
    }

    trait LASTORE extends Instruction
    {
        override def opcode = BytecodeOpCodes.LASTORE
    }

    trait FASTORE extends Instruction
    {
        override def opcode = BytecodeOpCodes.FASTORE
    }

    trait DASTORE extends Instruction
    {
        override def opcode = BytecodeOpCodes.DASTORE
    }

    trait AASTORE extends Instruction
    {
        override def opcode = BytecodeOpCodes.AASTORE
    }

    trait BASTORE extends Instruction
    {
        override def opcode = BytecodeOpCodes.BASTORE
    }

    trait CASTORE extends Instruction
    {
        override def opcode = BytecodeOpCodes.CASTORE
    }

    trait SASTORE extends Instruction
    {
        override def opcode = BytecodeOpCodes.SASTORE
    }

    trait POP extends Instruction
    {
        override def opcode = BytecodeOpCodes.POP
    }

    trait POP2 extends Instruction
    {
        override def opcode = BytecodeOpCodes.POP2
    }

    trait DUP extends Instruction
    {
        override def opcode = BytecodeOpCodes.DUP
    }

    trait DUP_X1 extends Instruction
    {
        override def opcode = BytecodeOpCodes.DUP_X1
    }

    trait DUP_X2 extends Instruction
    {
        override def opcode = BytecodeOpCodes.DUP_X2
    }

    trait DUP2 extends Instruction
    {
        override def opcode = BytecodeOpCodes.DUP2
    }

    trait DUP2_X1 extends Instruction
    {
        override def opcode = BytecodeOpCodes.DUP2_X1
    }

    trait DUP2_X2 extends Instruction
    {
        override def opcode = BytecodeOpCodes.DUP2_X2
    }

    trait SWAP extends Instruction
    {
        override def opcode = BytecodeOpCodes.SWAP
    }

    trait IADD extends Instruction
    {
        override def opcode = BytecodeOpCodes.IADD
    }

    trait LADD extends Instruction
    {
        override def opcode = BytecodeOpCodes.LADD
    }

    trait FADD extends Instruction
    {
        override def opcode = BytecodeOpCodes.FADD
    }

    trait DADD extends Instruction
    {
        override def opcode = BytecodeOpCodes.DADD
    }

    trait ISUB extends Instruction
    {
        override def opcode = BytecodeOpCodes.ISUB
    }

    trait LSUB extends Instruction
    {
        override def opcode = BytecodeOpCodes.LSUB
    }

    trait FSUB extends Instruction
    {
        override def opcode = BytecodeOpCodes.FSUB
    }

    trait DSUB extends Instruction
    {
        override def opcode = BytecodeOpCodes.DSUB
    }

    trait IMUL extends Instruction
    {
        override def opcode = BytecodeOpCodes.IMUL
    }

    trait LMUL extends Instruction
    {
        override def opcode = BytecodeOpCodes.LMUL
    }

    trait FMUL extends Instruction
    {
        override def opcode = BytecodeOpCodes.FMUL
    }

    trait DMUL extends Instruction
    {
        override def opcode = BytecodeOpCodes.DMUL
    }

    trait IDIV extends Instruction
    {
        override def opcode = BytecodeOpCodes.IDIV
    }

    trait LDIV extends Instruction
    {
        override def opcode = BytecodeOpCodes.LDIV
    }

    trait FDIV extends Instruction
    {
        override def opcode = BytecodeOpCodes.FDIV
    }

    trait DDIV extends Instruction
    {
        override def opcode = BytecodeOpCodes.DDIV
    }

    trait IREM extends Instruction
    {
        override def opcode = BytecodeOpCodes.IREM
    }

    trait LREM extends Instruction
    {
        override def opcode = BytecodeOpCodes.LREM
    }

    trait FREM extends Instruction
    {
        override def opcode = BytecodeOpCodes.FREM
    }

    trait DREM extends Instruction
    {
        override def opcode = BytecodeOpCodes.DREM
    }

    trait INEG extends Instruction
    {
        override def opcode = BytecodeOpCodes.INEG
    }

    trait LNEG extends Instruction
    {
        override def opcode = BytecodeOpCodes.LNEG
    }

    trait FNEG extends Instruction
    {
        override def opcode = BytecodeOpCodes.FNEG
    }

    trait DNEG extends Instruction
    {
        override def opcode = BytecodeOpCodes.DNEG
    }

    trait ISHL extends Instruction
    {
        override def opcode = BytecodeOpCodes.ISHL
    }

    trait LSHL extends Instruction
    {
        override def opcode = BytecodeOpCodes.LSHL
    }

    trait ISHR extends Instruction
    {
        override def opcode = BytecodeOpCodes.ISHR
    }

    trait LSHR extends Instruction
    {
        override def opcode = BytecodeOpCodes.LSHR
    }

    trait IUSHR extends Instruction
    {
        override def opcode = BytecodeOpCodes.IUSHR
    }

    trait LUSHR extends Instruction
    {
        override def opcode = BytecodeOpCodes.LUSHR
    }

    trait IAND extends Instruction
    {
        override def opcode = BytecodeOpCodes.IAND
    }

    trait LAND extends Instruction
    {
        override def opcode = BytecodeOpCodes.LAND
    }

    trait IOR extends Instruction
    {
        override def opcode = BytecodeOpCodes.IOR
    }

    trait LOR extends Instruction
    {
        override def opcode = BytecodeOpCodes.LOR
    }

    trait IXOR extends Instruction
    {
        override def opcode = BytecodeOpCodes.IXOR
    }

    trait LXOR extends Instruction
    {
        override def opcode = BytecodeOpCodes.LXOR
    }

    trait IINC extends LocalVariableInstruction with ConstantValueInstruction[Int]
    {
        override def opcode = BytecodeOpCodes.IINC
    }

    trait I2L extends Instruction
    {
        override def opcode = BytecodeOpCodes.I2L
    }

    trait I2F extends Instruction
    {
        override def opcode = BytecodeOpCodes.I2F
    }

    trait I2D extends Instruction
    {
        override def opcode = BytecodeOpCodes.I2D
    }

    trait L2I extends Instruction
    {
        override def opcode = BytecodeOpCodes.L2I
    }

    trait L2F extends Instruction
    {
        override def opcode = BytecodeOpCodes.L2F
    }

    trait L2D extends Instruction
    {
        override def opcode = BytecodeOpCodes.L2D
    }

    trait F2I extends Instruction
    {
        override def opcode = BytecodeOpCodes.F2I
    }

    trait F2L extends Instruction
    {
        override def opcode = BytecodeOpCodes.F2L
    }

    trait F2D extends Instruction
    {
        override def opcode = BytecodeOpCodes.F2D
    }

    trait D2I extends Instruction
    {
        override def opcode = BytecodeOpCodes.D2I
    }

    trait D2L extends Instruction
    {
        override def opcode = BytecodeOpCodes.D2L
    }

    trait D2F extends Instruction
    {
        override def opcode = BytecodeOpCodes.D2F
    }

    trait I2B extends Instruction
    {
        override def opcode = BytecodeOpCodes.I2B
    }

    trait INT2BYTE extends Instruction
    {
        override def opcode = BytecodeOpCodes.INT2BYTE
    }

    trait I2C extends Instruction
    {
        override def opcode = BytecodeOpCodes.I2C
    }

    trait INT2CHAR extends Instruction
    {
        override def opcode = BytecodeOpCodes.INT2CHAR
    }

    trait I2S extends Instruction
    {
        override def opcode = BytecodeOpCodes.I2S
    }

    trait INT2SHORT extends Instruction
    {
        override def opcode = BytecodeOpCodes.INT2SHORT
    }

    trait LCMP extends Instruction
    {
        override def opcode = BytecodeOpCodes.LCMP
    }

    trait FCMPL extends Instruction
    {
        override def opcode = BytecodeOpCodes.FCMPL
    }

    trait FCMPG extends Instruction
    {
        override def opcode = BytecodeOpCodes.FCMPG
    }

    trait DCMPL extends Instruction
    {
        override def opcode = BytecodeOpCodes.DCMPL
    }

    trait DCMPG extends Instruction
    {
        override def opcode = BytecodeOpCodes.DCMPG
    }

    trait IFEQ extends JumpInstruction
    {
        override def opcode = BytecodeOpCodes.IFEQ
    }

    trait IFNE extends JumpInstruction
    {
        override def opcode = BytecodeOpCodes.IFNE
    }

    trait IFLT extends JumpInstruction
    {
        override def opcode = BytecodeOpCodes.IFLT
    }

    trait IFGE extends JumpInstruction
    {
        override def opcode = BytecodeOpCodes.IFGE
    }

    trait IFGT extends JumpInstruction
    {
        override def opcode = BytecodeOpCodes.IFGT
    }

    trait IFLE extends JumpInstruction
    {
        override def opcode = BytecodeOpCodes.IFLE
    }

    trait IF_ICMPEQ extends JumpInstruction
    {
        override def opcode = BytecodeOpCodes.IF_ICMPEQ
    }

    trait IF_ICMPNE extends JumpInstruction
    {
        override def opcode = BytecodeOpCodes.IF_ICMPNE
    }

    trait IF_ICMPLT extends JumpInstruction
    {
        override def opcode = BytecodeOpCodes.IF_ICMPLT
    }

    trait IF_ICMPGE extends JumpInstruction
    {
        override def opcode = BytecodeOpCodes.IF_ICMPGE
    }

    trait IF_ICMPGT extends JumpInstruction
    {
        override def opcode = BytecodeOpCodes.IF_ICMPGT
    }

    trait IF_ICMPLE extends JumpInstruction
    {
        override def opcode = BytecodeOpCodes.IF_ICMPLE
    }

    trait IF_ACMPEQ extends JumpInstruction
    {
        override def opcode = BytecodeOpCodes.IF_ACMPEQ
    }

    trait IF_ACMPNE extends JumpInstruction
    {
        override def opcode = BytecodeOpCodes.IF_ACMPNE
    }

    trait GOTO extends JumpInstruction
    {
        override def opcode = BytecodeOpCodes.GOTO
    }

    trait JSR extends JumpInstruction
    {
        override def opcode = BytecodeOpCodes.JSR
    }

    trait RET extends LocalVariableInstruction
    {
        override def opcode = BytecodeOpCodes.RET
    }

    trait TABLESWITCH extends Instruction
    {
        override def opcode = BytecodeOpCodes.TABLESWITCH

        def defaultOffset: Int

        def low: Int

        def high: Int

        def offsets: Seq[Int]
    }

    trait LOOKUPSWITCH extends Instruction
    {
        override def opcode = BytecodeOpCodes.LOOKUPSWITCH

        def defaultOffset: Int

        def keys: Seq[Int]

        def offsets: Seq[Int]
    }

    trait IRETURN extends Instruction
    {
        override def opcode = BytecodeOpCodes.IRETURN
    }

    trait LRETURN extends Instruction
    {
        override def opcode = BytecodeOpCodes.LRETURN
    }

    trait FRETURN extends Instruction
    {
        override def opcode = BytecodeOpCodes.FRETURN
    }

    trait DRETURN extends Instruction
    {
        override def opcode = BytecodeOpCodes.DRETURN
    }

    trait ARETURN extends Instruction
    {
        override def opcode = BytecodeOpCodes.ARETURN
    }

    trait RETURN extends Instruction
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
    }

    trait INVOKESPECIAL extends MethodInstruction
    {
        override def opcode = BytecodeOpCodes.INVOKESPECIAL
    }

    trait INVOKENONVIRTUAL extends MethodInstruction
    {
        override def opcode = BytecodeOpCodes.INVOKENONVIRTUAL
    }

    trait INVOKESTATIC extends MethodInstruction
    {
        override def opcode = BytecodeOpCodes.INVOKESTATIC
    }

    trait INVOKEINTERFACE extends MethodInstruction
    {
        override def opcode = BytecodeOpCodes.INVOKEINTERFACE
    }

    trait NEW extends ObjectTypeInstruction
    {
        override def opcode = BytecodeOpCodes.NEW
    }

    trait NEWARRAY extends NewArrayInstruction[PrimitiveType]
    {
        override def opcode = BytecodeOpCodes.NEWARRAY
    }

    trait ANEWARRAY extends NewArrayInstruction[ObjectType]
    {
        override def opcode = BytecodeOpCodes.ANEWARRAY
    }

    trait ARRAYLENGTH extends Instruction
    {
        override def opcode = BytecodeOpCodes.ARRAYLENGTH
    }

    trait ATHROW extends Instruction
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

    trait MONITORENTER extends Instruction
    {
        override def opcode = BytecodeOpCodes.MONITORENTER
    }

    trait MONITOREXIT extends Instruction
    {
        override def opcode = BytecodeOpCodes.MONITOREXIT
    }

    trait WIDE extends Instruction
    {
        override def opcode = BytecodeOpCodes.WIDE
    }

    trait MULTIANEWARRAY[V] extends NewArrayInstruction[ArrayType[V]]
    {
        override def opcode = BytecodeOpCodes.MULTIANEWARRAY

        def dimensions: Int
    }

    trait IFNULL extends JumpInstruction
    {
        override def opcode = BytecodeOpCodes.IFNULL
    }

    trait IFNONNULL extends JumpInstruction
    {
        override def opcode = BytecodeOpCodes.IFNONNULL
    }

    trait GOTO_W extends Instruction
    {
        override def opcode = BytecodeOpCodes.GOTO_W
    }

    trait JSR_W extends Instruction
    {
        override def opcode = BytecodeOpCodes.JSR_W
    }


}
