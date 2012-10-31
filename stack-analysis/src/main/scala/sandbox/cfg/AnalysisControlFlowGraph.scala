package sandbox.cfg

import sae.bytecode.instructions.InstructionInfo
import sae.{SetRelation, Relation}
import sae.bytecode.structure.CodeInfo
import sae.syntax.sql.SELECT

/**
 * This class implements the control flow graph of a program. WILL BE CHANGED!
 *
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 24.10.12
 * Time: 14:33
 * To change this template use File | Settings | File Templates.
 */
case class AnalysisControlFlowGraph(ci: SetRelation[CodeInfo]) {

  private var predecessor : Relation[List[Int]] = null

  def computePredecessors : Relation[List[Int]] = {
    null
  }



}
