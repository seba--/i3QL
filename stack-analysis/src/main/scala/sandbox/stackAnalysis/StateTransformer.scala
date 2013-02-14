package sandbox.stackAnalysis

import de.tud.cs.st.bat.resolved._
import sandbox.dataflowAnalysis.ResultTransformer
import de.tud.cs.st.bat.resolved.GETSTATIC
import de.tud.cs.st.bat.resolved.ConstantLong
import de.tud.cs.st.bat.resolved.ASTORE
import de.tud.cs.st.bat.resolved.ALOAD
import de.tud.cs.st.bat.resolved.LDC
import de.tud.cs.st.bat.resolved.ISTORE
import de.tud.cs.st.bat.resolved.LSTORE
import de.tud.cs.st.bat.resolved.LLOAD
import de.tud.cs.st.bat.resolved.FLOAD
import de.tud.cs.st.bat.resolved.FSTORE
import de.tud.cs.st.bat.resolved.LDC_W
import de.tud.cs.st.bat.resolved.IINC
import de.tud.cs.st.bat.resolved.DLOAD
import de.tud.cs.st.bat.resolved.DSTORE
import de.tud.cs.st.bat.resolved.ConstantDouble
import de.tud.cs.st.bat.resolved.ILOAD
import de.tud.cs.st.bat.resolved.LDC2_W
import de.tud.cs.st.bat.resolved.BIPUSH
import sandbox.stackAnalysis.datastructure.{Stacks, ItemType, Item, State}


/**
 *
 *
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 01.11.12
 * Time: 15:05
 * To change this template use File | Settings | File Templates.
 */
