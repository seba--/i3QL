package sandbox.analysis

import sae.bytecode.instructions.InstructionInfo
import sandbox.cfg.AnalysisControlFlowGraph

/**
 * Abstract class for dataflow analysis.
 *
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 25.10.12
 * Time: 15:10
 * To change this template use File | Settings | File Templates.
 */
// rename to control flow analysis
abstract class DataFlowAnalysis[T](cfg: AnalysisControlFlowGraph) {
 /*
  private val graph: AnalysisControlFlowGraph = cfg

  def startValue: T

  def exit(instr: InstructionInfo, current: T): T

  def execute(): List[(Int, T)] = {

    var resList: List[(Int, T)] = Nil
   /* var currentInstruction: InstructionInfo = graph.getInstruction(graph.getStartSequenceNumber)
    var currentValue: T = startValue

    while (currentInstruction != null) {
      resList = (currentInstruction.pc, currentValue) :: resList
      currentValue = exit(currentInstruction, currentValue)
      currentInstruction = graph.getNextInstruction(currentInstruction.sequenceIndex)
    }
     */
    return resList

  }
   */

}
