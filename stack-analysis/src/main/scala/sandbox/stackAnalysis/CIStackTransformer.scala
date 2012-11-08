package sandbox.stackAnalysis

import sae.Relation
import sae.bytecode.structure.CodeInfo
import sae.syntax.sql._
import de.tud.cs.st.bat.resolved._
import sandbox.dataflowAnalysis.{TransformerEntry, ResultTransformer}

/**
 *
 *
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 01.11.12
 * Time: 15:05
 * To change this template use File | Settings | File Templates.
 */
case class CIStackTransformer(codeInfo: Relation[CodeInfo]) extends ResultTransformer[StackResult[Int, (Int, VarValue.Value)]] {

  val result: Relation[TransformerEntry[StackResult[Int, (Int, VarValue.Value)]]] = {
    compile(SELECT((c: CodeInfo) => TransformerEntry(c.declaringMethod, computeFunctions(c.code.instructions, computeTransformer))) FROM codeInfo)
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
      case ICONST_M1 | ICONST_0 | ICONST_1 | ICONST_2 | ICONST_3 | ICONST_4 | ICONST_5 => //2,3,4,5,6,7,8
        (p => StackResult(p.stack.push(pc), p.lv))
      /*    new Function1[StackResult[Int, (Int, VarValue.Value)], StackResult[Int, (Int, VarValue.Value)]] {
      def apply(p: StackResult[Int, (Int, VarValue.Value)]): StackResult[Int, (Int, VarValue.Value)] = StackResult(p.stack.push(pc), p.lv)
      override def toString() = "(p => StackResult(p.stack.push(pc), p.lv))"
    }  */
      case BIPUSH(_) => //16
        (p => StackResult(p.stack.push(pc), p.lv))
      case ILOAD_0 | ILOAD_1 | ILOAD_2 | ILOAD_3 => //26,27,28,29
        (p => StackResult(p.stack.push(pc), p.lv))
      case ISTORE_0 => //59
        (p => StackResult(p.stack.pop(), p.lv.setVar(0, (pc, VarValue.vInt))))
      case ISTORE_1 => //60
        (p => StackResult(p.stack.pop(), p.lv.setVar(1, (pc, VarValue.vInt))))
      case ISTORE_2 => //61
        (p => StackResult(p.stack.pop(), p.lv.setVar(2, (pc, VarValue.vInt))))
      case ISTORE_3 => //62
        (p => StackResult(p.stack.pop(), p.lv.setVar(3, (pc, VarValue.vInt))))
      case IADD => //96
        (p => StackResult(p.stack.pop().pop().push(pc), p.lv))
      case IINC(_, _) => //132
        (p => p)
      case IF_ICMPEQ(_) | IF_ICMPNE(_) | IF_ICMPLT(_) | IF_ICMPGE(_) | IF_ICMPGT(_) | IF_ICMPLE(_) => //159,160,161,162,163,164
        (p => StackResult(p.stack.pop().pop(), p.lv))
      case GOTO(_) => //167
        (p => p)
      case IRETURN => //172
        (p => StackResult(Stack[Int](p.stack.maxSize, Nil), p.lv))
      case _ =>
        (p => p)
    }
  }
}
