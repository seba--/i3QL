package sandbox.stackAnalysis.instructionInfo

import sae.bytecode.BytecodeDatabase
import sae.Relation
import sae.bytecode.instructions.InstructionInfo
import sae.operators.impl.TransactionalFixPointRecursionView
import sandbox.stackAnalysis.datastructure.State
import sandbox.stackAnalysis.StateTransformer

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 17.01.13
 * Time: 16:10
 * To change this template use File | Settings | File Templates.
 */
object IIStackAnalysis extends (BytecodeDatabase => Relation[InstructionState]) {

  private def anchorFunction(edge: ControlFlowEdge): Option[InstructionState] = {
    if (edge.isInstanceOf[AnchorControlFlowEdge]) {
      val anchorEdge: AnchorControlFlowEdge = edge.asInstanceOf[AnchorControlFlowEdge]
      Some(InstructionState(anchorEdge.getHead, State.createStartState(anchorEdge.attribute.max_stack, anchorEdge.attribute.max_locals, anchorEdge.attribute.declaringMethod)))
    } else
      None
  }

  private def stepFunction(edge: ControlFlowEdge, vertex: InstructionState) = {
    InstructionState(edge.getHead, StateTransformer.getTransformer(edge.getTail.pc, edge.getTail.instruction)(vertex.state))
  }

  def apply(database: BytecodeDatabase): Relation[InstructionState] = {
    return new TransactionalFixPointRecursionView[ControlFlowEdge, InstructionState, InstructionInfo](
      IIControlFlowAnalysis(database),
      anchorFunction,
      (edge => edge.getTail),
      (vertex => vertex.instruction),
      stepFunction

    )

  }
}
