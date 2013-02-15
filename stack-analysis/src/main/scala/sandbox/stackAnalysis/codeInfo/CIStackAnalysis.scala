package sandbox.stackAnalysis.codeInfo

import sandbox.dataflowAnalysis.{MethodResult, DataFlowAnalysis}
import de.tud.cs.st.bat.resolved.ObjectType
import sandbox.stackAnalysis.datastructure._
import sandbox.stackAnalysis.datastructure.State
import sandbox.stackAnalysis.datastructure.Stacks
import sae.bytecode.structure.CodeInfo
import sandbox.stackAnalysis.datastructure.LocalVariables
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
