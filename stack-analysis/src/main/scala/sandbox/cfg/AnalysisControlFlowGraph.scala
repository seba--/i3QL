package sandbox.cfg

import sae.bytecode.instructions.InstructionInfo
import sae.{SetRelation, Relation}
import sae.bytecode.structure.CodeInfo
import sae.syntax.sql._


/**
 * This class implements the control flow graph of a program. WILL BE CHANGED!
 *
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 24.10.12
 * Time: 14:33
 * To change this template use File | Settings | File Templates.
 */
class AnalysisControlFlowGraph(ci: Relation[CodeInfo]) {

  private val codeInfo : Relation[CodeInfo] = ci

  private var predecessor : Relation[List[Int]] = null

  def computePredecessors : Relation[List[Int]] = {
    val res : Relation[String] = compile(SELECT ((c : CodeInfo) => (c.declaringMethod.name)) FROM codeInfo WHERE ((_ : CodeInfo).code == null))

  }



}
