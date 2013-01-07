package sandbox.stackAnalysis.instructionInfo

import sandbox.stackAnalysis.datastructure.{Item, LocVariables, Stacks, State}
import sae.bytecode.instructions.InstructionInfo
import sae.bytecode.structure.CodeAttribute

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 13.12.12
 * Time: 15:10
 * To change this template use File | Settings | File Templates.
 */
case class ControlFlowVertex(instruction: InstructionInfo) {

  //TODO: Change maxlocals and stacksize
  private var state = State(Stacks(10, Nil), LocVariables(Array.ofDim[Item](8)))

  def setState(s: State) = {
    state = s
  }

  def getState: State = {
    state
  }


  def this(ii: InstructionInfo, attribute: CodeAttribute) = {
    this(ii)
    state = State.createEmptyState(attribute.max_stack, attribute.max_locals)
  }

  def this(attribute: CodeAttribute) = this(null, attribute)

  override def toString(): String = {
    if (instruction != null)
      return "<" + instruction.pc + ">" + instruction.instruction.mnemonic + "{" + state + "}"
    else
      return "Nothing" + "{" + state + "}"
  }

}
