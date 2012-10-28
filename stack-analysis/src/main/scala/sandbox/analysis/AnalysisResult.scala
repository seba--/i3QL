package sandbox.analysis


/**
 * This class implements the result of a stack analysis.
 *
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 25.10.12
 * Time: 14:01
 * To change this template use File | Settings | File Templates.
 */
class AnalysisResult(s: AnalysisStack[Int], l: AnalysisLocalVars[VarValue.Value]) {
  val stack: AnalysisStack[Int] = s
  val locals: AnalysisLocalVars[VarValue.Value] = l

  override def toString = "(" + stack.toString + " / " + locals.toString + ")"


}
