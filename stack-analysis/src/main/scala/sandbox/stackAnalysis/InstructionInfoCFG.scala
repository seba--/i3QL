package sandbox.stackAnalysis

import sae.Relation
import sandbox.dataflowAnalysis.AnalysisCFG

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 01.11.12
 * Time: 14:58
 * To change this template use File | Settings | File Templates.
 */
case class InstructionInfoCFG(rel : Relation[_]) extends AnalysisCFG {
   val predecessors = null

}
