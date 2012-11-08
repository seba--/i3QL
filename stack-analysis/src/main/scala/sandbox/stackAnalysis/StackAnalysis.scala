package sandbox.stackAnalysis

import sandbox.dataflowAnalysis.{ResultTransformer, AnalysisCFG, DataFlowAnalysis}
import sae.bytecode.structure.CodeInfo
import sae.Relation

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 04.11.12
 * Time: 23:57
 * To change this template use File | Settings | File Templates.
 */
case class StackAnalysis(c: Relation[CodeInfo], g: AnalysisCFG, t: ResultTransformer[StackResult[Int, (Int, VarValue.Value)]]) extends DataFlowAnalysis[StackResult[Int, (Int, VarValue.Value)]](c, g, t) {

  def startValue(ci: CodeInfo): StackResult[Int, (Int, VarValue.Value)] =
    StackResult(new Stack(ci.code.maxStack), new LocalVars(ci.code.maxLocals, None :: Nil))

  def emptyValue(ci: CodeInfo): StackResult[Int, (Int, VarValue.Value)] =
    StackResult(new Stack(ci.code.maxStack), new LocalVars(ci.code.maxLocals, Nil))

}
