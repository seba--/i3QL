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
  val functions: Relation[TransformerEntry[StackResult[Int, (Int, VarValue.Value)]]] = {
    compile(SELECT((c: CodeInfo) => {
      print("<" + c.declaringMethod.name + ">")
      TransformerEntry(c.declaringMethod, computeFunctionsPriv(c.code.instructions))
    }
    ) FROM codeInfo)
  }

  private def computeFunctionsPriv(a: Array[Instruction]): Array[Transformer] = {
    val res = Array.fill[Transformer](a.length)(null) //((s,l) => (s,l))

    var currentPC = 0

    while (currentPC != -1) {
      res(currentPC) = computeFunctionPriv(currentPC, a(currentPC))
      currentPC = CodeInfoTools.getNextPC(a, currentPC)
    }

    println(res.mkString("FunRes: ", ", ", ""))
    return res
  }

  private def computeFunctionPriv(pc: Int, instr: Instruction): Transformer = {
    instr match {
      case ICONST_M1 => (p => StackResult(p.stack.push(pc), p.lv))
      case ICONST_1 => (p => StackResult(p.stack.push(pc), p.lv))
      case ILOAD_1 => (p => StackResult(p.stack.push(pc), p.lv))
      case ILOAD_2 => (p => StackResult(p.stack.push(pc), p.lv))
      case ISTORE_1 => (p => StackResult(p.stack.pop(), p.lv.setVar(1, (pc, VarValue.vInt))))
      case ISTORE_2 => (p => StackResult(p.stack.pop(), p.lv.setVar(2, (pc, VarValue.vInt))))
      case ISTORE_3 => (p => StackResult(p.stack.pop(), p.lv.setVar(3, (pc, VarValue.vInt))))
      case IADD => (p => StackResult(p.stack.pop().pop().push(pc), p.lv))
      case _ => {
        p => StackResult(p.stack, p.lv)
      }
    }
  }
}
