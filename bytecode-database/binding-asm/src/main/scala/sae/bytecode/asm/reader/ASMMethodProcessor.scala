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

import org.objectweb.asm.{Label, Type, Opcodes, MethodVisitor}
import sae.bytecode.BytecodeDatabase
import sae.bytecode.asm.ASMDatabase
import scala.annotation.switch
import sae.bytecode.asm.instructions.opcodes._
import sae.bytecode.asm.structure._
import sae.bytecode.asm.instructions.{UnconditionalJumpInstruction, BasicInstruction}
import sae.bytecode.asm.ext.LabelExt

/**
 *
 * @author Ralf Mitschke
 */
trait ASMMethodProcessor
{
    val database: ASMDatabase

    import database._

    def processExceptionHandler(h: ExceptionHandler)

    def processCodeAttribute (a: CodeAttribute)

    def processBasicInstruction (i: Instruction)

    def processFieldReadInstruction (i: FieldAccessInstruction)

    def processFieldWriteInstruction (i: FieldAccessInstruction)

    def processUnconditionalJumpInstruction (i: JumpInstruction)

    def processConditionalJumpInstruction (i: JumpInstruction)

    def processConstantValueInstruction[V] (i: ConstantValueInstruction[V])

    def processNewArrayInstruction[V] (i: NewArrayInstruction[V])

    def processLookupSwitchInstruction (i: LOOKUPSWITCH)

    def processTableSwitchInstruction (i: TABLESWITCH)

    def processMethodInvocationInstruction (i: MethodInvocationInstruction)

    def processObjectTypeInstruction (i: ObjectTypeInstruction)

    def processLocalVariableLoadInstructions (i: LocalVariableAccessInstruction)

    def processLocalVariableStoreInstructions (i: LocalVariableAccessInstruction)

    def processIINCInstruction (i: IINC)

    def processRetInstructions (i: RET)

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

        var counter = 0

        override def visitMaxs (maxStack: Int, maxLocals: Int) {
            this.maxStack = maxStack
            this.maxLocals = maxLocals
        }


        /**
         * do computations for pc or other global accumulations.
         */
        protected def accumulate (instruction: Instruction) {
            counter += 1
            pc = instruction.nextPC
        }

        override def visitFieldInsn (opcode: Int, owner: String, name: String, desc: String) {
            val declaringType = Type.getObjectType (owner)
            val fieldType = Type.getType (desc)
            val fieldInfo = FieldReference (
                declaringType,
                name,
                fieldType
            )

            (opcode: @switch) match {
                case Opcodes.GETFIELD => {
                    val instruction = GETFIELD (methodDeclaration, pc, fieldInfo)
                    accumulate (instruction)
                    processFieldReadInstruction (instruction)
                }
                case Opcodes.PUTFIELD => {
                    val instruction = PUTFIELD (methodDeclaration, pc, fieldInfo)
                    accumulate (instruction)
                    processFieldWriteInstruction (instruction)
                }
                case Opcodes.GETSTATIC => {
                    val instruction = GETSTATIC (methodDeclaration, pc, fieldInfo)
                    accumulate (instruction)
                    processFieldReadInstruction (instruction)
                }
                case Opcodes.PUTSTATIC => {
                    val instruction = PUTSTATIC (methodDeclaration, pc, fieldInfo)
                    accumulate (instruction)
                    processFieldWriteInstruction (instruction)
                }
                case _ => throw new IllegalArgumentException
            }

        }

        override def visitIincInsn (`var`: Int, increment: Int) {
            val instruction = IINC (methodDeclaration, pc, `var`, increment)
            accumulate (instruction)
            processIINCInstruction (instruction)
        }

