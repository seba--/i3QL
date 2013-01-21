package sandbox.stackAnalysis.codeInfo

import sandbox.dataflowAnalysis.DataFlowAnalysis
import de.tud.cs.st.bat.resolved.ObjectType
import sandbox.stackAnalysis.datastructure._
import sandbox.stackAnalysis.datastructure.State
import sandbox.stackAnalysis.datastructure.Stacks
import sae.bytecode.structure.CodeInfo
import sandbox.stackAnalysis.datastructure.LocVariables
import sandbox.stackAnalysis.BytecodeTransformer


/**
 * Implementation of a dataflow analysis that analyzes the stack and the local variables.
 */
object CIStackAnalysis extends DataFlowAnalysis[State](CIControlFlowGraph, BytecodeTransformer) {

  override def startValue(ci: CodeInfo): State = {
    var stacks = Stacks(ci.code.maxStack, Nil).addStack()

    var lvs = if (ci.declaringMethod.isStatic)
      LocVariables(Array.fill[Item](ci.code.maxLocals)(Item(ItemType.None, -1, Item.FLAG_IS_NOT_INITIALIZED)))
    else
      LocVariables(Array.fill[Item](ci.code.maxLocals)(Item(ItemType.None, -1, Item.FLAG_IS_NOT_INITIALIZED))).setVar(0, Item(ItemType.SomeRef(ObjectType.Class), -1, Item.FLAG_IS_PARAMETER))

    var i: Int = if (ci.declaringMethod.isStatic) -1 else 0

    for (t <- ci.declaringMethod.parameterTypes) {
      i = i + 1
      lvs = lvs.setVar(i, Item(ItemType.fromType(t), -1, Item.FLAG_IS_PARAMETER))
    }

    State(stacks, lvs)
  }

  override def emptyValue(ci: CodeInfo): State = {
    State.createEmptyState(ci.code.maxStack, ci.code.maxLocals)
  }


}
