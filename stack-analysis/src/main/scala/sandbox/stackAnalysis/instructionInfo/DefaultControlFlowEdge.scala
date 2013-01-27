package sandbox.stackAnalysis.instructionInfo

import sae.bytecode.instructions.InstructionInfo
import sae.bytecode.structure.CodeAttribute

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 27.01.13
 * Time: 23:14
 * To change this template use File | Settings | File Templates.
 */
case class DefaultControlFlowEdge(tail: InstructionInfo, head: InstructionInfo) extends ControlFlowEdge{
  def getTail = tail
  def getHead = head

}
