package sandbox.stackAnalysis.instructionInfo

import sae.bytecode.instructions.InstructionInfo

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 27.01.13
 * Time: 23:13
 * To change this template use File | Settings | File Templates.
 */
trait ControlFlowEdge {
  def getPredecessor : InstructionInfo
  def getSuccessor : InstructionInfo
}