        override def visitInsn (opcode: Int) {
            val instruction: BasicInstruction =
                (opcode: @switch) match {
                    case Opcodes.NOP => NOP (methodDeclaration, pc)
                    case Opcodes.ACONST_NULL => ACONST_NULL (methodDeclaration, pc)

                    case Opcodes.ICONST_M1 => ICONST_M1 (methodDeclaration, pc)
                    case Opcodes.ICONST_0 => ICONST_0 (methodDeclaration, pc)
                    case Opcodes.ICONST_1 => ICONST_1 (methodDeclaration, pc)
                    case Opcodes.ICONST_2 => ICONST_2 (methodDeclaration, pc)
                    case Opcodes.ICONST_3 => ICONST_3 (methodDeclaration, pc)
                    case Opcodes.ICONST_4 => ICONST_4 (methodDeclaration, pc)
                    case Opcodes.ICONST_5 => ICONST_5 (methodDeclaration, pc)

                    case Opcodes.LCONST_0 => LCONST_0 (methodDeclaration, pc)
                    case Opcodes.LCONST_1 => LCONST_1 (methodDeclaration, pc)
                    case Opcodes.FCONST_0 => FCONST_0 (methodDeclaration, pc)
                    case Opcodes.FCONST_1 => FCONST_1 (methodDeclaration, pc)
                    case Opcodes.FCONST_2 => FCONST_2 (methodDeclaration, pc)
                    case Opcodes.DCONST_0 => DCONST_0 (methodDeclaration, pc)
                    case Opcodes.DCONST_1 => DCONST_1 (methodDeclaration, pc)

                    case Opcodes.IALOAD => IALOAD (methodDeclaration, pc)
                    case Opcodes.LALOAD => LALOAD (methodDeclaration, pc)
                    case Opcodes.FALOAD => FALOAD (methodDeclaration, pc)
                    case Opcodes.DALOAD => DALOAD (methodDeclaration, pc)
                    case Opcodes.AALOAD => AALOAD (methodDeclaration, pc)
                    case Opcodes.BALOAD => BALOAD (methodDeclaration, pc)
                    case Opcodes.CALOAD => CALOAD (methodDeclaration, pc)
                    case Opcodes.SALOAD => SALOAD (methodDeclaration, pc)

                    case Opcodes.IASTORE => IASTORE (methodDeclaration, pc)
                    case Opcodes.LASTORE => LASTORE (methodDeclaration, pc)
                    case Opcodes.FASTORE => FASTORE (methodDeclaration, pc)
                    case Opcodes.DASTORE => DASTORE (methodDeclaration, pc)
                    case Opcodes.AASTORE => AASTORE (methodDeclaration, pc)
                    case Opcodes.BASTORE => BASTORE (methodDeclaration, pc)
                    case Opcodes.CASTORE => CASTORE (methodDeclaration, pc)
                    case Opcodes.SASTORE => SASTORE (methodDeclaration, pc)

                    case Opcodes.POP => POP (methodDeclaration, pc)
                    case Opcodes.POP2 => POP2 (methodDeclaration, pc)
                    case Opcodes.DUP => DUP (methodDeclaration, pc)
                    case Opcodes.DUP_X1 => DUP_X1 (methodDeclaration, pc)
                    case Opcodes.DUP_X2 => DUP_X2 (methodDeclaration, pc)
                    case Opcodes.DUP2 => DUP2 (methodDeclaration, pc)
                    case Opcodes.DUP2_X1 => DUP2_X1 (methodDeclaration, pc)
                    case Opcodes.DUP2_X2 => DUP2_X2 (methodDeclaration, pc)
                    case Opcodes.SWAP => SWAP (methodDeclaration, pc)

                    case Opcodes.IADD => IADD (methodDeclaration, pc)
                    case Opcodes.LADD => LADD (methodDeclaration, pc)
                    case Opcodes.FADD => FADD (methodDeclaration, pc)
                    case Opcodes.DADD => DADD (methodDeclaration, pc)
                    case Opcodes.ISUB => ISUB (methodDeclaration, pc)
                    case Opcodes.LSUB => LSUB (methodDeclaration, pc)
                    case Opcodes.FSUB => FSUB (methodDeclaration, pc)
                    case Opcodes.DSUB => DSUB (methodDeclaration, pc)

                    case Opcodes.IMUL => IMUL (methodDeclaration, pc)
                    case Opcodes.LMUL => LMUL (methodDeclaration, pc)
                    case Opcodes.FMUL => FMUL (methodDeclaration, pc)
                    case Opcodes.DMUL => DMUL (methodDeclaration, pc)
                    case Opcodes.IDIV => IDIV (methodDeclaration, pc)
                    case Opcodes.LDIV => LDIV (methodDeclaration, pc)
                    case Opcodes.FDIV => FDIV (methodDeclaration, pc)
                    case Opcodes.DDIV => DDIV (methodDeclaration, pc)

                    case Opcodes.IREM => IREM (methodDeclaration, pc)
                    case Opcodes.LREM => LREM (methodDeclaration, pc)
                    case Opcodes.FREM => FREM (methodDeclaration, pc)
                    case Opcodes.DREM => DREM (methodDeclaration, pc)

                    case Opcodes.INEG => INEG (methodDeclaration, pc)
                    case Opcodes.LNEG => LNEG (methodDeclaration, pc)
                    case Opcodes.FNEG => FNEG (methodDeclaration, pc)
                    case Opcodes.DNEG => DNEG (methodDeclaration, pc)

                    case Opcodes.ISHL => ISHL (methodDeclaration, pc)
                    case Opcodes.LSHL => LSHL (methodDeclaration, pc)
                    case Opcodes.ISHR => ISHR (methodDeclaration, pc)
                    case Opcodes.LSHR => LSHR (methodDeclaration, pc)
                    case Opcodes.IUSHR => IUSHR (methodDeclaration, pc)
                    case Opcodes.LUSHR => LUSHR (methodDeclaration, pc)

                    case Opcodes.IAND => IAND (methodDeclaration, pc)
                    case Opcodes.LAND => LAND (methodDeclaration, pc)
                    case Opcodes.IOR => IOR (methodDeclaration, pc)
                    case Opcodes.LOR => LOR (methodDeclaration, pc)
                    case Opcodes.IXOR => IXOR (methodDeclaration, pc)
                    case Opcodes.LXOR => LXOR (methodDeclaration, pc)

                    case Opcodes.I2L => I2L (methodDeclaration, pc)
                    case Opcodes.I2F => I2F (methodDeclaration, pc)
                    case Opcodes.I2D => I2D (methodDeclaration, pc)
                    case Opcodes.L2I => L2I (methodDeclaration, pc)
                    case Opcodes.L2F => L2F (methodDeclaration, pc)
                    case Opcodes.L2D => L2D (methodDeclaration, pc)
                    case Opcodes.F2I => F2I (methodDeclaration, pc)
                    case Opcodes.F2L => F2L (methodDeclaration, pc)
                    case Opcodes.F2D => F2D (methodDeclaration, pc)
                    case Opcodes.D2I => D2I (methodDeclaration, pc)
                    case Opcodes.D2L => D2L (methodDeclaration, pc)
                    case Opcodes.D2F => D2F (methodDeclaration, pc)
                    case Opcodes.I2B => I2B (methodDeclaration, pc)
                    case Opcodes.I2C => I2C (methodDeclaration, pc)
                    case Opcodes.I2S => I2S (methodDeclaration, pc)

                    case Opcodes.LCMP => LCMP (methodDeclaration, pc)
                    case Opcodes.FCMPL => FCMPL (methodDeclaration, pc)
                    case Opcodes.FCMPG => FCMPG (methodDeclaration, pc)
                    case Opcodes.DCMPL => DCMPL (methodDeclaration, pc)
                    case Opcodes.DCMPG => DCMPG (methodDeclaration, pc)

                    case Opcodes.IRETURN => IRETURN (methodDeclaration, pc)
                    case Opcodes.LRETURN => LRETURN (methodDeclaration, pc)
                    case Opcodes.FRETURN => FRETURN (methodDeclaration, pc)
                    case Opcodes.DRETURN => DRETURN (methodDeclaration, pc)
                    case Opcodes.ARETURN => ARETURN (methodDeclaration, pc)
                    case Opcodes.RETURN => RETURN (methodDeclaration, pc)
                    case Opcodes.ARRAYLENGTH => ARRAYLENGTH (methodDeclaration, pc)
                    case Opcodes.ATHROW => ATHROW (methodDeclaration, pc)

                    case Opcodes.MONITORENTER => MONITORENTER (methodDeclaration, pc)
                    case Opcodes.MONITOREXIT => MONITOREXIT (methodDeclaration, pc)
                    case _ => throw new IllegalArgumentException
                }

            accumulate (instruction)
            processBasicInstruction (instruction)
        }

