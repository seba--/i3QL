package sandbox.stackAnalysis

import sandbox.dataflowAnalysis.{ResultTransformer, AnalysisCFG, DataFlowAnalysis}
import sae.bytecode.structure.CodeInfo
import sae.Relation
import Types._

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 04.11.12
 * Time: 23:57
 * To change this template use File | Settings | File Templates.
 */
case class StackAnalysis(c: Relation[CodeInfo], g: AnalysisCFG, t: ResultTransformer[StackResult]) extends DataFlowAnalysis[StackResult](c, g, t) {

  override def startValue(ci: CodeInfo): StackResult =
    Result[VarEntry, VarEntry](new Stack(ci.code.maxStack), new LocalVars(ci.code.maxLocals, None :: Nil))

  override def emptyValue(ci: CodeInfo): StackResult =
    Result[VarEntry, VarEntry](new Stack(ci.code.maxStack), new LocalVars(ci.code.maxLocals, Nil))

}
