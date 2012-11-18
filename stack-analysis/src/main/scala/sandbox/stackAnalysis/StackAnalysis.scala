package sandbox.stackAnalysis

import sandbox.dataflowAnalysis.{ResultTransformer, AnalysisCFG, DataFlowAnalysis}
import sae.bytecode.structure.CodeInfo
import sae.Relation
import Types._
import de.tud.cs.st.bat.resolved._

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 04.11.12
 * Time: 23:57
 * To change this template use File | Settings | File Templates.
 */
case class StackAnalysis(c: Relation[CodeInfo], g: AnalysisCFG, t: ResultTransformer[StackResult]) extends DataFlowAnalysis[StackResult](c, g, t) {

  override def startValue(ci: CodeInfo): StackResult = {
    var stacks = Stacks[Type, Int](ci.code.maxStack, Nil, Nil, Nil :: Nil)
    var lvs = new LocalVars[Type, Int](ci.code.maxLocals, None, None :: Nil).setVar(0, 1, ObjectType.Class, -1)

    var i: Int = 0
    for (t <- ci.declaringMethod.parameterTypes) {
      i = i + 1
      lvs = lvs.setVar(i, t.computationalType.operandSize, t, -1)

    }


    Result[Type, Int](stacks, lvs)
  }

  override def emptyValue(ci: CodeInfo): StackResult =
    Result[Type, Int](Stacks[Type, Int](ci.code.maxStack, Nil, Nil, Nil), new LocalVars(ci.code.maxLocals, None, Nil))

}
