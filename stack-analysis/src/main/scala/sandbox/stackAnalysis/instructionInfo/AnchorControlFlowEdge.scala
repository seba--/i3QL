package sandbox.stackAnalysis.instructionInfo

import sae.bytecode.instructions.InstructionInfo
import sae.bytecode.structure.CodeAttribute


/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 13.12.12
 * Time: 15:12
 * To change this template use File | Settings | File Templates.
 */
case class AnchorControlFlowEdge(tail: InstructionInfo, head: InstructionInfo, attribute : CodeAttribute) extends ControlFlowEdge{
  def getTail = tail
  def getHead = head
}
