package sandbox.stackAnalysis.codeInfo

import sandbox.stackAnalysis.datastructure.State
import sae.bytecode.structure.CodeInfo
import sandbox.stackAnalysis.StateTransformer


/**
 * Implementation of a dataflow analysis that analyzes the stack and the local variables.
 */
object CIStackAnalysis extends DataFlowAnalysis[State](CIControlFlowAnalysis, StateTransformer) {

  type MethodStates = MethodResult[State]

  override def startValue(ci: CodeInfo): State = {
    State.createStartState(ci.code.maxStack, ci.code.maxLocals, ci.declaringMethod)
  }

  override def emptyValue(ci: CodeInfo): State = {
    State.createEmptyState(ci.code.maxStack, ci.code.maxLocals)
  }


}
