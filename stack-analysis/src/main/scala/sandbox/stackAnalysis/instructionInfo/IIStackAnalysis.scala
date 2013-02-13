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
    if(edge.isInstanceOf[AnchorControlFlowEdge]) {
      val anchorEdge : AnchorControlFlowEdge = edge.asInstanceOf[AnchorControlFlowEdge]
      Some(ControlFlowVertex(anchorEdge.getHead,State.createStartState(anchorEdge.attribute.max_stack,anchorEdge.attribute.max_locals)))
    } else
      None
  }

  private def stepFunction(edge : ControlFlowEdge, vertex : ControlFlowVertex) = {
    ControlFlowVertex(edge.getHead, BytecodeTransformer.getTransformer(edge.getTail.pc,edge.getTail.instruction)(vertex.state))
  }

  def apply(database: BytecodeDatabase): Relation[ControlFlowVertex] = {
    return new TransactionalFixPointRecursionView[ControlFlowEdge,ControlFlowVertex,InstructionInfo](
      IIControlFlowGraph(database),
      anchorFunction,
      (edge => edge.getTail),
      (vertex => vertex.instruction),
      stepFunction

    )

  }
}
