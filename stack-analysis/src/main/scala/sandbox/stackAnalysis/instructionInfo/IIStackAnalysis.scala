package sandbox.stackAnalysis.instructionInfo

import sae.bytecode.BytecodeDatabase
import sae.Relation
import sae.bytecode.instructions.InstructionInfo
import sae.operators.impl.TransactionalFixPointRecursionView
import sandbox.stackAnalysis.datastructure.State
import sandbox.stackAnalysis.StateTransformer
import sae.bytecode.structure.MethodDeclaration

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 17.01.13
 * Time: 16:10
 * To change this template use File | Settings | File Templates.
 */
object IIStackAnalysis extends (BytecodeDatabase => Relation[StateInfo]) {

  private def anchorFunction(edge: ControlFlowEdge): Option[StateInfo] = {
    if (edge.isInstanceOf[AnchorControlFlowEdge]) {
      val anchorEdge: AnchorControlFlowEdge = edge.asInstanceOf[AnchorControlFlowEdge]
      Some(StateInfo(anchorEdge.getPredecessor.declaringMethod,
        anchorEdge.getPredecessor.pc,
        anchorEdge.getPredecessor.instruction,
        State.createStartState(anchorEdge.attribute.max_stack,
          anchorEdge.attribute.max_locals,
          anchorEdge.attribute.declaringMethod)))
    } else
      None
  }

  private def stepFunction(edge: ControlFlowEdge, stateInfo: StateInfo) : StateInfo = {
    StateInfo(edge.getPredecessor.declaringMethod,
      edge.getSuccessor.pc,
      edge.getSuccessor.instruction,
      StateTransformer(edge.getPredecessor.pc, edge.getPredecessor.instruction, stateInfo.state))
  }

  def apply(database: BytecodeDatabase): Relation[StateInfo] = {
    return new TransactionalFixPointRecursionView[ControlFlowEdge, StateInfo, (MethodDeclaration, Int)](
      IIControlFlowAnalysis(database),
      anchorFunction,
      (edge => (edge.getPredecessor.declaringMethod, edge.getPredecessor.pc)),
      (stateInfo => (stateInfo.declaringMethod,stateInfo.pc)),
      stepFunction
    )

  }
}
