package sandbox.stackAnalysis.instructionInfo

import sandbox.stackAnalysis.datastructure.{Item, LocVariables, Stacks, State}
import sae.bytecode.instructions.InstructionInfo
import sae.bytecode.structure.CodeAttribute
import sae.operators.Combinable

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 13.12.12
 * Time: 15:10
 * To change this template use File | Settings | File Templates.
 */
case class ControlFlowVertex(instruction: InstructionInfo, state : State) extends Combinable[ControlFlowVertex] {



  override def toString(): String = {
    if (instruction != null)
      return "<" + instruction.pc + ">" + instruction.instruction.mnemonic + "{" + state + "}"
    else
      return "Nothing" + "{" + state + "}"
  }

  /**
   * Combines this object with another object to create a new object.
   * @param other The object to be combined with.
   * @return A new object that is defined as the combination of this and the other object.
   */
  def combineWith(other: ControlFlowVertex) : ControlFlowVertex = {
     ControlFlowVertex(instruction,state.combineWith(other.state))
  }
}