        override def visitIntInsn (opcode: Int, operand: Int) {
            (opcode: @switch) match {
                case Opcodes.BIPUSH => {
                    val instruction = BIPUSH (methodDeclaration, pc, operand.toByte)
                    accumulate (instruction)
                    processConstantValueInstruction (instruction)
                }
                case Opcodes.SIPUSH => {
                    val instruction = SIPUSH (methodDeclaration, pc, operand.toShort)
                    accumulate (instruction)
                    processConstantValueInstruction (instruction)
                }
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

                    val instruction = NEWARRAY (methodDeclaration, pc, elementType)
                    accumulate (instruction)
                    processNewArrayInstruction (instruction)
                }
                case _ => throw new IllegalArgumentException
            }
        }

        override def visitJumpInsn (opcode: Int, label: Label) {
            val offset = label.asInstanceOf[LabelExt].originalOffset
            val instruction: JumpInstruction =
                (opcode: @switch) match {
                    case Opcodes.IFEQ => IFEQ (methodDeclaration, pc, offset)
                    case Opcodes.IFNE => IFNE (methodDeclaration, pc, offset)
                    case Opcodes.IFLT => IFLT (methodDeclaration, pc, offset)
                    case Opcodes.IFGE => IFGE (methodDeclaration, pc, offset)
                    case Opcodes.IFGT => IFGT (methodDeclaration, pc, offset)
                    case Opcodes.IFLE => IFLE (methodDeclaration, pc, offset)
                    case Opcodes.IF_ICMPEQ => IF_ICMPEQ (methodDeclaration, pc, offset)
                    case Opcodes.IF_ICMPNE => IF_ICMPNE (methodDeclaration, pc, offset)
                    case Opcodes.IF_ICMPLT => IF_ICMPLT (methodDeclaration, pc, offset)
                    case Opcodes.IF_ICMPGE => IF_ICMPGE (methodDeclaration, pc, offset)
                    case Opcodes.IF_ICMPGT => IF_ICMPGT (methodDeclaration, pc, offset)
                    case Opcodes.IF_ICMPLE => IF_ICMPLE (methodDeclaration, pc, offset)
                    case Opcodes.IF_ACMPEQ => IF_ACMPEQ (methodDeclaration, pc, offset)
                    case Opcodes.IF_ACMPNE => IF_ACMPNE (methodDeclaration, pc, offset)
                    case Opcodes.GOTO => GOTO (methodDeclaration, pc, offset)
                    case Opcodes.JSR => JSR (methodDeclaration, pc, offset)
                    case Opcodes.IFNULL => IFNULL (methodDeclaration, pc, offset)
                    case Opcodes.IFNONNULL => IFNONNULL (methodDeclaration, pc, offset)
                }

            accumulate (instruction)
            if (instruction.isInstanceOf[UnconditionalJumpInstruction]) {
                processUnconditionalJumpInstruction (instruction)
            } else
            {
                processConditionalJumpInstruction (instruction)
            }
        }

        override def visitLdcInsn (cst: scala.Any) {
            val instruction = LDC (methodDeclaration, pc, cst)
            accumulate (instruction)
            processConstantValueInstruction (instruction)
        }

        override def visitLookupSwitchInsn (dflt: Label, keys: Array[Int], labels: Array[Label]) {
            val defaultOffset = dflt.asInstanceOf[LabelExt].originalOffset
            val offsets = labels.map (_.asInstanceOf[LabelExt].originalOffset)
            val instruction = LOOKUPSWITCH (methodDeclaration, pc, defaultOffset, keys, offsets)
            accumulate (instruction)
            processLookupSwitchInstruction (instruction)
        }


        override def visitMethodInsn (opcode: Int, owner: String, name: String, desc: String) {
            val declaringType = Type.getObjectType (owner)
            val returnType = Type.getReturnType (desc)
            val parameterTypes = Type.getArgumentTypes (desc)
            val methodInfo = MethodReference (
                declaringType,
                name,
                returnType,
                parameterTypes
            )
            val instruction: MethodInvocationInstruction =
                (opcode: @switch) match {
                    case Opcodes.INVOKEVIRTUAL => INVOKEVIRTUAL (methodDeclaration, pc, methodInfo)
                    case Opcodes.INVOKESPECIAL => INVOKESPECIAL (methodDeclaration, pc, methodInfo)
                    case Opcodes.INVOKESTATIC => INVOKESTATIC (methodDeclaration, pc, methodInfo)
                    case Opcodes.INVOKEINTERFACE => INVOKEINTERFACE (methodDeclaration, pc, methodInfo)
                    case _ => throw new IllegalArgumentException
                }

            accumulate (instruction)
            processMethodInvocationInstruction (instruction)
        }

        override def visitMultiANewArrayInsn (desc: String, dims: Int) {
            val elementType = Type.getType (desc)
            val instruction = MULTIANEWARRAY (
                methodDeclaration,
                pc,
                elementType,
                dims
            )
            accumulate (instruction)
            processNewArrayInstruction (instruction)
        }

        override def visitTableSwitchInsn (min: Int, max: Int, dflt: Label, labels: Label*) {
            val defaultOffset = dflt.asInstanceOf[LabelExt].originalOffset
            val offsets = labels.map (_.asInstanceOf[LabelExt].originalOffset)
            val instruction = TABLESWITCH (methodDeclaration, pc, max, min, defaultOffset, offsets)
            accumulate (instruction)
            processTableSwitchInstruction (instruction)
        }

        override def visitTypeInsn (opcode: Int, `type`: String) {
            val objectType = Type.getObjectType (`type`)
            (opcode: @switch) match {
                case Opcodes.NEW => {
                    val instruction = NEW (methodDeclaration, pc, objectType)
                    accumulate (instruction)
                    processObjectTypeInstruction (instruction)
                }
                case Opcodes.ANEWARRAY => {
                    val instruction = ANEWARRAY (methodDeclaration, pc, objectType)
                    accumulate (instruction)
                    processNewArrayInstruction (instruction)
                }
                case Opcodes.CHECKCAST => {
                    val instruction = CHECKCAST (methodDeclaration, pc, objectType)
                    accumulate (instruction)
                    processObjectTypeInstruction (instruction)
                }
                case Opcodes.INSTANCEOF => {
                    val instruction = INSTANCEOF (methodDeclaration, pc, objectType)
                    accumulate (instruction)
                    processObjectTypeInstruction (instruction)
                }
                case _ => throw new IllegalArgumentException
            }
        }

        override def visitVarInsn (opcode: Int, `var`: Int) {
            (opcode: @switch) match {
                case Opcodes.ILOAD => {
                    val instruction = ILOAD (methodDeclaration, pc, `var`)
                    accumulate (instruction)
                    processLocalVariableLoadInstructions (instruction)
                }
                case Opcodes.LLOAD => {
                    val instruction = LLOAD (methodDeclaration, pc, `var`)
                    accumulate (instruction)
                    processLocalVariableLoadInstructions (instruction)
                }
                case Opcodes.FLOAD => {
                    val instruction = FLOAD (methodDeclaration, pc, `var`)
                    accumulate (instruction)
                    processLocalVariableLoadInstructions (instruction)
                }
                case Opcodes.DLOAD => {
                    val instruction = DLOAD (methodDeclaration, pc, `var`)
                    accumulate (instruction)
                    processLocalVariableLoadInstructions (instruction)
                }
                case Opcodes.ALOAD => {
                    val instruction = ALOAD (methodDeclaration, pc, `var`)
                    accumulate (instruction)
                    processLocalVariableLoadInstructions (instruction)
                }
                case Opcodes.ISTORE => {
                    val instruction = ISTORE (methodDeclaration, pc, `var`)
                    accumulate (instruction)
                    processLocalVariableStoreInstructions (instruction)
                }
                case Opcodes.LSTORE => {
                    val instruction = LSTORE (methodDeclaration, pc, `var`)
                    accumulate (instruction)
                    processLocalVariableStoreInstructions (instruction)
                }
                case Opcodes.FSTORE => {
                    val instruction = FSTORE (methodDeclaration, pc, `var`)
                    accumulate (instruction)
                    processLocalVariableStoreInstructions (instruction)
                }
                case Opcodes.DSTORE => {
                    val instruction = DSTORE (methodDeclaration, pc, `var`)
                    accumulate (instruction)
                    processLocalVariableStoreInstructions (instruction)
                }
                case Opcodes.ASTORE => {
                    val instruction = ASTORE (methodDeclaration, pc, `var`)
                    accumulate (instruction)
                    processLocalVariableStoreInstructions (instruction)
                }
                case Opcodes.RET => {
                    val instruction = RET (methodDeclaration, pc, `var`)
                    accumulate (instruction)
                    processRetInstructions (instruction)
                }
                case _ => throw new IllegalArgumentException
            }
        }

        override def visitTryCatchBlock (start: Label, end: Label, handler: Label, catchTypeName: String) {
            val startPC = start.asInstanceOf[LabelExt].originalOffset
            val endPC = end.asInstanceOf[LabelExt].originalOffset
            val handlerPC = handler.asInstanceOf[LabelExt].originalOffset
            val catchType = if (catchTypeName == null) None else Some (Type.getObjectType (catchTypeName))
            val exceptionHandler = ExceptionHandler (methodDeclaration, catchType, startPC, endPC, handlerPC)
            processExceptionHandler(exceptionHandler)
        }



        override def visitEnd () {
            val codeAttribute = CodeAttribute(methodDeclaration, pc, maxStack, maxLocals)
            processCodeAttribute(codeAttribute)
        }
    }


}
