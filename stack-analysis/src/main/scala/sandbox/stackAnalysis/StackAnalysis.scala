package sandbox.stackAnalysis

import sandbox.dataflowAnalysis.{ResultTransformer, AnalysisCFG, DataFlowAnalysis}
import sae.bytecode.structure.MethodDeclaration

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 04.11.12
 * Time: 23:57
 * To change this template use File | Settings | File Templates.
 */
case class StackAnalysis(g : AnalysisCFG, t : ResultTransformer[StackResult[Int, (Int, VarValue.Value)]]) extends DataFlowAnalysis[StackResult[Int, (Int, VarValue.Value)]](g,t) {

  def startValue(m : MethodDeclaration) : StackResult[Int, (Int, VarValue.Value)] =
    StackResult(new Stack(10),new LocalVars(10))
}
