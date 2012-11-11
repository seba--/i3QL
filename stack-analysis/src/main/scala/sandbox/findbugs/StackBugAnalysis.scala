package sandbox.findbugs

import sandbox.stackAnalysis.StackAnalysis
import sae.bytecode.structure.{MethodDeclaration, CodeInfo}
import sae.Relation

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 09.11.12
 * Time: 14:20
 * To change this template use File | Settings | File Templates.
 */
class StackBugAnalysis(ci: CodeInfo, analysis: StackAnalysis) {

  case class BugEntry(declaringMethod: MethodDeclaration, log: BugLogger) {

  }

  val result: Relation[BugEntry] = null
}
