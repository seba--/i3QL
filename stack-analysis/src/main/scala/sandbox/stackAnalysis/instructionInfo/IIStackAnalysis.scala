package sandbox.stackAnalysis.instructionInfo

import sae.bytecode.BytecodeDatabase
import sae.Relation
import sae.bytecode.instructions.InstructionInfo
import sae.operators.impl.TransactionalFixPointRecursionView
import sandbox.stackAnalysis.datastructure.State
import sandbox.stackAnalysis.BytecodeTransformer

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 17.01.13
 * Time: 16:10
 * To change this template use File | Settings | File Templates.
 */
object IIStackAnalysis extends (BytecodeDatabase => Relation[ControlFlowVertex]) {

  private def anchorFunction(edge : ControlFlowEdge) : Option[ControlFlowVertex] = {
    if(edge.previous.pc == 0)
      Some(ControlFlowVertex(edge.previous,State.createStartState(10,6)))
    else
      None
  }

  private def stepFunction(edge : ControlFlowEdge, vertex : ControlFlowVertex) = {
    ControlFlowVertex(edge.next, BytecodeTransformer.getTransformer(edge.previous.pc,edge.previous.instruction)(vertex.state))
  }

  def apply(database: BytecodeDatabase): Relation[ControlFlowVertex] = {
    println("apply")
    return new TransactionalFixPointRecursionView[ControlFlowEdge,ControlFlowVertex,InstructionInfo](
      IIControlFlowGraph(database),
      anchorFunction,
      (edge => edge.previous),
      (vertex => vertex.instruction),
      stepFunction

    )

  }
}
