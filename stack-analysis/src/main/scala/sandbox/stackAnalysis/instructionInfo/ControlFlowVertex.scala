package sandbox.stackAnalysis.instructionInfo

import sandbox.stackAnalysis.datastructure.{Item, LocVariables, Stacks, State}
import sae.bytecode.instructions.InstructionInfo

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 13.12.12
 * Time: 15:10
 * To change this template use File | Settings | File Templates.
 */
case class ControlFlowVertex(instruction: InstructionInfo, state: State) {

  //TODO:change maxstack and max locals
  def this(instruction: InstructionInfo) = this(instruction, State(Stacks(10, Nil), LocVariables(Array.ofDim[Item](8))))

  def this() = this(null, State(Stacks(10, Nil).addStack(), LocVariables(Array.ofDim[Item](8))))


  override def toString(): String = {
    if (instruction != null)
      return "<" + instruction.pc + ">" + instruction.instruction.mnemonic + " is " + state
    else
      return "Nothing is " + state
  }

}
