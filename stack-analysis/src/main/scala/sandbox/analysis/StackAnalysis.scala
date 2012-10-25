package sandbox.analysis

import sandbox.CFG.AnalysisControlFlowGraph
import sae.bytecode.instructions.InstructionInfo

/**
 * This class implements the stack analysis as an instance of data flow analysis.
 *
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 25.10.12
 * Time: 15:29
 * To change this template use File | Settings | File Templates.
 */
class StackAnalysis(cfg: AnalysisControlFlowGraph, mss: Int, mlv: Int) extends DataFlowAnalysis[AnalysisResult](cfg) {

  private val maxStackSize = mss
  private val maxLocalVariables = mlv

  def startValue = new AnalysisResult(new AnalysisStack[Int], new AnalysisLocalVars[Int](maxLocalVariables))

  def applyTo(instr: InstructionInfo, current: AnalysisResult): AnalysisResult = {
    instr.instruction.opcode match {
      case 2 => new AnalysisResult(current.stack.push(instr.pc), current.locals) //ICONST_M1
      case 4 => new AnalysisResult(current.stack.push(instr.pc), current.locals) //ICONST_1
      case 27 => new AnalysisResult(current.stack.push(instr.pc), current.locals) //ILOAD_1
      case 28 => new AnalysisResult(current.stack.push(instr.pc), current.locals) //ILOAD_2
      case 60 => new AnalysisResult(current.stack.pop(), current.locals.setVar(1, instr.pc)) //ISTORE_1
      case 61 => new AnalysisResult(current.stack.pop(), current.locals.setVar(2, instr.pc)) //ISTORE_2
      case 62 => new AnalysisResult(current.stack.pop(), current.locals.setVar(3, instr.pc)) //ISTORE_3
      case 96 => new AnalysisResult(current.stack.pop().pop().push(instr.pc), current.locals) //IADD
      case _ => current //Other expression do not change the outcome

    }
  }
}
