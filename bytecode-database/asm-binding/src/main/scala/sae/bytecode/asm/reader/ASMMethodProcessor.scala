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
package sae.bytecode.asm.reader

import scala.annotation.switch
import org.objectweb.asm._
import sae.bytecode.asm.ext.LabelExt

/**
 *
 * @author Ralf Mitschke
 */
trait ASMMethodProcessor
    extends ASMElementProcessor
{

    import database._

    def methodVisitor (methodDeclaration: MethodDeclaration): MethodVisitor =
        new ASMMethodVisitor (methodDeclaration)

    class ASMMethodVisitor (
        val methodDeclaration: MethodDeclaration
    )
        extends MethodVisitor (Opcodes.ASM4)
    {
        var maxStack = -1

        var maxLocals = -1

        var pc = 0

        var index = 0

        override def visitMaxs (maxStack: Int, maxLocals: Int) {
            this.maxStack = maxStack
            this.maxLocals = maxLocals
        }


        /**
         * store the instruction and do computations for pc .
         */
        protected def storeInstruction (instruction: Instruction) {
            processInstruction (instruction)
            index += 1
            pc = instruction.nextProgramCounter
        }

        override def visitFieldInsn (opcode: Int, owner: String, name: String, desc: String) {
            val declaringType = Type.getType (owner)
            val fieldType = Type.getType (desc)
            val fieldInfo = FieldInfo (
                declaringType,
                name,
                fieldType
            )
            val instruction =
                (opcode: @switch) match {
                    case Opcodes.GETFIELD => GETFIELD (methodDeclaration, pc, index, fieldInfo)
                    case Opcodes.PUTFIELD => PUTFIELD (methodDeclaration, pc, index, fieldInfo)
                    case Opcodes.GETSTATIC => GETSTATIC (methodDeclaration, pc, index, fieldInfo)
                    case Opcodes.PUTSTATIC => PUTSTATIC (methodDeclaration, pc, index, fieldInfo)
                    case _ => throw new IllegalArgumentException
                }

            storeInstruction (instruction)
        }

        override def visitIincInsn (`var`: Int, increment: Int) {
            storeInstruction (
                IINC (methodDeclaration, pc, index, `var`, increment)
            )
        }

        override def visitInsn (opcode: Int) {
            val instruction =
                (opcode: @switch) match {
                    case Opcodes.NOP => NOP (methodDeclaration, pc, index)
                    case Opcodes.ACONST_NULL => ACONST_NULL (methodDeclaration, pc, index)

                    case Opcodes.ICONST_M1 => ICONST_M1 (methodDeclaration, pc, index)
                    case Opcodes.ICONST_0 => ICONST_0 (methodDeclaration, pc, index)
                    case Opcodes.ICONST_1 => ICONST_1 (methodDeclaration, pc, index)
                    case Opcodes.ICONST_2 => ICONST_2 (methodDeclaration, pc, index)
                    case Opcodes.ICONST_3 => ICONST_3 (methodDeclaration, pc, index)
                    case Opcodes.ICONST_4 => ICONST_4 (methodDeclaration, pc, index)
                    case Opcodes.ICONST_5 => ICONST_5 (methodDeclaration, pc, index)

                    case Opcodes.LCONST_0 => LCONST_0 (methodDeclaration, pc, index)
                    case Opcodes.LCONST_1 => LCONST_1 (methodDeclaration, pc, index)
                    case Opcodes.FCONST_0 => FCONST_0 (methodDeclaration, pc, index)
                    case Opcodes.FCONST_1 => FCONST_1 (methodDeclaration, pc, index)
                    case Opcodes.FCONST_2 => FCONST_2 (methodDeclaration, pc, index)
                    case Opcodes.DCONST_0 => DCONST_0 (methodDeclaration, pc, index)
                    case Opcodes.DCONST_1 => DCONST_1 (methodDeclaration, pc, index)

                    case Opcodes.IALOAD => IALOAD (methodDeclaration, pc, index)
                    case Opcodes.LALOAD => LALOAD (methodDeclaration, pc, index)
                    case Opcodes.FALOAD => FALOAD (methodDeclaration, pc, index)
                    case Opcodes.DALOAD => DALOAD (methodDeclaration, pc, index)
                    case Opcodes.AALOAD => AALOAD (methodDeclaration, pc, index)
                    case Opcodes.BALOAD => BALOAD (methodDeclaration, pc, index)
                    case Opcodes.CALOAD => CALOAD (methodDeclaration, pc, index)
                    case Opcodes.SALOAD => SALOAD (methodDeclaration, pc, index)

                    case Opcodes.IASTORE => IASTORE (methodDeclaration, pc, index)
                    case Opcodes.LASTORE => LASTORE (methodDeclaration, pc, index)
                    case Opcodes.FASTORE => FASTORE (methodDeclaration, pc, index)
                    case Opcodes.DASTORE => DASTORE (methodDeclaration, pc, index)
                    case Opcodes.AASTORE => AASTORE (methodDeclaration, pc, index)
                    case Opcodes.BASTORE => BASTORE (methodDeclaration, pc, index)
                    case Opcodes.CASTORE => CASTORE (methodDeclaration, pc, index)
                    case Opcodes.SASTORE => SASTORE (methodDeclaration, pc, index)

                    case Opcodes.POP => POP (methodDeclaration, pc, index)
                    case Opcodes.POP2 => POP2 (methodDeclaration, pc, index)
                    case Opcodes.DUP => DUP (methodDeclaration, pc, index)
                    case Opcodes.DUP_X1 => DUP_X1 (methodDeclaration, pc, index)
                    case Opcodes.DUP_X2 => DUP_X2 (methodDeclaration, pc, index)
                    case Opcodes.DUP2 => DUP2 (methodDeclaration, pc, index)
                    case Opcodes.DUP2_X1 => DUP2_X1 (methodDeclaration, pc, index)
                    case Opcodes.DUP2_X2 => DUP2_X2 (methodDeclaration, pc, index)
                    case Opcodes.SWAP => SWAP (methodDeclaration, pc, index)

                    case Opcodes.IADD => IADD (methodDeclaration, pc, index)
                    case Opcodes.LADD => LADD (methodDeclaration, pc, index)
                    case Opcodes.FADD => FADD (methodDeclaration, pc, index)
                    case Opcodes.DADD => DADD (methodDeclaration, pc, index)
                    case Opcodes.ISUB => ISUB (methodDeclaration, pc, index)
                    case Opcodes.LSUB => LSUB (methodDeclaration, pc, index)
                    case Opcodes.FSUB => FSUB (methodDeclaration, pc, index)
                    case Opcodes.DSUB => DSUB (methodDeclaration, pc, index)

                    case Opcodes.IMUL => IMUL (methodDeclaration, pc, index)
                    case Opcodes.LMUL => LMUL (methodDeclaration, pc, index)
                    case Opcodes.FMUL => FMUL (methodDeclaration, pc, index)
                    case Opcodes.DMUL => DMUL (methodDeclaration, pc, index)
                    case Opcodes.IDIV => IDIV (methodDeclaration, pc, index)
                    case Opcodes.LDIV => LDIV (methodDeclaration, pc, index)
                    case Opcodes.FDIV => FDIV (methodDeclaration, pc, index)
                    case Opcodes.DDIV => DDIV (methodDeclaration, pc, index)

                    case Opcodes.IREM => IREM (methodDeclaration, pc, index)
                    case Opcodes.LREM => LREM (methodDeclaration, pc, index)
                    case Opcodes.FREM => FREM (methodDeclaration, pc, index)
                    case Opcodes.DREM => DREM (methodDeclaration, pc, index)

                    case Opcodes.INEG => INEG (methodDeclaration, pc, index)
                    case Opcodes.LNEG => LNEG (methodDeclaration, pc, index)
                    case Opcodes.FNEG => FNEG (methodDeclaration, pc, index)
                    case Opcodes.DNEG => DNEG (methodDeclaration, pc, index)

                    case Opcodes.ISHL => ISHL (methodDeclaration, pc, index)
                    case Opcodes.LSHL => LSHL (methodDeclaration, pc, index)
                    case Opcodes.ISHR => ISHR (methodDeclaration, pc, index)
                    case Opcodes.LSHR => LSHR (methodDeclaration, pc, index)
                    case Opcodes.IUSHR => IUSHR (methodDeclaration, pc, index)
                    case Opcodes.LUSHR => LUSHR (methodDeclaration, pc, index)

                    case Opcodes.IAND => IAND (methodDeclaration, pc, index)
                    case Opcodes.LAND => LAND (methodDeclaration, pc, index)
                    case Opcodes.IOR => IOR (methodDeclaration, pc, index)
                    case Opcodes.LOR => LOR (methodDeclaration, pc, index)
                    case Opcodes.IXOR => IXOR (methodDeclaration, pc, index)
                    case Opcodes.LXOR => LXOR (methodDeclaration, pc, index)

                    case Opcodes.I2L => I2L (methodDeclaration, pc, index)
                    case Opcodes.I2F => I2F (methodDeclaration, pc, index)
                    case Opcodes.I2D => I2D (methodDeclaration, pc, index)
                    case Opcodes.L2I => L2I (methodDeclaration, pc, index)
                    case Opcodes.L2F => L2F (methodDeclaration, pc, index)
                    case Opcodes.L2D => L2D (methodDeclaration, pc, index)
                    case Opcodes.F2I => F2I (methodDeclaration, pc, index)
                    case Opcodes.F2L => F2L (methodDeclaration, pc, index)
                    case Opcodes.F2D => F2D (methodDeclaration, pc, index)
                    case Opcodes.D2I => D2I (methodDeclaration, pc, index)
                    case Opcodes.D2L => D2L (methodDeclaration, pc, index)
                    case Opcodes.D2F => D2F (methodDeclaration, pc, index)
                    case Opcodes.I2B => I2B (methodDeclaration, pc, index)
                    case Opcodes.I2C => I2C (methodDeclaration, pc, index)
                    case Opcodes.I2S => I2S (methodDeclaration, pc, index)

                    case Opcodes.LCMP => LCMP (methodDeclaration, pc, index)
                    case Opcodes.FCMPL => FCMPL (methodDeclaration, pc, index)
                    case Opcodes.FCMPG => FCMPG (methodDeclaration, pc, index)
                    case Opcodes.DCMPL => DCMPL (methodDeclaration, pc, index)
                    case Opcodes.DCMPG => DCMPG (methodDeclaration, pc, index)

                    case Opcodes.IRETURN => IRETURN (methodDeclaration, pc, index)
                    case Opcodes.LRETURN => LRETURN (methodDeclaration, pc, index)
                    case Opcodes.FRETURN => FRETURN (methodDeclaration, pc, index)
                    case Opcodes.DRETURN => DRETURN (methodDeclaration, pc, index)
                    case Opcodes.ARETURN => ARETURN (methodDeclaration, pc, index)
                    case Opcodes.RETURN => RETURN (methodDeclaration, pc, index)
                    case Opcodes.ARRAYLENGTH => ARRAYLENGTH (methodDeclaration, pc, index)
                    case Opcodes.ATHROW => ATHROW (methodDeclaration, pc, index)

                    case Opcodes.MONITORENTER => MONITORENTER (methodDeclaration, pc, index)
                    case Opcodes.MONITOREXIT => MONITOREXIT (methodDeclaration, pc, index)
                    case _ => throw new IllegalArgumentException
                }

            storeInstruction (instruction)
        }

        override def visitIntInsn (opcode: Int, operand: Int) {
            val instruction =
                (opcode: @switch) match {
                    case Opcodes.BIPUSH => BIPUSH (methodDeclaration, pc, index, operand.toByte)
                    case Opcodes.SIPUSH => SIPUSH (methodDeclaration, pc, index, operand.toShort)
                    case Opcodes.NEWARRAY => {
                        val elementType =
                            (operand: @switch) match {
                                case Opcodes.T_BOOLEAN => Type.BOOLEAN_TYPE
                                case Opcodes.T_CHAR => Type.CHAR_TYPE
                                case Opcodes.T_FLOAT => Type.FLOAT_TYPE
                                case Opcodes.T_DOUBLE => Type.DOUBLE_TYPE
                                case Opcodes.T_BYTE => Type.BYTE_TYPE
                                case Opcodes.T_SHORT => Type.SHORT_TYPE
                                case Opcodes.T_INT => Type.INT_TYPE
                                case Opcodes.T_LONG => Type.LONG_TYPE
                                case _ => throw new IllegalArgumentException
                            }
                        val arrayType = ArrayType (elementType, 1)

                        NEWARRAY (methodDeclaration, pc, index, elementType, arrayType)
                    }
                    case _ => throw new IllegalArgumentException
                }

            storeInstruction (instruction)
        }

        override def visitJumpInsn (opcode: Int, label: Label) {
            val offset = label.asInstanceOf[LabelExt].originalOffset
            val instruction =
                (opcode: @switch) match {
                    case Opcodes.IFEQ => IFEQ (methodDeclaration, pc, index, offset)
                    case Opcodes.IFNE => IFNE (methodDeclaration, pc, index, offset)
                    case Opcodes.IFLT => IFLT (methodDeclaration, pc, index, offset)
                    case Opcodes.IFGE => IFGE (methodDeclaration, pc, index, offset)
                    case Opcodes.IFGT => IFGT (methodDeclaration, pc, index, offset)
                    case Opcodes.IFLE => IFLE (methodDeclaration, pc, index, offset)
                    case Opcodes.IF_ICMPEQ => IF_ICMPEQ (methodDeclaration, pc, index, offset)
                    case Opcodes.IF_ICMPNE => IF_ICMPNE (methodDeclaration, pc, index, offset)
                    case Opcodes.IF_ICMPLT => IF_ICMPLT (methodDeclaration, pc, index, offset)
                    case Opcodes.IF_ICMPGE => IF_ICMPGE (methodDeclaration, pc, index, offset)
                    case Opcodes.IF_ICMPGT => IF_ICMPGT (methodDeclaration, pc, index, offset)
                    case Opcodes.IF_ICMPLE => IF_ICMPLE (methodDeclaration, pc, index, offset)
                    case Opcodes.IF_ACMPEQ => IF_ACMPEQ (methodDeclaration, pc, index, offset)
                    case Opcodes.IF_ACMPNE => IF_ACMPNE (methodDeclaration, pc, index, offset)
                    case Opcodes.GOTO => GOTO (methodDeclaration, pc, index, offset)
                    case Opcodes.JSR => JSR (methodDeclaration, pc, index, offset)
                    case Opcodes.IFNULL => IFNULL (methodDeclaration, pc, index, offset)
                    case Opcodes.IFNONNULL => IFNONNULL (methodDeclaration, pc, index, offset)
                }

            storeInstruction (instruction)
        }

        override def visitLdcInsn (cst: scala.Any) {
            storeInstruction (
                LDC (methodDeclaration, pc, index, cst)
            )
        }

        override def visitLookupSwitchInsn (dflt: Label, keys: Array[Int], labels: Array[Label]) {
            val defaultOffset = dflt.asInstanceOf[LabelExt].originalOffset
            val offsets = labels.map (_.asInstanceOf[LabelExt].originalOffset)
            storeInstruction (
                LOOKUPSWITCH (methodDeclaration, pc, index, keys, defaultOffset, offsets)
            )
        }


        override def visitMethodInsn (opcode: Int, owner: String, name: String, desc: String) {
            val declaringType = Type.getType (owner)
            val returnType = Type.getReturnType (desc)
            val parameterTypes = Type.getArgumentTypes (desc)
            val methodInfo = MethodInfo (
                declaringType,
                name,
                returnType,
                parameterTypes
            )
            val instruction =
                (opcode: @switch) match {
                    case Opcodes.INVOKEVIRTUAL => INVOKEVIRTUAL (methodDeclaration, pc, index, methodInfo)
                    case Opcodes.INVOKESPECIAL => INVOKESPECIAL (methodDeclaration, pc, index, methodInfo)
                    case Opcodes.INVOKESTATIC => INVOKESTATIC (methodDeclaration, pc, index, methodInfo)
                    case Opcodes.INVOKEINTERFACE => INVOKEINTERFACE (methodDeclaration, pc, index, methodInfo)
                    case _ => throw new IllegalArgumentException
                }

            storeInstruction (instruction)
        }

        override def visitMultiANewArrayInsn (desc: String, dims: Int) {
            val elementType = Type.getType (desc)
            val arrayType = ArrayType (elementType, dims)
            storeInstruction (
                MULTIANEWARRAY (
                    methodDeclaration,
                    pc,
                    index,
                    elementType,
                    arrayType,
                    dims
                )
            )
        }

        override def visitTableSwitchInsn (min: Int, max: Int, dflt: Label, labels: Label*) {
            val defaultOffset = dflt.asInstanceOf[LabelExt].originalOffset
            val offsets = labels.map (_.asInstanceOf[LabelExt].originalOffset)
            storeInstruction (
                TABLESWITCH (methodDeclaration, pc, index, max, min, defaultOffset, offsets)
            )
        }

        override def visitTypeInsn (opcode: Int, `type`: String) {
            val objectType = Type.getObjectType (`type`)
            val instruction =
                (opcode: @switch) match {
                    case Opcodes.NEW => NEW (methodDeclaration, pc, index, objectType)
                    case Opcodes.ANEWARRAY => ANEWARRAY (methodDeclaration, pc, index, objectType,
                        ArrayType (objectType, 1))
                    case Opcodes.CHECKCAST => CHECKCAST (methodDeclaration, pc, index, objectType)
                    case Opcodes.INSTANCEOF => INSTANCEOF (methodDeclaration, pc, index, objectType)
                    case _ => throw new IllegalArgumentException
                }


            storeInstruction (instruction)
        }

        override def visitVarInsn (opcode: Int, `var`: Int) {
            val instruction =
                (opcode: @switch) match {
                    case Opcodes.ILOAD => ILOAD (methodDeclaration, pc, index, `var`)
                    case Opcodes.LLOAD => LLOAD (methodDeclaration, pc, index, `var`)
                    case Opcodes.FLOAD => FLOAD (methodDeclaration, pc, index, `var`)
                    case Opcodes.DLOAD => DLOAD (methodDeclaration, pc, index, `var`)
                    case Opcodes.ALOAD => ALOAD (methodDeclaration, pc, index, `var`)
                    case Opcodes.ISTORE => ISTORE (methodDeclaration, pc, index, `var`)
                    case Opcodes.LSTORE => LSTORE (methodDeclaration, pc, index, `var`)
                    case Opcodes.FSTORE => FSTORE (methodDeclaration, pc, index, `var`)
                    case Opcodes.DSTORE => DSTORE (methodDeclaration, pc, index, `var`)
                    case Opcodes.ASTORE => ASTORE (methodDeclaration, pc, index, `var`)
                    case Opcodes.RET => RET (methodDeclaration, pc, index, `var`)
                    case _ => throw new IllegalArgumentException
                }
            storeInstruction (instruction)

        }

        override def visitTryCatchBlock (start: Label, end: Label, handler: Label, `type`: String) {
            super.visitTryCatchBlock (start, end, handler, `type`)
        }
    }


}