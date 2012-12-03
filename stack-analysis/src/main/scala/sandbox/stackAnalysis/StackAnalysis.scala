package sandbox.stackAnalysis

import sandbox.dataflowAnalysis.{ResultTransformer, AnalysisCFG, DataFlowAnalysis}
import sae.bytecode.structure.CodeInfo
import sae.Relation
import de.tud.cs.st.bat.resolved.ObjectType
import sandbox.stackAnalysis.TypeOption.NoneType

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 04.11.12
 * Time: 23:57
 * To change this template use File | Settings | File Templates.
 */
case class StackAnalysis(c: Relation[CodeInfo], g: AnalysisCFG, t: ResultTransformer[Configuration]) extends DataFlowAnalysis[Configuration](c, g, t) {

  override def startValue(ci: CodeInfo): Configuration = {
    var stacks = Stacks(ci.code.maxStack, Nil).addStack()

    var lvs = if (ci.declaringMethod.isStatic)
      LocVariables(Array.fill[LocVariable](ci.code.maxLocals)(LocVariable(NoneType :: Nil)))
    else
      LocVariables(Array.fill[LocVariable](ci.code.maxLocals)(LocVariable(NoneType :: Nil))).setVar(0, ObjectType.Class, -1)

    var i: Int = if (ci.declaringMethod.isStatic) -1 else 0

    for (t <- ci.declaringMethod.parameterTypes) {
      i = i + 1
      lvs = lvs.setVar(i, t, -1)
    }

    Configuration(stacks, lvs)
  }


}
