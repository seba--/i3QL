package sandbox.stackAnalysis


import de.tud.cs.st.bat.resolved._
import sandbox.dataflowAnalysis.{MethodTransformer, ResultTransformer}
import sae.Relation
import Types._
import sae.bytecode.structure.CodeInfo
import sae.syntax.sql.SELECT

/**
 *
 *
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 01.11.12
 * Time: 15:05
 * To change this template use File | Settings | File Templates.
 */
case class CodeInfoTransformer(codeInfo: Relation[CodeInfo]) extends ResultTransformer[StackResult] {

  val result: Relation[MethodTransformer[StackResult]] = {
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

        // println(a(savedPC).mnemonic + " " + currentPC + " - " + ci.declaringMethod)
        currentPC = CodeInfoTools.getNextPC(a, savedPC)
      } else if (a(savedPC).isInstanceOf[TABLESWITCH] || a(savedPC).isInstanceOf[LOOKUPSWITCH]) {
        System.err.println("HIER NICHT!")
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
        (p => Result(p.s.push(1, ObjectType.Object, pc), p.l))

      case ICONST_M1 | ICONST_0 | ICONST_1 | ICONST_2 | ICONST_3 | ICONST_4 | ICONST_5 => //2,3,4,5,6,7,8
        (p => Result(p.s.push(IntegerType, pc), p.l))

      case LCONST_0 | LCONST_1 => //9,10
        (p => Result(p.s.push(2, LongType, pc), p.l))

      case FCONST_0 | FCONST_1 | FCONST_2 => //11, 12, 13
        (p => Result(p.s.push(1, FloatType, pc), p.l))

      case DCONST_0 | DCONST_1 => //14, 15
        (p => Result(p.s.push(2, DoubleType, pc), p.l))

      case BIPUSH(_) => //16
        (p => Result(p.s.push(ByteType, pc), p.l))

      case SIPUSH(_) => //17
        (p => Result(p.s.push(ShortType, pc), p.l))

      case LDC(const) => //18
        const match {

          case ConstantString(_) =>
            (p => Result(p.s.push(ObjectType.String, pc), p.l))
          case ConstantInteger(_) =>
            (p => Result(p.s.push(IntegerType, pc), p.l))
          case ConstantFloat(_) =>
            (p => Result(p.s.push(FloatType, pc), p.l))
          case ConstantClass(_) =>
            (p => Result(p.s.push(ObjectType.Class, pc), p.l))
          case _ =>
            (p => {
              System.err.println("LDC: must be type string, integer, float or class. Found: " + const)
              p
            })
        }

      case LDC_W(const) => //19 //TODO: Merge with instruction LDC
        const match {

          case ConstantString(_) =>
            (p => Result(p.s.push(ObjectType.String, pc), p.l))
          case ConstantInteger(_) =>
            (p => Result(p.s.push(IntegerType, pc), p.l))
          case ConstantFloat(_) =>
            (p => Result(p.s.push(FloatType, pc), p.l))
          case ConstantClass(_) =>
            (p => Result(p.s.push(ObjectType.Class, pc), p.l))
          case _ =>
            (p => {
              System.err.println("LDC: must be type string, integer, float or class. Found: " + const)
              p
            })
        }

      case LDC2_W(const) => const match {
        //20
        case ConstantLong(_) =>
          (p => Result(p.s.push(2, LongType, pc), p.l))
        case ConstantDouble(_) =>
          (p => Result(p.s.push(2, DoubleType, pc), p.l))
        case _ =>
          (p => {
            System.err.println("LDC2_W: must be type double or long. Found: " + const)
            p
          })
      }

      case ILOAD(x) => //21 //TODO: load pc from variables instead of new pc
        (p => Result(p.s.push(IntegerType, pc), p.l))

      case LLOAD(_) => //21
        (p => Result(p.s.push(2, LongType, pc), p.l))

      case FLOAD(_) => //23
        (p => Result(p.s.push(1, FloatType, pc), p.l))

      case DLOAD(_) => //24
        (p => Result(p.s.push(2, DoubleType, pc), p.l))

      case ALOAD(x) => //25
        (p => Result(p.s.push(fromStore(x, p.l.typeStore, ObjectType.Object), pc), p.l))

      case ILOAD_0 | ILOAD_1 | ILOAD_2 | ILOAD_3 => //26,27,28,29
        (p => Result(p.s.push(IntegerType, pc), p.l))

      case LLOAD_0 | LLOAD_1 | LLOAD_2 | LLOAD_3 => //30,31,32,33
        (p => Result(p.s.push(2, LongType, pc), p.l))

      case FLOAD_0 | FLOAD_1 | FLOAD_2 | FLOAD_3 => //34,35,36,37
        (p => Result(p.s.push(1, FloatType, pc), p.l))

      case DLOAD_0 | DLOAD_1 | DLOAD_2 | DLOAD_3 => //38,39,40,41
        (p => Result(p.s.push(2, DoubleType, pc), p.l))

      case ALOAD_0 => //42
        (p => Result(p.s.push(fromStore(0, p.l.typeStore, ObjectType.Object), pc), p.l))
      case ALOAD_1 => //43
        (p => Result(p.s.push(fromStore(1, p.l.typeStore, ObjectType.Object), pc), p.l))
      case ALOAD_2 => //44
        (p => Result(p.s.push(fromStore(2, p.l.typeStore, ObjectType.Object), pc), p.l))
      case ALOAD_3 => //45
        (p => Result(p.s.push(fromStore(3, p.l.typeStore, ObjectType.Object), pc), p.l))

      case LALOAD => //47
        (p => Result(p.s.pop().pop().push(2, LongType, pc), p.l))

      case IALOAD => //48
        (p => Result(p.s.pop().pop().push(1, IntegerType, pc), p.l))

      case FALOAD => //48
        (p => Result(p.s.pop().pop().push(1, FloatType, pc), p.l))

      case DALOAD => //49
        (p => Result(p.s.pop().pop().push(2, DoubleType, pc), p.l))

      case AALOAD => //50
        (p => Result(p.s.pop().pop().push(ObjectType.Object, pc), p.l))

      case BALOAD => //51
        (p => Result(p.s.pop().pop().push(ByteType, pc), p.l))

      case CALOAD => //51
        (p => Result(p.s.pop().pop().push(CharType, pc), p.l))

      case SALOAD => //53
        (p => Result(p.s.pop().pop().push(ShortType, pc), p.l))

      case ISTORE(x) => //54
        (p => Result(p.s.pop(), p.l.setVar(x, IntegerType, pc)))

      case LSTORE(x) => //55
        (p => Result(p.s.pop(), p.l.setVar(x, 2, LongType, pc)))

      case FSTORE(x) => //56
        (p => Result(p.s.pop(), p.l.setVar(x, FloatType, pc)))

      case DSTORE(x) => //57
        (p => Result(p.s.pop(), p.l.setVar(x, 2, DoubleType, pc)))

      case ASTORE(x) => //58
        (p => Result(p.s.pop(), p.l.setVar(x, 1, if (p.s.types != Nil) p.s.types.head else None, Some(pc))))

      case ISTORE_0 => //59
        (p => Result(p.s.pop(), p.l.setVar(0, IntegerType, pc)))
      case ISTORE_1 => //60
        (p => Result(p.s.pop(), p.l.setVar(1, IntegerType, pc)))
      case ISTORE_2 => //61
        (p => Result(p.s.pop(), p.l.setVar(2, IntegerType, pc)))
      case ISTORE_3 => //62
        (p => Result(p.s.pop(), p.l.setVar(3, IntegerType, pc)))

      case LSTORE_0 => //63
        (p => Result(p.s.pop(), p.l.setVar(0, 2, LongType, pc)))
      case LSTORE_1 => //64
        (p => Result(p.s.pop(), p.l.setVar(1, 2, LongType, pc)))
      case LSTORE_2 => //65
        (p => Result(p.s.pop(), p.l.setVar(2, 2, LongType, pc)))
      case LSTORE_3 => //66
        (p => Result(p.s.pop(), p.l.setVar(3, 2, LongType, pc)))

      case FSTORE_0 => //67
        (p => Result(p.s.pop(), p.l.setVar(0, 1, FloatType, pc)))
      case FSTORE_1 => //68
        (p => Result(p.s.pop(), p.l.setVar(1, 1, FloatType, pc)))
      case FSTORE_2 => //69
        (p => Result(p.s.pop(), p.l.setVar(2, 1, FloatType, pc)))
      case FSTORE_3 => //70
        (p => Result(p.s.pop(), p.l.setVar(3, 1, FloatType, pc)))

      case DSTORE_0 => //71
        (p => Result(p.s.pop(), p.l.setVar(0, 2, DoubleType, pc)))
      case DSTORE_1 => //72
        (p => Result(p.s.pop(), p.l.setVar(1, 2, DoubleType, pc)))
      case DSTORE_2 => //73
        (p => Result(p.s.pop(), p.l.setVar(2, 2, DoubleType, pc)))
      case DSTORE_3 => //74
        (p => Result(p.s.pop(), p.l.setVar(3, 2, DoubleType, pc)))

      case ASTORE_0 => //75
        (p => Result(p.s.pop(), p.l.setVar(0, 1, if (p.s.types != Nil) p.s.types.head else None, Some(pc))))
      case ASTORE_1 => //76
        (p => Result(p.s.pop(), p.l.setVar(1, 1, if (p.s.types != Nil) p.s.types.head else None, Some(pc))))
      case ASTORE_2 => //77
        (p => Result(p.s.pop(), p.l.setVar(2, 1, if (p.s.types != Nil) p.s.types.head else None, Some(pc))))
      case ASTORE_3 => //78
        (p => Result(p.s.pop(), p.l.setVar(3, 1, if (p.s.types != Nil) p.s.types.head else None, Some(pc))))


      case x =>
        computeTransformer2(pc, x)
    }
  }

  private def computeTransformer2(pc: Int, instr: Instruction): Transformer = {
    instr match {

      case IASTORE | LASTORE | FASTORE | DASTORE | AASTORE | BASTORE | CASTORE | SASTORE => //79, 80, 81, 82, 83, 84, 85
        (p => Result(p.s.pop().pop().pop(), p.l))

      case POP => //87
        (p => Result(p.s.jPop(1), p.l))

      case POP2 => //88
        (p => Result(p.s.jPop(2), p.l))

      case DUP => //89 //TODO:DUP duplicating pc or setting new pc?
        (p => Result(p.s.jDup(1, 0), p.l))

      case DUP_X1 => //90
        (p => Result(p.s.jDup(1, 1), p.l))

      case DUP_X2 => //91
        (p => Result(p.s.jDup(1, 2), p.l))

      case DUP2 => //92
        (p => Result(p.s.jDup(2, 0), p.l))

      case DUP2_X1 => //93
        (p => Result(p.s.jDup(2, 1), p.l))

      case DUP2_X2 => //94
        (p => Result(p.s.jDup(2, 2), p.l))

      case SWAP => //95
        (p => Result(p.s.jSwap(), p.l))

      case IADD | ISUB | IDIV | IMUL | IREM | ISHL | ISHR |
           IUSHR | IAND | IOR | IXOR => //96, 100, 108, 112, 120, 122, 124, 126, 128, 130
        (p => Result(p.s.pop().pop().push(IntegerType, pc), p.l))

      case LADD | LSUB | LMUL | LDIV | LREM | LSHL | LSHR | LUSHR | LAND | LOR | LXOR => //97, 101, 105, 109, 121, 125, 123, 127, 129, 131
        (p => Result(p.s.pop().pop().push(LongType, pc), p.l))

      case FADD | FDIV | FMUL | FREM | FSUB => //98, 110 ,106,114, 102
        (p => Result(p.s.pop().pop().push(1, FloatType, pc), p.l))

      case DADD | DDIV | DMUL | DREM | DSUB => //99, 111, 107, 115, 103
        (p => Result(p.s.pop().pop().push(2, DoubleType, pc), p.l))


      case INEG | LNEG | FNEG | DNEG => //116, 118, 119
        (p => p)

      case IINC(i, _) => //132
        (p => Result(p.s.pop(), p.l.setVar(i, IntegerType, pc)))

      case I2L => //133
        (p => Result(p.s.pop().push(2, LongType, pc), p.l))
      case I2F => //134
        (p => Result(p.s.pop().push(1, FloatType, pc), p.l))
      case I2D => //135
        (p => Result(p.s.pop().push(2, DoubleType, pc), p.l))

      case L2I => //136
        (p => Result(p.s.pop().push(1, IntegerType, pc), p.l))
      case L2F => //137
        (p => Result(p.s.pop().push(1, FloatType, pc), p.l))
      case L2D => //138
        (p => Result(p.s.pop().push(2, DoubleType, pc), p.l))

      case F2I => //139
        (p => Result(p.s.pop().push(1, IntegerType, pc), p.l))
      case F2L => //140
        (p => Result(p.s.pop().push(2, LongType, pc), p.l))
      case F2D => //141
        (p => Result(p.s.pop().push(2, DoubleType, pc), p.l))

      case D2I => //142
        (p => Result(p.s.pop().push(1, IntegerType, pc), p.l))
      case D2L => //143
        (p => Result(p.s.pop().push(2, LongType, pc), p.l))
      case D2F => //144
        (p => Result(p.s.pop().push(1, FloatType, pc), p.l))

      case I2B => //145
        (p => Result(p.s.pop().push(1, ByteType, pc), p.l))
      case I2C => //146
        (p => Result(p.s.pop().push(1, CharType, pc), p.l))
      case I2S => //147
        (p => Result(p.s.pop().push(1, ShortType, pc), p.l))


      case x =>
        computeTransformer3(pc, x)

    }

  }

  private def computeTransformer3(pc: Int, instr: Instruction): Transformer = {
    instr match {
      case LCMP | FCMPG | FCMPL | DCMPG | DCMPL => //148,149,150,151,152
        (p => Result(p.s.pop().pop().push(IntegerType, pc), p.l))

      case IFEQ(_) | IFNE(_) | IFLT(_) | IFGE(_) | IFGT(_) | IFLE(_) |
           IF_ICMPEQ(_) | IF_ICMPNE(_) | IF_ICMPLT(_) | IF_ICMPGE(_) |
           IF_ICMPGT(_) | IF_ICMPLE(_) | IF_ACMPEQ(_) | IF_ACMPNE(_) => //153,154,155,156,157,158,159,160,161,162,163,164,165,166
        (p => Result(p.s.pop().pop(), p.l))

      case GOTO(_) | GOTO_W(_) => //167, 200
        (p => p)

      case JSR(_) | JSR_W(_) => //168 //TODO: Better push return address
        (p => Result(p.s.push(1, ObjectType.Object, pc), p.l))

      case RET(_) => //169
        (p => p)

      case TABLESWITCH(_, _, _, _) | LOOKUPSWITCH(_, _, _) => //170,171
        (p => Result(p.s.pop(), p.l))

      case IRETURN | LRETURN | ARETURN | FRETURN | DRETURN | RETURN => //172, 173, 174 ,175
        (p => Result(Stacks[Type, Int](p.s.maxSize, Nil, Nil, Nil), p.l))

      case GETSTATIC(_, _, t) => //178
        (p => Result(p.s.push(t.computationalType.operandSize, t, pc), p.l))

      case PUTSTATIC(_, _, _) => //179
        (p => Result(p.s.pop(), p.l))

      case GETFIELD(_, _, t) => //180
        (p => Result(p.s.pop().push(t.computationalType.operandSize, t, pc), p.l))

      case PUTFIELD(_, _, _) => //181
        (p => Result(p.s.pop().pop(), p.l))

      case INVOKEVIRTUAL(_, _, method) => //182
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
        (p => Result(p.s.push(t, pc), p.l))

      case NEWARRAY(aType) => //188
        aType match {
          case 4 =>
            (p => Result(p.s.pop().push(ArrayType(BooleanType), pc), p.l))
          case 5 =>
            (p => Result(p.s.pop().push(ArrayType(CharType), pc), p.l))
          case 6 =>
            (p => Result(p.s.pop().push(ArrayType(FloatType), pc), p.l))
          case 7 =>
            (p => Result(p.s.pop().push(ArrayType(DoubleType), pc), p.l))
          case 8 =>
            (p => Result(p.s.pop().push(ArrayType(ByteType), pc), p.l))
          case 9 =>
            (p => Result(p.s.pop().push(ArrayType(ShortType), pc), p.l))
          case 10 =>
            (p => Result(p.s.pop().push(ArrayType(IntegerType), pc), p.l))
          case 11 =>
            (p => Result(p.s.pop().push(ArrayType(LongType), pc), p.l))
          case _ => {
            System.err.println(aType + ": aType not supported.")
            (p => p)
          }
        }

      case ANEWARRAY(t) => //189
        (p => Result(p.s.pop().push(ArrayType(t), pc), p.l))

      case ARRAYLENGTH => //190
        (p => Result(p.s.pop().push(IntegerType, pc), p.l))

      case ATHROW => //191 //TODO: implement
        (p => p)

      case CHECKCAST(_) => //192
        (p => p)

      case INSTANCEOF(_) => //193
        (p => Result(p.s.pop().push(IntegerType, pc), p.l))

      case MONITORENTER | MONITOREXIT => //194, 195
        (p => Result(p.s.pop(), p.l))

      case WIDE => //196
        (p => p)

      case MULTIANEWARRAY(t, dim) => //197
        (p => {
          var s = p.s
          for (i <- 1 to dim)
            s = p.s.pop()
          Result(s.push(ArrayType(dim, t), pc), p.l)
        })

      case IFNULL(_) | IFNONNULL(_) => //199
        (p => Result(p.s.pop(), p.l))

      case x => {
        System.err.println("Instruction is not supported: " + x.mnemonic)
        (p => p)
      }

    }
  }

  private def invokeTransformer(pc: Int, method: MethodDescriptor, isStatic: Boolean): (Result[Type, Int] => Result[Type, Int]) = {
    (p => {
      var stack = p.s
      for (i <- (if (isStatic) 1 else 0) to method.parameterTypes.size) //use of to: one need to pop the declaring class from the stack
        stack = stack.pop()

      if (!method.returnType.isVoidType)
        stack = stack.push(method.returnType.computationalType.operandSize, method.returnType, pc)

      Result(stack, p.l)
    })
  }

  private def fromStore[A](index: Int, ts: Array[Option[A]], default: A): A = {
    ts(index) match {
      case Some(t) => t
      case None => default
    }
  }


}