case object StateTransformer extends ResultTransformer[State] {

  def getTransformer(pc: Int, instr: Instruction): Transformer = {

    instr.opcode match {

      case 0x00 => //NOP
        (p => p)

      case 0x01 => //ACONST_NULL
        (p => State(p.stacks.push(Item.createNullItem(pc)), p.variables))

      case 0x02 => //ICONST_M1
        (p => State(p.stacks.push(Item.createItem( pc, ItemType.fromType(IntegerType), -1)), p.variables))
      case 0x03 => //ICONST_0
        (p => State(p.stacks.push(Item.createItem(pc,ItemType.fromType(IntegerType),  0)), p.variables))
      case 0x04 => //ICONST_1
        (p => State(p.stacks.push(Item.createItem(pc,ItemType.fromType(IntegerType),  1)), p.variables))
      case 0x05 => //ICONST_2
        (p => State(p.stacks.push(Item.createItem(pc,ItemType.fromType(IntegerType),  2)), p.variables))
      case 0x06 => //ICONST_3
        (p => State(p.stacks.push(Item.createItem(pc,ItemType.fromType(IntegerType),  3)), p.variables))
      case 0x07 => //ICONST_4
        (p => State(p.stacks.push(Item.createItem(pc,ItemType.fromType(IntegerType),  4)), p.variables))
      case 0x08 => //ICONST_5
        (p => State(p.stacks.push(Item.createItem(pc,ItemType.fromType(IntegerType),  5)), p.variables))

      case 0x09 => // LCONST_0
        (p => State(p.stacks.push(Item.createItem(pc,ItemType.fromType(LongType), 0)), p.variables))
      case 0x0a => //LCONST_1
        (p => State(p.stacks.push(Item.createItem(pc,ItemType.fromType(LongType), 1)), p.variables))

      case 0x0b => //FCONST_0
        (p => State(p.stacks.push(Item.createItem(pc,ItemType.fromType(FloatType), 0)), p.variables))
      case 0x0c => //FCONST_1
        (p => State(p.stacks.push(Item.createItem(pc,ItemType.fromType(FloatType), 1)), p.variables))
      case 0x0d => //FCONST_2
        (p => State(p.stacks.push(Item.createItem(pc,ItemType.fromType(FloatType), 2)), p.variables))

      case 0x0e => //DCONST_0
        (p => State(p.stacks.push(Item.createItem(pc,ItemType.fromType(DoubleType), 0)), p.variables))
      case 0x0f => //DCONST_1
        (p => State(p.stacks.push(Item.createItem(pc,ItemType.fromType(DoubleType), 1)), p.variables))

      case 0x10 => {
        //BIPUSH(x)
        val i = instr.asInstanceOf[BIPUSH]
        (p => State(p.stacks.push(Item.createItem(pc,ItemType.fromType(ByteType), i.value)), p.variables))
      }
      case 0x11 => {
        //SIPUSH(x)
        val i = instr.asInstanceOf[SIPUSH]
        (p => State(p.stacks.push(Item.createItem(pc,ItemType.fromType(ShortType), i.value)), p.variables))
      }

      case 0x12 => //LDC(const)
        matchLDCConstant(pc, instr.asInstanceOf[LDC].constantValue)

      case 0x13 => //LDC_W(const)
        matchLDCConstant(pc, instr.asInstanceOf[LDC_W].constantValue)


      case 0x14 => //LDC2_W(const)
        instr.asInstanceOf[LDC2_W].constantValue match {

          case ConstantLong(x) =>
            (p => State(p.stacks.push(Item.createItem(pc, ItemType.fromType(LongType), x)), p.variables))
          case ConstantDouble(x) =>
            (p => State(p.stacks.push(Item.createItem(pc, ItemType.fromType(DoubleType), x)), p.variables))
          case _ => {
            System.err.println("LDC2_W: must be type double or long.")
            (p => p)
          }
        }

      case 0x15 => //ILOAD(x)
        (p => State(p.stacks.push(p.variables.varStore(instr.asInstanceOf[ILOAD].lvIndex)), p.variables))
      case 0x16 => //LLOAD(x)
        (p => State(p.stacks.push(p.variables.varStore(instr.asInstanceOf[LLOAD].lvIndex)), p.variables))
      case 0x17 => //FLOAD(x)
        (p => State(p.stacks.push(p.variables.varStore(instr.asInstanceOf[FLOAD].lvIndex)), p.variables))
      case 0x18 => //DLOAD(x)
        (p => State(p.stacks.push(p.variables.varStore(instr.asInstanceOf[DLOAD].lvIndex)), p.variables))
      case 0x19 => //ALOAD(x)
        (p => State(p.stacks.push(p.variables.varStore(instr.asInstanceOf[ALOAD].lvIndex)), p.variables))

      case 0x1a | 0x1e | 0x22 | 0x26 | 0x2a => //xLOAD_0
        (p => State(p.stacks.push(p.variables.varStore(0)), p.variables))
      case 0x1b | 0x1f | 0x23 | 0x27 | 0x2b => //xLOAD_1
        (p => State(p.stacks.push(p.variables.varStore(1)), p.variables))
      case 0x1c | 0x20 | 0x24 | 0x28 | 0x2c => //xLOAD_2
        (p => State(p.stacks.push(p.variables.varStore(2)), p.variables))
      case 0x1d | 0x21 | 0x25 | 0x29 | 0x2d => //xLOAD_3
        (p => State(p.stacks.push(p.variables.varStore(3)), p.variables))


      case 0x2e => //IALOAD
        (p => State(p.stacks.pop().pop().push(IntegerType, pc), p.variables))

      case 0x2f => //LALOAD
        (p => State(p.stacks.pop().pop().push(LongType, pc), p.variables))

      case 0x30 => //FALOAD => //48
        (p => State(p.stacks.pop().pop().push(FloatType, pc), p.variables))

      case 0x31 => //DALOAD => //49
        (p => State(p.stacks.pop().pop().push(DoubleType, pc), p.variables))

      case 0x32 => //AALOAD => //50
        (p => State(p.stacks.pop().pop().push(ObjectType.Object, pc), p.variables))

      case 0x33 => //BALOAD
        (p => State(p.stacks.pop().pop().push(ByteType, pc), p.variables))

      case 0x34 => //CALOAD
        (p => State(p.stacks.pop().pop().push(CharType, pc), p.variables))

      case 0x35 => //SALOAD => //53
        (p => State(p.stacks.pop().pop().push(ShortType, pc), p.variables))

      case 0x36 => //ISTORE(x) => //54
        (p => State(p.stacks.pop(), p.variables.setVar(instr.asInstanceOf[ISTORE].lvIndex, Item.upperBound(p.stacks.head))))

      case 0x37 => //LSTORE(x) => //55
        (p => State(p.stacks.pop(), p.variables.setVar(instr.asInstanceOf[LSTORE].lvIndex, Item.upperBound(p.stacks.head))))

      case 0x38 => //FSTORE(x) => //56
        (p => State(p.stacks.pop(), p.variables.setVar(instr.asInstanceOf[FSTORE].lvIndex, Item.upperBound(p.stacks.head))))

      case 0x39 => //DSTORE(x) => //57
        (p => State(p.stacks.pop(), p.variables.setVar(instr.asInstanceOf[DSTORE].lvIndex, Item.upperBound(p.stacks.head))))

      case 0x3a => //ASTORE(x) => //58
        (p => State(p.stacks.pop(), p.variables.setVar(instr.asInstanceOf[ASTORE].lvIndex, Item.upperBound(p.stacks.head))))

      case 0x3b | 0x3f | 0x43 | 0x47 | 0x4b => //xSTORE_0
        (p => State(p.stacks.pop(), p.variables.setVar(0, Item.upperBound(p.stacks.head))))
      case 0x3c | 0x40 | 0x44 | 0x48 | 0x4c => //xSTORE_1
        (p => State(p.stacks.pop(), p.variables.setVar(1, Item.upperBound(p.stacks.head))))
      case 0x3d | 0x41 | 0x45 | 0x49 | 0x4d => //xSTORE_2
        (p => State(p.stacks.pop(), p.variables.setVar(2, Item.upperBound(p.stacks.head))))
      case 0x3e | 0x42 | 0x46 | 0x4a | 0x4e => //xSTORE_3
        (p => State(p.stacks.pop(), p.variables.setVar(3, Item.upperBound(p.stacks.head))))

      case _ =>
        computeTransformer2(pc, instr)
    }
  }

  private def computeTransformer2(pc: Int, instr: Instruction): Transformer = {
    instr.opcode match {

      case 0x4f | 0x50 | 0x51 | 0x52 | 0x53 | 0x54 | 0x55 | 0x56 => //IASTORE | LASTORE | FASTORE | DASTORE | AASTORE | BASTORE | CASTORE | SASTORE => //79, 80, 81, 82, 83, 84, 85
        (p => State(p.stacks.pop().pop().pop(), p.variables))

      case 0x57 => //POP => //87
        (p => State(p.stacks.pop(1), p.variables))

      case 0x58 => //POP2 => //88
        (p => State(p.stacks.pop(2), p.variables))

      case 0x59 => //DUP => //89
        (p => State(p.stacks.dup(1, 0), p.variables))

      case 0x5a => //DUP_X1 => //90
        (p => State(p.stacks.dup(1, 1), p.variables))

      case 0x5b => //DUP_X2 => //91
        (p => State(p.stacks.dup(1, 2), p.variables))

      case 0x5c => //DUP2 => //92
        (p => State(p.stacks.dup(2, 0), p.variables))

      case 0x5d => //DUP2_X1 => //93
        (p => State(p.stacks.dup(2, 1), p.variables))

      case 0x5e => //DUP2_X2 => //94
        (p => State(p.stacks.dup(2, 2), p.variables))

      case 0x5f => //SWAP => //95
        (p => State(p.stacks.swap(), p.variables))

      case 0x60 | 0x64 | 0x68 | 0x6c | 0x70 | 0x78 | 0x7a | 0x7c | 0x7e | 0x80 | 0x82 => //IADD | ISUB | IDIV | IMUL | IREM | ISHL | ISHR |
        //IUSHR | IAND | IOR | IXOR => //96, 100, 108, 112, 120, 122, 124, 126, 128, 130
        (p => State(p.stacks.pop().pop().push(IntegerType, pc), p.variables))

      case 0x61 | 0x65 | 0x69 | 0x6d | 0x71 | 0x79 | 0x7b | 0x7d | 0x7f | 0x81 | 0x83 => //LADD | LSUB | LMUL | LDIV | LREM | LSHL | LSHR | LUSHR | LAND | LOR | LXOR => //97, 101, 105, 109, 121, 125, 123, 127, 129, 131
        (p => State(p.stacks.pop().pop().push(LongType, pc), p.variables))

      case 0x62 | 0x66 | 0x6a | 0x6e | 0x72 => //FADD | FDIV | FMUL | FREM | FSUB => //98, 110 ,106,114, 102
        (p => State(p.stacks.pop().pop().push(FloatType, pc), p.variables))

      case 0x63 | 0x67 | 0x6b | 0x6f | 0x73 => //DADD | DDIV | DMUL | DREM | DSUB => //99, 111, 107, 115, 103
        (p => State(p.stacks.pop().pop().push(DoubleType, pc), p.variables))


      case 0x74 | 0x75 | 0x76 | 0x77 => //INEG | LNEG | FNEG | DNEG => //116, 118, 119
        (p => p)

      case 0x84 => //IINC(i, _) => //132
        (p => State(p.stacks, p.variables.setVar(instr.asInstanceOf[IINC].lvIndex, IntegerType, pc)))

      case 0x85 => //I2L => //133
        (p => State(p.stacks.pop().push(LongType, pc), p.variables))
      case 0x86 => //I2F => //134
        (p => State(p.stacks.pop().push(FloatType, pc), p.variables))
      case 0x87 => //I2D => //135
        (p => State(p.stacks.pop().push(DoubleType, pc), p.variables))

      case 0x88 => //L2I => //136
        (p => State(p.stacks.pop().push(IntegerType, pc), p.variables))
      case 0x89 => //L2F => //137
        (p => State(p.stacks.pop().push(FloatType, pc), p.variables))
      case 0x8a => //L2D => //138
        (p => State(p.stacks.pop().push(DoubleType, pc), p.variables))

      case 0x8b => //F2I => //139
        (p => State(p.stacks.pop().push(IntegerType, pc), p.variables))
      case 0x8c => //F2L => //140
        (p => State(p.stacks.pop().push(LongType, pc), p.variables))
      case 0x8d => //F2D => //141
        (p => State(p.stacks.pop().push(DoubleType, pc), p.variables))

      case 0x8e => //D2I => //142
        (p => State(p.stacks.pop().push(IntegerType, pc), p.variables))
      case 0x8f => //D2L => //143
        (p => State(p.stacks.pop().push(LongType, pc), p.variables))
      case 0x90 => //D2F => //144
        (p => State(p.stacks.pop().push(FloatType, pc), p.variables))

      case 0x91 => //I2B => //145
        (p => State(p.stacks.pop().push(ByteType, pc), p.variables))
      case 0x92 => //I2C => //146
        (p => State(p.stacks.pop().push(CharType, pc), p.variables))
      case 0x93 => //I2S => //147
        (p => State(p.stacks.pop().push(ShortType, pc), p.variables))


      case _ =>
        computeTransformer3(pc, instr)

    }

  }

  private def computeTransformer3(pc: Int, instr: Instruction): Transformer = {
    instr.opcode match {
      case 0x94 | 0x95 | 0x96 | 0x97 | 0x98 => //LCMP | FCMPG | FCMPL | DCMPG | DCMPL => //148,149,150,151,152
        (p => State(p.stacks.pop().pop().push(IntegerType, pc), p.variables))

      case 0x99 | 0x9a | 0x9b | 0x9c | 0x9d | 0x9e => //IFEQ(_) | IFNE(_) | IFLT(_) | IFGE(_) | IFGT(_) | IFLE(_) =>
        (p => State(p.stacks.pop(), p.variables))

      case 0x9f | 0xa0 | 0xa1 | 0xa2 | 0xa3 | 0xa4 | 0xa5 | 0xa6 => //IF_ICMPEQ(_) | IF_ICMPNE(_) | IF_ICMPLT(_) | IF_ICMPGE(_) |
        //IF_ICMPGT(_) | IF_ICMPLE(_) | IF_ACMPEQ(_) | IF_ACMPNE(_) => //153,154,155,156,157,158,159,160,161,162,163,164,165,166
        (p => State(p.stacks.pop().pop(), p.variables))

      case 0xa7 | 0xc8 => //GOTO(_) | GOTO_W(_) //167, 200
        (p => p)

      case 0xa8 | 0xc9 => {
        //JSR(_) | JSR_W(_) //168
        System.err.println("Instructions JSR and JSR_W are not supported.")
        (p => State(p.stacks.push(ObjectType.Object, pc), p.variables))
      }

      case 0xa9 => {
        //RET(_) //169
        System.err.println("Instruction RET is not supported.")
        (p => p)
      }

      case 0xaa | 0xab => //TABLESWITCH(_, _, _, _) | LOOKUPSWITCH(_, _, _) => //170,171
        (p => State(p.stacks.pop(), p.variables))

      case 0xac | 0xad | 0xae | 0xaf | 0xb0 | 0xb1 => //IRETURN | LRETURN | ARETURN | FRETURN | DRETURN | RETURN => //172, 173, 174 ,175
        (p => State(Stacks(p.stacks.maxStack, Nil).addStack(), p.variables))

      case 0xb2 => {
        //GETSTATIC(_, name, t) //178
        val i = instr.asInstanceOf[GETSTATIC]
        (p => State(p.stacks.push(new Item(pc, ItemType.fromType(i.fieldType), Item.FLAG_ORIGINATES_FROM_FIELD, i.name)), p.variables))
      }
      case 0xb3 => //PUTSTATIC(_, _, _) => //179
        (p => State(p.stacks.pop(), p.variables))

      case 0xb4 => {
        //GETFIELD(_, name, t) => //180
        val i = instr.asInstanceOf[GETFIELD]
        (p => State(p.stacks.pop().push(new Item(pc, ItemType.fromType(i.fieldType), Item.FLAG_ORIGINATES_FROM_FIELD, i.name)), p.variables))
      }

      case 0xb5 => //PUTFIELD(_, _, _) => //181
        (p => State(p.stacks.pop().pop(), p.variables))

      case 0xb6 => //INVOKEVIRTUAL(c, name, method) => //182
        invokeTransformer(pc, instr.asInstanceOf[INVOKEVIRTUAL].methodDescriptor, false)
      case 0xb7 => //INVOKESPECIAL(_, _, method) => //183
        invokeTransformer(pc, instr.asInstanceOf[INVOKESPECIAL].methodDescriptor, false)
      case 0xb8 => //INVOKESTATIC(_, _, method) => //184
        invokeTransformer(pc, instr.asInstanceOf[INVOKESTATIC].methodDescriptor, true)
      case 0xb9 => //INVOKEINTERFACE(_, _, method) => //185
        invokeTransformer(pc, instr.asInstanceOf[INVOKEINTERFACE].methodDescriptor, false)
      case 0xba => //INVOKEDYNAMIC(_, method) => //186
        invokeTransformer(pc, instr.asInstanceOf[INVOKEDYNAMIC].methodDescriptor, false)

      case 0xbb => //NEW(t) => //187
        (p => State(p.stacks.push(Item( pc, instr.asInstanceOf[NEW].objectType, Item.FLAG_IS_CREATED_BY_NEW)), p.variables))

      case 0xbc => //NEWARRAY(aType) => //188
        instr.asInstanceOf[NEWARRAY].atype match {
          case 4 =>
            (p => State(p.stacks.pop().push(Item(pc, ArrayType(BooleanType),  Item.FLAG_IS_CREATED_BY_NEW)), p.variables))
          case 5 =>
            (p => State(p.stacks.pop().push(Item(pc, ArrayType(CharType),  Item.FLAG_IS_CREATED_BY_NEW)), p.variables))
          case 6 =>
            (p => State(p.stacks.pop().push(Item(pc, ArrayType(FloatType),  Item.FLAG_IS_CREATED_BY_NEW)), p.variables))
          case 7 =>
            (p => State(p.stacks.pop().push(Item(pc, ArrayType(DoubleType),  Item.FLAG_IS_CREATED_BY_NEW)), p.variables))
          case 8 =>
            (p => State(p.stacks.pop().push(Item(pc, ArrayType(ByteType),  Item.FLAG_IS_CREATED_BY_NEW)), p.variables))
          case 9 =>
            (p => State(p.stacks.pop().push(Item(pc, ArrayType(ShortType),  Item.FLAG_IS_CREATED_BY_NEW)), p.variables))
          case 10 =>
            (p => State(p.stacks.pop().push(Item(pc, ArrayType(IntegerType),  Item.FLAG_IS_CREATED_BY_NEW)), p.variables))
          case 11 =>
            (p => State(p.stacks.pop().push(Item(pc, ArrayType(LongType),  Item.FLAG_IS_CREATED_BY_NEW)), p.variables))
          case x => {
            System.err.println(x + ": Arraytype not supported by NEWARRAY.")
            (p => p)
          }
        }

      case 0xbd => //ANEWARRAY(t) => //189
        (p => State(p.stacks.pop().push(ArrayType(instr.asInstanceOf[ANEWARRAY].componentType), pc), p.variables))

      case 0xbe => //ARRAYLENGTH => //190
        (p => State(p.stacks.pop().push(IntegerType, pc), p.variables))

      case 0xbf => //ATHROW => //191 //TODO: implement
        (p => p)

      case 0xc0 => //CHECKCAST(_) => //192
        (p => p)

      case 0xc1 => //INSTANCEOF(_) => //193
        (p => State(p.stacks.pop().push(IntegerType, pc), p.variables))

      case 0xc2 | 0xc3 => //MONITORENTER | MONITOREXIT => //194, 195
        (p => State(p.stacks.pop(), p.variables))

      case 0xc4 => //WIDE => //196
        (p => p)

      case 0xc5 => {
        // MULTIANEWARRAY(t, dim) => //197
        val i = instr.asInstanceOf[MULTIANEWARRAY]
        (p => {
          var s = p.stacks
          for (i <- 1 to i.dimensions)
            s = s.pop()
          State(s.push(ArrayType(i.dimensions, i.componentType), pc), p.variables)
        })
      }

      case 0xc6 | 0xc7 => //IFNULL(_) | IFNONNULL(_) => //199
        (p => State(p.stacks.pop(), p.variables))

      case _ => {
        System.err.println("Instruction is not supported: " + instr.mnemonic)
        (p => p)
      }

    }
  }

  private def invokeTransformer(pc: Int, method: MethodDescriptor, isStatic: Boolean): (State => State) = {
    (p => {
      var stack = p.stacks
      for (i <- (if (isStatic) 1 else 0) to method.parameterTypes.size) //use of to: one need to pop the declaring class from the stack
        stack = stack.pop()

      if (!method.returnType.isVoidType)
        stack = stack.push(Item( pc, ItemType.fromType(method.returnType), Item.FLAG_IS_RETURN_VALUE))

      State(stack, p.variables)
    })
  }

  private def matchLDCConstant(pc: Int, const: ConstantValue[_]): State => State = {
    const match {

      case ConstantString(x) =>
        (p => State(p.stacks.push(Item.createItem(pc, ItemType.fromType(ObjectType.String), x)), p.variables))
      case ConstantInteger(x) =>
        (p => State(p.stacks.push(Item.createItem(pc, ItemType.fromType(IntegerType), x)), p.variables))
      case ConstantFloat(x) =>
        (p => State(p.stacks.push(Item.createItem(pc, ItemType.fromType(FloatType), x)), p.variables))
      case ConstantClass(x) =>
        (p => State(p.stacks.push(Item.createItem(pc, ItemType.fromType(ObjectType.Class), x)), p.variables))
      case _ => {
        System.err.println("LDC_W: must be type string, integer, float or class. Found: " + const)
        (p => p)
      }
    }
  }
}
