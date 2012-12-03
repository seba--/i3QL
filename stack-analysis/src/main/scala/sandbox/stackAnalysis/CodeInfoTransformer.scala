package sandbox.stackAnalysis


import de.tud.cs.st.bat.resolved._
import sandbox.dataflowAnalysis.{MethodTransformer, ResultTransformer}
import sae.Relation
import sae.bytecode.structure.CodeInfo
import sae.syntax.sql.SELECT
import sandbox.stackAnalysis.TypeOption.NullType

/**
 *
 *
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 01.11.12
 * Time: 15:05
 * To change this template use File | Settings | File Templates.
 */
case class CodeInfoTransformer(codeInfo: Relation[CodeInfo]) extends ResultTransformer[Configuration] {

  val result: Relation[MethodTransformer[Configuration]] = {
    sae.syntax.sql.compile(SELECT((c: CodeInfo) => MethodTransformer(c.declaringMethod, computeFunctions(c.code.instructions, c))) FROM codeInfo)
  }

  private def computeFunctions(a: Array[Instruction], ci: CodeInfo): Array[Transformer] = {
    val res = Array.fill[Transformer](a.length)(null) //((s,l) => (s,l))

    var currentPC = 0

    while (currentPC < a.length && currentPC != -1) {
      res(currentPC) = computeTransformer(currentPC, a(currentPC))

      //TODO: change when bug fixed
      val savedPC = currentPC
      currentPC = a(currentPC).indexOfNextInstruction(currentPC, ci.code)
      if (currentPC < a.length && a(currentPC) == null) {
        currentPC = CodeInfoTools.getNextPC(a, savedPC)
      }
      //TODO: change when bug fixed

    }

    return res
  }

  private def computeTransformer(pc: Int, instr: Instruction): Transformer = {

    instr match {

      case NOP => //0
        (p => p)

      case ACONST_NULL => //1
        (p => Configuration(p.s.push(NullType(pc)), p.l))

      case ICONST_M1 | ICONST_0 | ICONST_1 | ICONST_2 | ICONST_3 | ICONST_4 | ICONST_5 => //2,3,4,5,6,7,8
        (p => Configuration(p.s.push(IntegerType, pc), p.l))

      case LCONST_0 | LCONST_1 => //9,10
        (p => Configuration(p.s.push(LongType, pc), p.l))

      case FCONST_0 | FCONST_1 | FCONST_2 => //11, 12, 13
        (p => Configuration(p.s.push(FloatType, pc), p.l))

      case DCONST_0 | DCONST_1 => //14, 15
        (p => Configuration(p.s.push(DoubleType, pc), p.l))

      case BIPUSH(_) => //16
        (p => Configuration(p.s.push(ByteType, pc), p.l))

      case SIPUSH(_) => //17
        (p => Configuration(p.s.push(ShortType, pc), p.l))

      case LDC(const) => //18
        const match {

          case ConstantString(_) =>
            (p => Configuration(p.s.push(ObjectType.String, pc), p.l))
          case ConstantInteger(_) =>
            (p => Configuration(p.s.push(IntegerType, pc), p.l))
          case ConstantFloat(_) =>
            (p => Configuration(p.s.push(FloatType, pc), p.l))
          case ConstantClass(_) =>
            (p => Configuration(p.s.push(ObjectType.Class, pc), p.l))
          case _ =>
             {
              System.err.println("LDC: must be type string, integer, float or class. Found: " + const)
               (p => p)
            }
        }

      case LDC_W(const) => //19 //TODO: Merge with instruction LDC
        const match {

          case ConstantString(_) =>
            (p => Configuration(p.s.push(ObjectType.String, pc), p.l))
          case ConstantInteger(_) =>
            (p => Configuration(p.s.push(IntegerType, pc), p.l))
          case ConstantFloat(_) =>
            (p => Configuration(p.s.push(FloatType, pc), p.l))
          case ConstantClass(_) =>
            (p => Configuration(p.s.push(ObjectType.Class, pc), p.l))
          case _ =>
             {
              System.err.println("LDC_W: must be type string, integer, float or class. Found: " + const)
               (p => p)
            }
        }

      case LDC2_W(const) => const match {
        //20
        case ConstantLong(_) =>
          (p => Configuration(p.s.push(LongType, pc), p.l))
        case ConstantDouble(_) =>
          (p => Configuration(p.s.push(DoubleType, pc), p.l))
        case _ =>
          {
            System.err.println("LDC2_W: must be type double or long. Found: " + const)
            (p => p)
          }
      }

      case ILOAD(x) => //21 //TODO: load pc from variables instead of new pc
        (p => Configuration(p.s.push(IntegerType, pc), p.l))

      case LLOAD(_) => //21
        (p => Configuration(p.s.push(LongType, pc), p.l))

      case FLOAD(_) => //23
        (p => Configuration(p.s.push(FloatType, pc), p.l))

      case DLOAD(_) => //24
        (p => Configuration(p.s.push(DoubleType, pc), p.l))

      case ALOAD(x) => //25
        (p => Configuration(p.s.push(p.l.varStore(x).declaredType.convertPC(pc)), p.l))

      case ILOAD_0 | ILOAD_1 | ILOAD_2 | ILOAD_3 => //26,27,28,29
        (p => Configuration(p.s.push(IntegerType, pc), p.l))

      case LLOAD_0 | LLOAD_1 | LLOAD_2 | LLOAD_3 => //30,31,32,33
        (p => Configuration(p.s.push(LongType, pc), p.l))

      case FLOAD_0 | FLOAD_1 | FLOAD_2 | FLOAD_3 => //34,35,36,37
        (p => Configuration(p.s.push(FloatType, pc), p.l))

      case DLOAD_0 | DLOAD_1 | DLOAD_2 | DLOAD_3 => //38,39,40,41
        (p => Configuration(p.s.push(DoubleType, pc), p.l))

      case ALOAD_0 => //42
        (p => Configuration(p.s.push(p.l.varStore(0).declaredType.convertPC(pc)), p.l))
      case ALOAD_1 => //43
        (p => Configuration(p.s.push(p.l.varStore(1).declaredType.convertPC(pc)), p.l))
      case ALOAD_2 => //44
        (p => Configuration(p.s.push(p.l.varStore(2).declaredType.convertPC(pc)), p.l))
      case ALOAD_3 => //45
        (p => Configuration(p.s.push(p.l.varStore(3).declaredType.convertPC(pc)), p.l))

      case LALOAD => //47
        (p => Configuration(p.s.pop().pop().push(LongType, pc), p.l))

      case IALOAD => //48
        (p => Configuration(p.s.pop().pop().push(IntegerType, pc), p.l))

      case FALOAD => //48
        (p => Configuration(p.s.pop().pop().push(FloatType, pc), p.l))

      case DALOAD => //49
        (p => Configuration(p.s.pop().pop().push(DoubleType, pc), p.l))

      case AALOAD => //50
        (p => Configuration(p.s.pop().pop().push(ObjectType.Object, pc), p.l))

      case BALOAD => //51
        (p => Configuration(p.s.pop().pop().push(ByteType, pc), p.l))

      case CALOAD => //51
        (p => Configuration(p.s.pop().pop().push(CharType, pc), p.l))

      case SALOAD => //53
        (p => Configuration(p.s.pop().pop().push(ShortType, pc), p.l))

      case ISTORE(x) => //54
        (p => Configuration(p.s.pop(), p.l.setVar(x, IntegerType, pc)))

      case LSTORE(x) => //55
        (p => Configuration(p.s.pop(), p.l.setVar(x, LongType, pc)))

      case FSTORE(x) => //56
        (p => Configuration(p.s.pop(), p.l.setVar(x, FloatType, pc)))

      case DSTORE(x) => //57
        (p => Configuration(p.s.pop(), p.l.setVar(x, DoubleType, pc)))

      case ASTORE(x) => //58
        (p => Configuration(p.s.pop(), p.l.setVar(x, p.s.declaredTypeOf(0).convertPC(pc))))

      case ISTORE_0 => //59
        (p => Configuration(p.s.pop(), p.l.setVar(0, IntegerType, pc)))
      case ISTORE_1 => //60
        (p => Configuration(p.s.pop(), p.l.setVar(1, IntegerType, pc)))
      case ISTORE_2 => //61
        (p => Configuration(p.s.pop(), p.l.setVar(2, IntegerType, pc)))
      case ISTORE_3 => //62
        (p => Configuration(p.s.pop(), p.l.setVar(3, IntegerType, pc)))

      case LSTORE_0 => //63
        (p => Configuration(p.s.pop(), p.l.setVar(0, LongType, pc)))
      case LSTORE_1 => //64
        (p => Configuration(p.s.pop(), p.l.setVar(1, LongType, pc)))
      case LSTORE_2 => //65
        (p => Configuration(p.s.pop(), p.l.setVar(2, LongType, pc)))
      case LSTORE_3 => //66
        (p => Configuration(p.s.pop(), p.l.setVar(3, LongType, pc)))

      case FSTORE_0 => //67
        (p => Configuration(p.s.pop(), p.l.setVar(0, FloatType, pc)))
      case FSTORE_1 => //68
        (p => Configuration(p.s.pop(), p.l.setVar(1, FloatType, pc)))
      case FSTORE_2 => //69
        (p => Configuration(p.s.pop(), p.l.setVar(2, FloatType, pc)))
      case FSTORE_3 => //70
        (p => Configuration(p.s.pop(), p.l.setVar(3, FloatType, pc)))

      case DSTORE_0 => //71
        (p => Configuration(p.s.pop(), p.l.setVar(0, DoubleType, pc)))
      case DSTORE_1 => //72
        (p => Configuration(p.s.pop(), p.l.setVar(1, DoubleType, pc)))
      case DSTORE_2 => //73
        (p => Configuration(p.s.pop(), p.l.setVar(2, DoubleType, pc)))
      case DSTORE_3 => //74
        (p => Configuration(p.s.pop(), p.l.setVar(3, DoubleType, pc)))

      case ASTORE_0 => //75
        (p => Configuration(p.s.pop(), p.l.setVar(0, p.s.declaredTypeOf(0).convertPC(pc))))
      case ASTORE_1 => //76
        (p => Configuration(p.s.pop(), p.l.setVar(1, p.s.declaredTypeOf(0).convertPC(pc))))
      case ASTORE_2 => //77
        (p => Configuration(p.s.pop(), p.l.setVar(2, p.s.declaredTypeOf(0).convertPC(pc))))
      case ASTORE_3 => //78
        (p => Configuration(p.s.pop(), p.l.setVar(3, p.s.declaredTypeOf(0).convertPC(pc))))


      case x =>
        computeTransformer2(pc, x)
    }
  }

  private def computeTransformer2(pc: Int, instr: Instruction): Transformer = {
    instr match {

      case IASTORE | LASTORE | FASTORE | DASTORE | AASTORE | BASTORE | CASTORE | SASTORE => //79, 80, 81, 82, 83, 84, 85
        (p => Configuration(p.s.pop().pop().pop(), p.l))

      case POP => //87
        (p => Configuration(p.s.pop(1), p.l))

      case POP2 => //88
        (p => Configuration(p.s.pop(2), p.l))

      case DUP => //89 //TODO:DUP duplicating pc or setting new pc?
        (p => Configuration(p.s.dup(1, 0), p.l))

      case DUP_X1 => //90
        (p => Configuration(p.s.dup(1, 1), p.l))

      case DUP_X2 => //91
        (p => Configuration(p.s.dup(1, 2), p.l))

      case DUP2 => //92
        (p => Configuration(p.s.dup(2, 0), p.l))

      case DUP2_X1 => //93
        (p => Configuration(p.s.dup(2, 1), p.l))

      case DUP2_X2 => //94
        (p => Configuration(p.s.dup(2, 2), p.l))

      case SWAP => //95
        (p => Configuration(p.s.swap(), p.l))

      case IADD | ISUB | IDIV | IMUL | IREM | ISHL | ISHR |
           IUSHR | IAND | IOR | IXOR => //96, 100, 108, 112, 120, 122, 124, 126, 128, 130
        (p => Configuration(p.s.pop().pop().push(IntegerType, pc), p.l))

      case LADD | LSUB | LMUL | LDIV | LREM | LSHL | LSHR | LUSHR | LAND | LOR | LXOR => //97, 101, 105, 109, 121, 125, 123, 127, 129, 131
        (p => Configuration(p.s.pop().pop().push(LongType, pc), p.l))

      case FADD | FDIV | FMUL | FREM | FSUB => //98, 110 ,106,114, 102
        (p => Configuration(p.s.pop().pop().push(FloatType, pc), p.l))

      case DADD | DDIV | DMUL | DREM | DSUB => //99, 111, 107, 115, 103
        (p => Configuration(p.s.pop().pop().push(DoubleType, pc), p.l))


      case INEG | LNEG | FNEG | DNEG => //116, 118, 119
        (p => p)

      case IINC(i, _) => //132
        (p => Configuration(p.s.pop(), p.l.setVar(i, IntegerType, pc)))

      case I2L => //133
        (p => Configuration(p.s.pop().push(LongType, pc), p.l))
      case I2F => //134
        (p => Configuration(p.s.pop().push(FloatType, pc), p.l))
      case I2D => //135
        (p => Configuration(p.s.pop().push(DoubleType, pc), p.l))

      case L2I => //136
        (p => Configuration(p.s.pop().push(IntegerType, pc), p.l))
      case L2F => //137
        (p => Configuration(p.s.pop().push(FloatType, pc), p.l))
      case L2D => //138
        (p => Configuration(p.s.pop().push(DoubleType, pc), p.l))

      case F2I => //139
        (p => Configuration(p.s.pop().push(IntegerType, pc), p.l))
      case F2L => //140
        (p => Configuration(p.s.pop().push(LongType, pc), p.l))
      case F2D => //141
        (p => Configuration(p.s.pop().push(DoubleType, pc), p.l))

      case D2I => //142
        (p => Configuration(p.s.pop().push(IntegerType, pc), p.l))
      case D2L => //143
        (p => Configuration(p.s.pop().push(LongType, pc), p.l))
      case D2F => //144
        (p => Configuration(p.s.pop().push(FloatType, pc), p.l))

      case I2B => //145
        (p => Configuration(p.s.pop().push(ByteType, pc), p.l))
      case I2C => //146
        (p => Configuration(p.s.pop().push(CharType, pc), p.l))
      case I2S => //147
        (p => Configuration(p.s.pop().push(ShortType, pc), p.l))


      case x =>
        computeTransformer3(pc, x)

    }

  }

  private def computeTransformer3(pc: Int, instr: Instruction): Transformer = {
    instr match {
      case LCMP | FCMPG | FCMPL | DCMPG | DCMPL => //148,149,150,151,152
        (p => Configuration(p.s.pop().pop().push(IntegerType, pc), p.l))

      case IFEQ(_) | IFNE(_) | IFLT(_) | IFGE(_) | IFGT(_) | IFLE(_) |
           IF_ICMPEQ(_) | IF_ICMPNE(_) | IF_ICMPLT(_) | IF_ICMPGE(_) |
           IF_ICMPGT(_) | IF_ICMPLE(_) | IF_ACMPEQ(_) | IF_ACMPNE(_) => //153,154,155,156,157,158,159,160,161,162,163,164,165,166
        (p => Configuration(p.s.pop().pop(), p.l))

      case GOTO(_) | GOTO_W(_) => //167, 200
        (p => p)

      case JSR(_) | JSR_W(_) => { //168 //TODO: Better push return address
        System.err.println("Instructions JSR and JSR_W are not supported.")
        (p => Configuration(p.s.push(ObjectType.Object, pc), p.l))
      }

      case RET(_) => { //169
        System.err.println("Instruction RET is not supported.")
        (p => p)
      }

      case TABLESWITCH(_, _, _, _) | LOOKUPSWITCH(_, _, _) => //170,171
        (p => Configuration(p.s.pop(), p.l))

      case IRETURN | LRETURN | ARETURN | FRETURN | DRETURN | RETURN => //172, 173, 174 ,175
        (p => Configuration(Stacks(p.s.maxSize, Nil).addStack(), p.l))

      case GETSTATIC(_, _, t) => //178
        (p => Configuration(p.s.push(t, pc), p.l))

      case PUTSTATIC(_, _, _) => //179
        (p => Configuration(p.s.pop(), p.l))

      case GETFIELD(_, _, t) => //180
        (p => Configuration(p.s.pop().push(t, pc), p.l))

      case PUTFIELD(_, _, _) => //181
        (p => Configuration(p.s.pop().pop(), p.l))

      case INVOKEVIRTUAL(c, name, method) =>  //182
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
        (p => Configuration(p.s.push(t, pc), p.l))

      case NEWARRAY(aType) => //188
        aType match {
          case 4 =>
            (p => Configuration(p.s.pop().push(ArrayType(BooleanType), pc), p.l))
          case 5 =>
            (p => Configuration(p.s.pop().push(ArrayType(CharType), pc), p.l))
          case 6 =>
            (p => Configuration(p.s.pop().push(ArrayType(FloatType), pc), p.l))
          case 7 =>
            (p => Configuration(p.s.pop().push(ArrayType(DoubleType), pc), p.l))
          case 8 =>
            (p => Configuration(p.s.pop().push(ArrayType(ByteType), pc), p.l))
          case 9 =>
            (p => Configuration(p.s.pop().push(ArrayType(ShortType), pc), p.l))
          case 10 =>
            (p => Configuration(p.s.pop().push(ArrayType(IntegerType), pc), p.l))
          case 11 =>
            (p => Configuration(p.s.pop().push(ArrayType(LongType), pc), p.l))
          case _ => {
            System.err.println(aType + ": Arraytype not supported by NEWARRAY.")
            (p => p)
          }
        }

      case ANEWARRAY(t) => //189
        (p => Configuration(p.s.pop().push(ArrayType(t), pc), p.l))

      case ARRAYLENGTH => //190
        (p => Configuration(p.s.pop().push(IntegerType, pc), p.l))

      case ATHROW => //191 //TODO: implement
        (p => p)

      case CHECKCAST(_) => //192
        (p => p)

      case INSTANCEOF(_) => //193
        (p => Configuration(p.s.pop().push(IntegerType, pc), p.l))

      case MONITORENTER | MONITOREXIT => //194, 195
        (p => Configuration(p.s.pop(), p.l))

      case WIDE => //196
        (p => p)

      case MULTIANEWARRAY(t, dim) => //197
        (p => {
          var s = p.s
          for (i <- 1 to dim)
            s = p.s.pop()
          Configuration(s.push(ArrayType(dim, t), pc), p.l)
        })

      case IFNULL(_) | IFNONNULL(_) => //199
        (p => Configuration(p.s.pop(), p.l))

      case x => {
        System.err.println("Instruction is not supported: " + x.mnemonic)
        (p => p)
      }

    }
  }

  private def invokeTransformer(pc: Int, method: MethodDescriptor, isStatic: Boolean): (Configuration => Configuration) = {
    (p => {
      var stack = p.s
      for (i <- (if (isStatic) 1 else 0) to method.parameterTypes.size) //use of to: one need to pop the declaring class from the stack
        stack = stack.pop()

      if (!method.returnType.isVoidType)
        stack = stack.push(method.returnType, pc)

      Configuration(stack, p.l)
    })
  }
}
