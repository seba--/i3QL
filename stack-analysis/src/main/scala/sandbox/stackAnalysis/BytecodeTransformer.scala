package sandbox.stackAnalysis

import de.tud.cs.st.bat.resolved._
import sandbox.dataflowAnalysis.ResultTransformer
import de.tud.cs.st.bat.resolved.GETSTATIC
import de.tud.cs.st.bat.resolved.ConstantLong
import de.tud.cs.st.bat.resolved.ASTORE
import de.tud.cs.st.bat.resolved.LOOKUPSWITCH
import de.tud.cs.st.bat.resolved.INVOKESTATIC
import de.tud.cs.st.bat.resolved.INVOKEINTERFACE
import de.tud.cs.st.bat.resolved.ALOAD
import de.tud.cs.st.bat.resolved.LDC
import de.tud.cs.st.bat.resolved.IFLE
import de.tud.cs.st.bat.resolved.JSR
import de.tud.cs.st.bat.resolved.IF_ACMPEQ
import de.tud.cs.st.bat.resolved.IF_ICMPNE
import de.tud.cs.st.bat.resolved.ISTORE
import de.tud.cs.st.bat.resolved.ConstantInteger
import de.tud.cs.st.bat.resolved.LSTORE
import de.tud.cs.st.bat.resolved.IF_ICMPLT
import de.tud.cs.st.bat.resolved.GOTO
import de.tud.cs.st.bat.resolved.LLOAD
import de.tud.cs.st.bat.resolved.IFLT
import de.tud.cs.st.bat.resolved.INVOKEVIRTUAL
import de.tud.cs.st.bat.resolved.IF_ICMPGE
import de.tud.cs.st.bat.resolved.GOTO_W
import de.tud.cs.st.bat.resolved.IF_ICMPEQ
import de.tud.cs.st.bat.resolved.IFNONNULL
import de.tud.cs.st.bat.resolved.ConstantString
import de.tud.cs.st.bat.resolved.FLOAD
import de.tud.cs.st.bat.resolved.FSTORE
import de.tud.cs.st.bat.resolved.IFNULL
import de.tud.cs.st.bat.resolved.LDC_W
import de.tud.cs.st.bat.resolved.IINC
import de.tud.cs.st.bat.resolved.NEW
import de.tud.cs.st.bat.resolved.IF_ICMPGT
import de.tud.cs.st.bat.resolved.PUTFIELD
import de.tud.cs.st.bat.resolved.INSTANCEOF
import de.tud.cs.st.bat.resolved.IFNE
import de.tud.cs.st.bat.resolved.DLOAD
import de.tud.cs.st.bat.resolved.TABLESWITCH
import de.tud.cs.st.bat.resolved.MULTIANEWARRAY
import de.tud.cs.st.bat.resolved.GETFIELD
import de.tud.cs.st.bat.resolved.ConstantFloat
import de.tud.cs.st.bat.resolved.IFGE
import de.tud.cs.st.bat.resolved.IFEQ
import de.tud.cs.st.bat.resolved.INVOKESPECIAL
import de.tud.cs.st.bat.resolved.JSR_W
import de.tud.cs.st.bat.resolved.IFGT
import de.tud.cs.st.bat.resolved.INVOKEDYNAMIC
import de.tud.cs.st.bat.resolved.IF_ICMPLE
import de.tud.cs.st.bat.resolved.ANEWARRAY
import de.tud.cs.st.bat.resolved.DSTORE
import de.tud.cs.st.bat.resolved.SIPUSH
import de.tud.cs.st.bat.resolved.ConstantDouble
import de.tud.cs.st.bat.resolved.ILOAD
import de.tud.cs.st.bat.resolved.RET
import de.tud.cs.st.bat.resolved.NEWARRAY
import de.tud.cs.st.bat.resolved.PUTSTATIC
import de.tud.cs.st.bat.resolved.ConstantClass
import de.tud.cs.st.bat.resolved.LDC2_W
import de.tud.cs.st.bat.resolved.BIPUSH
import de.tud.cs.st.bat.resolved.IF_ACMPNE
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
case object BytecodeTransformer extends ResultTransformer[State] {

  def getTransformer(pc: Int, instr: Instruction): Transformer = {

    instr match {

      case NOP => //0
        (p => p)

      case ACONST_NULL => //1
        (p => State(p.s.push(Item.createNullItem(pc)), p.l))

      case ICONST_M1 =>
        (p => State(p.s.push(Item.createItem(ItemType.fromType(IntegerType), pc, -1)), p.l))
      case ICONST_0 =>
        (p => State(p.s.push(Item.createItem(ItemType.fromType(IntegerType), pc, 0)), p.l))
      case ICONST_1 =>
        (p => State(p.s.push(Item.createItem(ItemType.fromType(IntegerType), pc, 1)), p.l))
      case ICONST_2 =>
        (p => State(p.s.push(Item.createItem(ItemType.fromType(IntegerType), pc, 2)), p.l))
      case ICONST_3 =>
        (p => State(p.s.push(Item.createItem(ItemType.fromType(IntegerType), pc, 3)), p.l))
      case ICONST_4 =>
        (p => State(p.s.push(Item.createItem(ItemType.fromType(IntegerType), pc, 4)), p.l))
      case ICONST_5 =>
        (p => State(p.s.push(Item.createItem(ItemType.fromType(IntegerType), pc, 5)), p.l))

      case LCONST_0 => //9,10
        (p => State(p.s.push(Item.createItem(ItemType.fromType(LongType), pc, 0)), p.l))
      case LCONST_1 => //9,10
        (p => State(p.s.push(Item.createItem(ItemType.fromType(LongType), pc, 1)), p.l))

      case FCONST_0 => //11, 12, 13
        (p => State(p.s.push(Item.createItem(ItemType.fromType(FloatType), pc, 0)), p.l))
      case FCONST_1 => //11, 12, 13
        (p => State(p.s.push(Item.createItem(ItemType.fromType(FloatType), pc, 1)), p.l))
      case FCONST_2 => //11, 12, 13
        (p => State(p.s.push(Item.createItem(ItemType.fromType(FloatType), pc, 2)), p.l))

      case DCONST_0 => //14, 15
        (p => State(p.s.push(Item.createItem(ItemType.fromType(DoubleType), pc, 0)), p.l))
      case DCONST_1 => //14, 15
        (p => State(p.s.push(Item.createItem(ItemType.fromType(DoubleType), pc, 1)), p.l))

      case BIPUSH(x) => //16
        (p => State(p.s.push(Item.createItem(ItemType.fromType(ByteType), pc, x)), p.l))

      case SIPUSH(x) => //17
        (p => State(p.s.push(Item.createItem(ItemType.fromType(ShortType), pc, x)), p.l))

      case LDC(const) => //18
        matchLDCConstant(pc, const)

      case LDC_W(const) => //19
        matchLDCConstant(pc, const)


      case LDC2_W(const) => const match {
        //20
        case ConstantLong(x) =>
          (p => State(p.s.push(Item.createItem(ItemType.fromType(LongType), pc, x)), p.l))
        case ConstantDouble(x) =>
          (p => State(p.s.push(Item.createItem(ItemType.fromType(DoubleType), pc, x)), p.l))
        case _ => {
          System.err.println("LDC2_W: must be type double or long. Found: " + const)
          (p => p)
        }
      }

      case ILOAD(x) => //21
        (p => State(p.s.push(p.l.varStore(x)), p.l))

      case LLOAD(x) => //21
        (p => State(p.s.push(p.l.varStore(x)), p.l))

      case FLOAD(x) => //23
        (p => State(p.s.push(p.l.varStore(x)), p.l))

      case DLOAD(x) => //24
        (p => State(p.s.push(p.l.varStore(x)), p.l))

      case ALOAD(x) => //25
        (p => State(p.s.push(p.l.varStore(x)), p.l))

      case ALOAD_0 | ILOAD_0 | LLOAD_0 | FLOAD_0 | DLOAD_0 => //42
        (p => State(p.s.push(p.l.varStore(0)), p.l))
      case ALOAD_1 | ILOAD_1 | LLOAD_1 | FLOAD_1 | DLOAD_1 => //42
        (p => State(p.s.push(p.l.varStore(1)), p.l))
      case ALOAD_2 | ILOAD_2 | LLOAD_2 | FLOAD_2 | DLOAD_2 => //42
        (p => State(p.s.push(p.l.varStore(2)), p.l))
      case ALOAD_3 | ILOAD_3 | LLOAD_3 | FLOAD_3 | DLOAD_3 => //42
        (p => State(p.s.push(p.l.varStore(3)), p.l))

      case LALOAD => //47
        (p => State(p.s.pop().pop().push(LongType, pc), p.l))

      case IALOAD => //48
        (p => State(p.s.pop().pop().push(IntegerType, pc), p.l))

      case FALOAD => //48
        (p => State(p.s.pop().pop().push(FloatType, pc), p.l))

      case DALOAD => //49
        (p => State(p.s.pop().pop().push(DoubleType, pc), p.l))

      case AALOAD => //50
        (p => State(p.s.pop().pop().push(ObjectType.Object, pc), p.l))

      case BALOAD => //51
        (p => State(p.s.pop().pop().push(ByteType, pc), p.l))

      case CALOAD => //51
        (p => State(p.s.pop().pop().push(CharType, pc), p.l))

      case SALOAD => //53
        (p => State(p.s.pop().pop().push(ShortType, pc), p.l))

      case ISTORE(x) => //54
        (p => State(p.s.pop(), p.l.setVar(x, Item.combine(p.s.head))))

      case LSTORE(x) => //55
        (p => State(p.s.pop(), p.l.setVar(x, Item.combine(p.s.head))))

      case FSTORE(x) => //56
        (p => State(p.s.pop(), p.l.setVar(x, Item.combine(p.s.head))))

      case DSTORE(x) => //57
        (p => State(p.s.pop(), p.l.setVar(x, Item.combine(p.s.head))))

      case ASTORE(x) => //58
        (p => State(p.s.pop(), p.l.setVar(x, Item.combine(p.s.head))))

      case ISTORE_0 | LSTORE_0 | FSTORE_0 | DSTORE_0 | ASTORE_0 => //59
        (p => State(p.s.pop(), p.l.setVar(0, Item.combine(p.s.head))))
      case ISTORE_1 | LSTORE_1 | FSTORE_1 | DSTORE_1 | ASTORE_1 =>
        (p => State(p.s.pop(), p.l.setVar(1, Item.combine(p.s.head))))
      case ISTORE_2 | LSTORE_2 | FSTORE_2 | DSTORE_2 | ASTORE_2 =>
        (p => State(p.s.pop(), p.l.setVar(2, Item.combine(p.s.head))))
      case ISTORE_3 | LSTORE_3 | FSTORE_3 | DSTORE_3 | ASTORE_3 =>
        (p => State(p.s.pop(), p.l.setVar(3, Item.combine(p.s.head))))

      case x =>
        computeTransformer2(pc, x)
    }
  }

  private def computeTransformer2(pc: Int, instr: Instruction): Transformer = {
    instr match {

      case IASTORE | LASTORE | FASTORE | DASTORE | AASTORE | BASTORE | CASTORE | SASTORE => //79, 80, 81, 82, 83, 84, 85
        (p => State(p.s.pop().pop().pop(), p.l))

      case POP => //87
        (p => State(p.s.pop(1), p.l))

      case POP2 => //88
        (p => State(p.s.pop(2), p.l))

      case DUP => //89
        (p => State(p.s.dup(1, 0), p.l))

      case DUP_X1 => //90
        (p => State(p.s.dup(1, 1), p.l))

      case DUP_X2 => //91
        (p => State(p.s.dup(1, 2), p.l))

      case DUP2 => //92
        (p => State(p.s.dup(2, 0), p.l))

      case DUP2_X1 => //93
        (p => State(p.s.dup(2, 1), p.l))

      case DUP2_X2 => //94
        (p => State(p.s.dup(2, 2), p.l))

      case SWAP => //95
        (p => State(p.s.swap(), p.l))

      case IADD | ISUB | IDIV | IMUL | IREM | ISHL | ISHR |
           IUSHR | IAND | IOR | IXOR => //96, 100, 108, 112, 120, 122, 124, 126, 128, 130
        (p => State(p.s.pop().pop().push(IntegerType, pc), p.l))

      case LADD | LSUB | LMUL | LDIV | LREM | LSHL | LSHR | LUSHR | LAND | LOR | LXOR => //97, 101, 105, 109, 121, 125, 123, 127, 129, 131
        (p => State(p.s.pop().pop().push(LongType, pc), p.l))

      case FADD | FDIV | FMUL | FREM | FSUB => //98, 110 ,106,114, 102
        (p => State(p.s.pop().pop().push(FloatType, pc), p.l))

      case DADD | DDIV | DMUL | DREM | DSUB => //99, 111, 107, 115, 103
        (p => State(p.s.pop().pop().push(DoubleType, pc), p.l))


      case INEG | LNEG | FNEG | DNEG => //116, 118, 119
        (p => p)

      case IINC(i, _) => //132
        (p => State(p.s.pop(), p.l.setVar(i, IntegerType, pc)))

      case I2L => //133
        (p => State(p.s.pop().push(LongType, pc), p.l))
      case I2F => //134
        (p => State(p.s.pop().push(FloatType, pc), p.l))
      case I2D => //135
        (p => State(p.s.pop().push(DoubleType, pc), p.l))

      case L2I => //136
        (p => State(p.s.pop().push(IntegerType, pc), p.l))
      case L2F => //137
        (p => State(p.s.pop().push(FloatType, pc), p.l))
      case L2D => //138
        (p => State(p.s.pop().push(DoubleType, pc), p.l))

      case F2I => //139
        (p => State(p.s.pop().push(IntegerType, pc), p.l))
      case F2L => //140
        (p => State(p.s.pop().push(LongType, pc), p.l))
      case F2D => //141
        (p => State(p.s.pop().push(DoubleType, pc), p.l))

      case D2I => //142
        (p => State(p.s.pop().push(IntegerType, pc), p.l))
      case D2L => //143
        (p => State(p.s.pop().push(LongType, pc), p.l))
      case D2F => //144
        (p => State(p.s.pop().push(FloatType, pc), p.l))

      case I2B => //145
        (p => State(p.s.pop().push(ByteType, pc), p.l))
      case I2C => //146
        (p => State(p.s.pop().push(CharType, pc), p.l))
      case I2S => //147
        (p => State(p.s.pop().push(ShortType, pc), p.l))


      case x =>
        computeTransformer3(pc, x)

    }

  }

  private def computeTransformer3(pc: Int, instr: Instruction): Transformer = {
    instr match {
      case LCMP | FCMPG | FCMPL | DCMPG | DCMPL => //148,149,150,151,152
        (p => State(p.s.pop().pop().push(IntegerType, pc), p.l))

      case IFEQ(_) | IFNE(_) | IFLT(_) | IFGE(_) | IFGT(_) | IFLE(_) =>
        (p => State(p.s.pop(), p.l))

      case IF_ICMPEQ(_) | IF_ICMPNE(_) | IF_ICMPLT(_) | IF_ICMPGE(_) |
           IF_ICMPGT(_) | IF_ICMPLE(_) | IF_ACMPEQ(_) | IF_ACMPNE(_) => //153,154,155,156,157,158,159,160,161,162,163,164,165,166
        (p => State(p.s.pop().pop(), p.l))

      case GOTO(_) | GOTO_W(_) => //167, 200
        (p => p)

      case JSR(_) | JSR_W(_) => {
        //168 //TODO: Better push return address
        System.err.println("Instructions JSR and JSR_W are not supported.")
        (p => State(p.s.push(ObjectType.Object, pc), p.l))
      }

      case RET(_) => {
        //169
        System.err.println("Instruction RET is not supported.")
        (p => p)
      }

      case TABLESWITCH(_, _, _, _) | LOOKUPSWITCH(_, _, _) => //170,171
        (p => State(p.s.pop(), p.l))

      case IRETURN | LRETURN | ARETURN | FRETURN | DRETURN | RETURN => //172, 173, 174 ,175
        (p => State(Stacks(p.s.maxSize, Nil).addStack(), p.l))

      case GETSTATIC(_, _, t) => //178
        (p => State(p.s.push(t, pc), p.l))

      case PUTSTATIC(_, _, _) => //179
        (p => State(p.s.pop(), p.l))

      case GETFIELD(_, _, t) => //180
        (p => State(p.s.pop().push(t, pc), p.l))

      case PUTFIELD(_, _, _) => //181
        (p => State(p.s.pop().pop(), p.l))

      case INVOKEVIRTUAL(c, name, method) => //182
        invokeTransformer(pc, method, false)
      case INVOKESPECIAL(_, _, method) => //183
        invokeTransformer(pc, method, false)
      case INVOKESTATIC(_, _, method) => //184
        invokeTransformer(pc, method, true)
      case INVOKEINTERFACE(_, _, method) => //185
        invokeTransformer(pc, method, false)
      case INVOKEDYNAMIC(_, method) => //186
        invokeTransformer(pc, method, false)

      case NEW(t) => //187
        (p => State(p.s.push(Item(t, pc, Item.FLAG_IS_CREATED_BY_NEW)), p.l))

      case NEWARRAY(aType) => //188
        aType match {
          case 4 =>
            (p => State(p.s.pop().push(Item(ArrayType(BooleanType), pc, Item.FLAG_IS_CREATED_BY_NEW)), p.l))
          case 5 =>
            (p => State(p.s.pop().push(Item(ArrayType(CharType), pc, Item.FLAG_IS_CREATED_BY_NEW)), p.l))
          case 6 =>
            (p => State(p.s.pop().push(Item(ArrayType(FloatType), pc, Item.FLAG_IS_CREATED_BY_NEW)), p.l))
          case 7 =>
            (p => State(p.s.pop().push(Item(ArrayType(DoubleType), pc, Item.FLAG_IS_CREATED_BY_NEW)), p.l))
          case 8 =>
            (p => State(p.s.pop().push(Item(ArrayType(ByteType), pc, Item.FLAG_IS_CREATED_BY_NEW)), p.l))
          case 9 =>
            (p => State(p.s.pop().push(Item(ArrayType(ShortType), pc, Item.FLAG_IS_CREATED_BY_NEW)), p.l))
          case 10 =>
            (p => State(p.s.pop().push(Item(ArrayType(IntegerType), pc, Item.FLAG_IS_CREATED_BY_NEW)), p.l))
          case 11 =>
            (p => State(p.s.pop().push(Item(ArrayType(LongType), pc, Item.FLAG_IS_CREATED_BY_NEW)), p.l))
          case _ => {
            System.err.println(aType + ": Arraytype not supported by NEWARRAY.")
            (p => p)
          }
        }

      case ANEWARRAY(t) => //189
        (p => State(p.s.pop().push(ArrayType(t), pc), p.l))

      case ARRAYLENGTH => //190
        (p => State(p.s.pop().push(IntegerType, pc), p.l))

      case ATHROW => //191 //TODO: implement
        (p => p)

      case CHECKCAST(_) => //192
        (p => p)

      case INSTANCEOF(_) => //193
        (p => State(p.s.pop().push(IntegerType, pc), p.l))

      case MONITORENTER | MONITOREXIT => //194, 195
        (p => State(p.s.pop(), p.l))

      case WIDE => //196
        (p => p)

      case MULTIANEWARRAY(t, dim) => //197
        (p => {
          var s = p.s
          for (i <- 1 to dim)
            s = p.s.pop()
          State(s.push(ArrayType(dim, t), pc), p.l)
        })

      case IFNULL(_) | IFNONNULL(_) => //199
        (p => State(p.s.pop(), p.l))

      case x => {
        System.err.println("Instruction is not supported: " + x.mnemonic)
        (p => p)
      }

    }
  }

  private def invokeTransformer(pc: Int, method: MethodDescriptor, isStatic: Boolean): (State => State) = {
    (p => {
      var stack = p.s
      for (i <- (if (isStatic) 1 else 0) to method.parameterTypes.size) //use of to: one need to pop the declaring class from the stack
        stack = stack.pop()

      if (!method.returnType.isVoidType)
        stack = stack.push(Item(ItemType.fromType(method.returnType), pc, Item.FLAG_IS_RETURN_VALUE))

      State(stack, p.l)
    })
  }

  private def matchLDCConstant(pc: Int, const: ConstantValue[_]): State => State = {
    const match {

      case ConstantString(x) =>
        (p => State(p.s.push(Item.createItem(ItemType.fromType(ObjectType.String), pc, x)), p.l))
      case ConstantInteger(x) =>
        (p => State(p.s.push(Item.createItem(ItemType.fromType(IntegerType), pc, x)), p.l))
      case ConstantFloat(x) =>
        (p => State(p.s.push(Item.createItem(ItemType.fromType(FloatType), pc, x)), p.l))
      case ConstantClass(x) =>
        (p => State(p.s.push(Item.createItem(ItemType.fromType(ObjectType.Class), pc, x)), p.l))
      case _ => {
        System.err.println("LDC_W: must be type string, integer, float or class. Found: " + const)
        (p => p)
      }
    }
  }
}
