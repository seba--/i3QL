package sandbox.stackAnalysis.instructionInfo

import sandbox.stackAnalysis.datastructure.{Item, LocalVariables, Stacks, State}
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
case class InstructionState(instruction: InstructionInfo, state: State) {

  override def toString(): String = {
    if (instruction != null)
      return "<" + instruction.pc + ">" + instruction.instruction.mnemonic + "{" + state + "}"
    else
      return "Nothing" + "{" + state + "}"
  }
}
