package sandbox.findbugs

import sandbox.stackAnalysis.{Result, StackAnalysis}
import sae.bytecode.structure.{MethodDeclaration, CodeInfo}
import sae.Relation
import sae.syntax.sql.SELECT
import sandbox.dataflowAnalysis.MethodResult
import sandbox.stackAnalysis.Result
import de.tud.cs.st.bat.resolved._
import sae.syntax.sql.alternative.FROM
import sae.syntax.sql._


/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 09.11.12
 * Time: 14:20
 * To change this template use File | Settings | File Templates.
 */
class StackBugAnalysis(ciRel: Relation[CodeInfo], analysis: StackAnalysis) {

  case class BugEntry(declaringMethod: MethodDeclaration, log: BugLogger) {

  }

  val result: Relation[BugEntry] =
    compile(SELECT ((ci : CodeInfo, mr : MethodResult[Result[Type,Int]]) => BugEntry(ci.declaringMethod, computeBugLogger(ci,mr))) FROM (ciRel, analysis.result) WHERE (((_ : CodeInfo).declaringMethod) === ((_ : MethodResult[Result[Type,Int]]).declaringMethod)))

 private def computeBugLogger(ci : CodeInfo, mr : MethodResult[Result[Type,Int]]) : BugLogger = {
  return new BugLogger()
 }

}
