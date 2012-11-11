package sandbox.stackAnalysis

import sae.Relation
import sae.bytecode.structure.CodeInfo
import sae.syntax.sql._
import de.tud.cs.st.bat.resolved._
import sandbox.dataflowAnalysis.{MethodTransformer, ResultTransformer}
import Types._
import Types.VarType

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
    compile(SELECT((c: CodeInfo) => MethodTransformer(c.declaringMethod, computeFunctions(c.code.instructions, computeTransformer))) FROM codeInfo)
  }

  private def computeFunctions(a: Array[Instruction], fun: (Int, Instruction) => Transformer): Array[Transformer] = {
    val res = Array.fill[Transformer](a.length)(null) //((s,l) => (s,l))

    var currentPC = 0

    while (currentPC != -1) {
      res(currentPC) = fun(currentPC, a(currentPC))
      currentPC = CodeInfoTools.getNextPC(a, currentPC)
    }

    return res
  }

  private def computeTransformer(pc: Int, instr: Instruction): Transformer = {
    instr match {

      case ACONST_NULL => //1
        (p => Result[VarEntry, VarEntry](p.s.push((pc, VarType.vReference)), p.l))

      case ICONST_M1 | ICONST_0 | ICONST_1 | ICONST_2 | ICONST_3 | ICONST_4 | ICONST_5 => //2,3,4,5,6,7,8
        (p => Result[VarEntry, VarEntry](p.s.push((pc, VarType.vInt)), p.l))

      case LCONST_0 | LCONST_1 => //9,10
        (p => Result[VarEntry, VarEntry](p.s.push(2, (pc, VarType.vInt)), p.l))

      case BIPUSH(_) => //16
        (p => Result[VarEntry, VarEntry](p.s.push((pc, VarType.vInt)), p.l))

      case LDC(const) => const match {
        //18
        case ConstantString(_) =>
          (p => Result[VarEntry, VarEntry](p.s.push((pc, VarType.vReference)), p.l))
        case ConstantInteger(_) =>
          (p => Result[VarEntry, VarEntry](p.s.push((pc, VarType.vInt)), p.l))
        case ConstantFloat(_) =>
          (p => Result[VarEntry, VarEntry](p.s.push((pc, VarType.vFloat)), p.l))
        case ConstantClass(_) =>
          (p => Result[VarEntry, VarEntry](p.s.push((pc, VarType.vReference)), p.l))
        case _ =>
          (p => {
            System.err.println("LDC: must be type string, integer, float or class. Found: " + const)
            p
          })
      }

      case LDC2_W(const) => const match {
        //20
        case ConstantLong(_) =>
          (p => Result[VarEntry, VarEntry](p.s.push(2, (pc, VarType.vLong)), p.l))
        case ConstantDouble(_) =>
          (p => Result[VarEntry, VarEntry](p.s.push(2, (pc, VarType.vDouble)), p.l))
        case _ =>
          (p => {
            System.err.println("LDC2_W: must be type double or long. Found: " + const)
            p
          })
      }

      case ILOAD(_) => //21
        (p => Result[VarEntry, VarEntry](p.s.push((pc, VarType.vInt)), p.l))

      case LLOAD(_) => //21
        (p => Result[VarEntry, VarEntry](p.s.push(2, (pc, VarType.vLong)), p.l))

      case ALOAD(_) => //25
        (p => Result[VarEntry, VarEntry](p.s.push((pc, VarType.vReference)), p.l))

      case ILOAD_0 | ILOAD_1 | ILOAD_2 | ILOAD_3 => //26,27,28,29
        (p => Result[VarEntry, VarEntry](p.s.push((pc, VarType.vInt)), p.l))

      case LLOAD_0 | LLOAD_1 | LLOAD_2 | LLOAD_3 => //30,31,32,33
        (p => Result[VarEntry, VarEntry](p.s.push(2, (pc, VarType.vLong)), p.l))

      case ALOAD_0 | ALOAD_1 | ALOAD_2 | ALOAD_3 => //42,43,44,45
        (p => Result[VarEntry, VarEntry](p.s.push((pc, VarType.vReference)), p.l))

      case ISTORE(x) => //54
        (p => Result[VarEntry, VarEntry](p.s.pop(), p.l.setVar(x, (pc, VarType.vInt))))

      case LSTORE(x) => //55
        (p => Result[VarEntry, VarEntry](p.s.pop(), p.l.setVar(x, 2, (pc, VarType.vLong))))

      case ASTORE(x) => //58
        (p => Result[VarEntry, VarEntry](p.s.pop(), p.l.setVar(x, (pc, VarType.vReference))))

      case ISTORE_0 => //59
        (p => Result[VarEntry, VarEntry](p.s.pop(), p.l.setVar(0, (pc, VarType.vInt))))
      case ISTORE_1 => //60
        (p => Result[VarEntry, VarEntry](p.s.pop(), p.l.setVar(1, (pc, VarType.vInt))))
      case ISTORE_2 => //61
        (p => Result[VarEntry, VarEntry](p.s.pop(), p.l.setVar(2, (pc, VarType.vInt))))
      case ISTORE_3 => //62
        (p => Result[VarEntry, VarEntry](p.s.pop(), p.l.setVar(3, (pc, VarType.vInt))))

      case LSTORE_0 => //63
        (p => Result[VarEntry, VarEntry](p.s.pop(), p.l.setVar(0, 2, (pc, VarType.vLong))))
      case LSTORE_1 => //64
        (p => Result[VarEntry, VarEntry](p.s.pop(), p.l.setVar(1, 2, (pc, VarType.vLong))))
      case LSTORE_2 => //65
        (p => Result[VarEntry, VarEntry](p.s.pop(), p.l.setVar(2, 2, (pc, VarType.vLong))))
      case LSTORE_3 => //66
        (p => Result[VarEntry, VarEntry](p.s.pop(), p.l.setVar(3, 2, (pc, VarType.vLong))))

      case ASTORE_0 => //75
        (p => Result[VarEntry, VarEntry](p.s.pop(), p.l.setVar(0, (pc, VarType.vReference))))
      case ASTORE_1 => //76
        (p => Result[VarEntry, VarEntry](p.s.pop(), p.l.setVar(1, (pc, VarType.vReference))))
      case ASTORE_2 => //77
        (p => Result[VarEntry, VarEntry](p.s.pop(), p.l.setVar(2, (pc, VarType.vReference))))
      case ASTORE_3 => //78
        (p => Result[VarEntry, VarEntry](p.s.pop(), p.l.setVar(3, (pc, VarType.vReference))))

      case IADD => //96
        (p => Result[VarEntry, VarEntry](p.s.pop().pop().push((pc, VarType.vInt)), p.l))

      case IINC(_, _) => //132
        (p => p)

      case I2L => //133
        (p => Result[VarEntry, VarEntry](p.s.pop().push(2, (pc, VarType.vLong)), p.l))

      case LCMP => //148
        (p => Result[VarEntry, VarEntry](p.s.pop().pop().push((pc, VarType.vInt)), p.l))

      case IFEQ(_) | IFNE(_) | IFLT(_) | IFGE(_) | IFGT(_) | IFLE(_) |
           IF_ICMPEQ(_) | IF_ICMPNE(_) | IF_ICMPLT(_) | IF_ICMPGE(_) |
           IF_ICMPGT(_) | IF_ICMPLE(_) | IF_ACMPEQ(_) | IF_ACMPNE(_) => //153,154,155,156,157,158,159,160,161,162,163,164,165,166
        (p => Result[VarEntry, VarEntry](p.s.pop().pop(), p.l))

      case GOTO(_) => //167
        (p => p)

      case IRETURN => //172
        (p => Result[VarEntry, VarEntry](Stack[VarEntry](p.s.maxSize, Nil), p.l))

      case x =>
        (p => {
          System.err.println("Instruction is not supported: " + x.mnemonic)
          p
        })
    }
  }
}
